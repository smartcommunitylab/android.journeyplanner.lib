package eu.trentorise.smartcampus.jp;

import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessorNoDialog;
import eu.trentorise.smartcampus.jp.custom.AsyncTaskNoDialog;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.custom.data.TimeTable;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SmartCheckTTFragment extends FeedbackFragment {
	private static final int DAYS_WINDOWS = 1;
	protected static final String PARAM_SMARTLINE = "smartline";
	private SmartLine params;
	private ArrayList<SmartLine> lines;
	private TimeTable actualTimeTable;
	private long from_date_milisecond;
	private long to_date_milisecond;
	private String[] rows = null;
	private Map<String, String>[] cols = null;
	private String[][] data = null;
	private final int ROW_HEIGHT = 50;
	private final int COL_WIDTH = 100;
	private TableLayout tlMainContent = null;
	private int firstColumn = 0;
	private int endColumn = 0;
	private int day = 0;
	private ProgressDialog progress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_SMARTLINE)) {
			this.params = (SmartLine) savedInstanceState.getParcelable(PARAM_SMARTLINE);
		} else if (getArguments() != null && getArguments().containsKey(PARAM_SMARTLINE)) {
			this.params = (SmartLine) getArguments().getParcelable(PARAM_SMARTLINE);
		}

		create_interval();

		// get the BusTimeTable
		progress = ProgressDialog
				.show(getSherlockActivity(), "", getResources().getString(R.string.loading_dialog_label), true);
		AsyncTaskNoDialog<Object, Void, TimeTable> task = new AsyncTaskNoDialog<Object, Void, TimeTable>(getSherlockActivity(),
				new GetBusTimeTableProcessor(getSherlockActivity()), progress);
		task.execute(from_date_milisecond, to_date_milisecond, params.getRouteID().get(0));
	}

	private void create_interval() {
		Date basic_date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(basic_date);
		cal.add(Calendar.HOUR_OF_DAY, -1);
		// cal.set(Calendar.HOUR_OF_DAY, 0);
		// cal.set(Calendar.MINUTE, 0);
		// cal.set(Calendar.SECOND, 0);
		// cal.set(Calendar.MILLISECOND, 0);
		Date from_date = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		cal.add(Calendar.DAY_OF_YEAR, DAYS_WINDOWS + 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date to_date = cal.getTime();
		from_date_milisecond = from_date.getTime();
		to_date_milisecond = to_date.getTime();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheckbustt, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private class GetBusTimeTableProcessor extends AbstractAsyncTaskProcessorNoDialog<Object, TimeTable> {

		public GetBusTimeTableProcessor(SherlockFragmentActivity activity) {
			super(activity);
		}

		@Override
		public TimeTable performAction(Object... parmas) throws SecurityException, Exception {
			long from_day = (Long) parmas[0];
			long to_day = (Long) parmas[1];
			String routeId = (String) parmas[2];
			return JPHelper.getTransitTimeTableById(from_day, to_day, routeId);
		}

		@Override
		public void handleResult(TimeTable result) {

			actualTimeTable = result;
			// reload the timetable
			try {
				reloadTimeTable(actualTimeTable);
			} catch (Exception e) {
				e.printStackTrace();
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							SmartCheckTTFragment.this.getSherlockActivity().getSupportFragmentManager().popBackStack();
							break;

						}
					}
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(SmartCheckTTFragment.this.getSherlockActivity());
				builder.setMessage("Problem loading").setPositiveButton("Back", dialogClickListener).show();
				if ((progress != null) && (progress.isShowing()))
					progress.dismiss();
			}

		}

	}

	/*
	 * big method that build in runtime the timetable using the result get from
	 * processing
	 */

	private void reloadTimeTable(final TimeTable actualBusTimeTable) throws Exception {

		final int COL_PLACE_WIDTH = 170;
		long actualDate = from_date_milisecond;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
		List<Integer> courseForDay = new ArrayList<Integer>();
		// sum of every column
		int tempNumbCol = 0;
		int daySkipped = 0;
		courseForDay.add(0);
		for (List<Map<String, String>> tt : actualBusTimeTable.getDelays()) {
			tempNumbCol = tempNumbCol + tt.size();
			courseForDay.add(tempNumbCol);
		}

		final int NUM_COLS = tempNumbCol;
		// are equals to the first
		final int NUM_ROWS = actualBusTimeTable.getStops().size(); // keeping it
																	// square
																	// just
																	// because
																	// i'm lazy
		final int NUMB_OF_TT = actualBusTimeTable.getTimes().size();

		cols = new HashMap[NUM_COLS];
		rows = new String[NUM_ROWS];
		data = new String[NUM_ROWS][NUM_COLS];
		for (int i = 0; i < NUM_ROWS; i++) {

			int indexOfDay = 0;
			int indexOfCourseInThatDay = 0;
			rows[i] = actualBusTimeTable.getStops().get(i);

			for (int j = 0; j < NUM_COLS; j++) {

				// if
				// (!actualBusTimeTable.getDelays().get(indexOfDay).isEmpty()){
				while (actualBusTimeTable.getDelays().get(indexOfDay).isEmpty()) {
					indexOfDay++;
				}
				if (i == 0) {
					Map<String, String> delays = actualBusTimeTable.getDelays().get(indexOfDay).get(indexOfCourseInThatDay);
					/*
					 * TODO: TEST
					 */
					// if (delays.isEmpty()) {
					// delays.put(CreatorType.SERVICE.toString(), "1");
					// delays.put(CreatorType.USER.toString(), "2");
					// }
					/*
					 * 
					 */
					cols[j] = delays;
				}

				data[i][j] = actualBusTimeTable.getTimes().get(indexOfDay).get(indexOfCourseInThatDay).get(i);

				if (indexOfCourseInThatDay == actualBusTimeTable.getDelays().get(indexOfDay).size() - 1) {
					indexOfDay++;
					indexOfCourseInThatDay = 0;
				} else {
					indexOfCourseInThatDay++;
				}
				// } else {
				// indexOfDay++;
				// indexOfCourseInThatDay=0;
				// daySkipped++;
				//
				// }
			}
		}

		LinearLayout layout = (LinearLayout) getSherlockActivity().findViewById(R.id.layout_bustt);

		// setup left column with row labels
		LinearLayout leftlayout = new LinearLayout(getSherlockActivity());
		leftlayout.setOrientation(LinearLayout.VERTICAL);
		TextView dayLabel = new TextView(getSherlockActivity());
		dayLabel.setText(getString(R.string.dayLabel));
		dayLabel.setTextAppearance(getSherlockActivity(), R.style.place_tt_jp);
		dayLabel.setBackgroundResource(R.drawable.cell_place);
		dayLabel.setGravity(Gravity.CENTER);
		dayLabel.setMinHeight(ROW_HEIGHT);

		TextView delaysLabel = new TextView(getSherlockActivity());
		delaysLabel.setText(R.string.delaysLabel);
		delaysLabel.setTextAppearance(getSherlockActivity(), R.style.late_tt_system_jp);
		delaysLabel.setBackgroundResource(R.drawable.cell_place);
		delaysLabel.setGravity(Gravity.CENTER);
		delaysLabel.setMinHeight(ROW_HEIGHT);

		leftlayout.addView(dayLabel);
		leftlayout.addView(delaysLabel);

		LinkedScrollView lsvLeftCol = new LinkedScrollView(getSherlockActivity());
		lsvLeftCol.setVerticalScrollBarEnabled(false); // this one will look
														// wrong
		TableLayout tlLeftCol = new TableLayout(getSherlockActivity());
		TableLayout.LayoutParams tlLeftColParams = new TableLayout.LayoutParams();
		tlLeftColParams.width = COL_PLACE_WIDTH;
		tlLeftCol.setLayoutParams(tlLeftColParams);
		for (int i = 0; i < rows.length; i++) {
			TableRow tr = new TableRow(getSherlockActivity());
			TextView tv = new TextView(getSherlockActivity());
			if (i >= 0) // -2 is the blank top left cell (Days) -1 is for the
						// delays
			{
				// set the place with scrolling effect clicking on it
				// <TextView
				// ...
				// android:ellipsize="marquee"
				// android:focusable="true"
				// android:focusableInTouchMode="true"
				// android:marqueeRepeatLimit="1"
				// android:scrollHorizontally="true"
				// android:singleLine="true"
				// ... />

				tv.setText(rows[i]);
				tv.setMinimumHeight(ROW_HEIGHT);
				tv.setWidth(COL_PLACE_WIDTH);
				tv.setEllipsize(TruncateAt.MARQUEE);
				tv.setFocusable(true);
				tv.setFocusableInTouchMode(true);
				tv.setMarqueeRepeatLimit(1);
				tv.setHorizontallyScrolling(true);
				tv.setSingleLine(true);
				tv.setGravity(Gravity.CENTER_VERTICAL);
				tv.setTextAppearance(getSherlockActivity(), R.style.place_tt_jp);
				tv.setBackgroundResource(R.drawable.cell_place);
				tv.setPadding(10, 0, 0, 0);
				tv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

					}
				});

			} else
				tr.addView(new TextView(getSherlockActivity()));
			tr.addView(tv);
			tr.setMinimumHeight(ROW_HEIGHT);

			tlLeftCol.addView(tr);
		}
		lsvLeftCol.addView(tlLeftCol);

		// add the main horizontal scroll
		HorizzontalDetectEndScrollView hsvMainContent = new HorizzontalDetectEndScrollView(getSherlockActivity());
		hsvMainContent.setHorizontalScrollBarEnabled(false); // you could
																// probably
																// leave this
																// one enabled
																// if you want

		LinearLayout llMainContent = new LinearLayout(getSherlockActivity()); // Scroll
																				// view
																				// needs
																				// a
																				// single
																				// child
		llMainContent.setOrientation(LinearLayout.VERTICAL);

		// add the headings
		TableLayout tlColHeadings = new TableLayout(getSherlockActivity());

		TableRow trDay = new TableRow(getSherlockActivity());
		trDay.setMinimumHeight(ROW_HEIGHT);
		TableRow trDelays = new TableRow(getSherlockActivity());
		trDelays.setGravity(Gravity.BOTTOM);
		trDelays.setMinimumHeight(ROW_HEIGHT);
		boolean alternateDay = false;
		int actualDay = 0;

		Date tempDate = new Date(actualDate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(tempDate);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		tempDate = cal.getTime();
		actualDate = tempDate.getTime();

		for (int i = 0; i < cols.length; i++) {
			LinearLayout dll = new LinearLayout(getSherlockActivity());
			dll.setMinimumWidth(COL_WIDTH);
			dll.setMinimumHeight(ROW_HEIGHT);
			dll.setOrientation(LinearLayout.HORIZONTAL);
			dll.setBackgroundColor(getSherlockActivity().getResources().getColor(R.color.sc_light_gray));
			dll.setBackgroundResource(R.drawable.cell_late);
			dll.setGravity(Gravity.CENTER);

			for (Entry<String, String> delay : cols[i].entrySet()) {
				CreatorType ct = CreatorType.getAlertType(delay.getKey());

				TextView tv = new TextView(getSherlockActivity());
				tv.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
				tv.setBackgroundColor(getSherlockActivity().getResources().getColor(R.color.sc_light_gray));
				tv.setBackgroundResource(R.drawable.cell_late);
				tv.setGravity(Gravity.CENTER);

				if (ct.equals(CreatorType.USER)) {
					tv.setTextAppearance(getSherlockActivity(), R.style.late_tt_user_jp);
					tv.setText(getSherlockActivity().getString(R.string.smart_check_tt_delay_user, delay.getValue()));
				} else {
					tv.setTextAppearance(getSherlockActivity(), R.style.late_tt_system_jp);
					tv.setText(getSherlockActivity().getString(R.string.smart_check_tt_delay, delay.getValue()));
					// trDelays.addView(tv);
				}

				dll.addView(tv);
			}
			trDelays.addView(dll);

			// get new day
			if (i == courseForDay.get(actualDay)) {
				// new day
				actualDay++;
				int addDays = 1;
				while (courseForDay.get(actualDay) - courseForDay.get(actualDay - 1) == 0) {
					actualDay++;
					addDays++;
				}
				// get the span from the number of the course for each day
				int toSpan = courseForDay.get(actualDay) - courseForDay.get(actualDay - 1);
				TextView day = new TextView(getSherlockActivity());
				day.setBackgroundColor(getSherlockActivity().getResources().getColor(android.R.color.white));
				tempDate = new Date(actualDate);
				cal = Calendar.getInstance();
				cal.setTime(tempDate);
				cal.add(Calendar.DAY_OF_YEAR, addDays);
				tempDate = cal.getTime();
				actualDate = tempDate.getTime();
				day.setText(dateFormat.format(actualDate));
				day.setTextAppearance(getSherlockActivity(), R.style.day_tt_jp);

				if (alternateDay) {
					day.setBackgroundColor(getSherlockActivity().getResources().getColor(R.color.tt_day_gray));
					alternateDay = false;
				} else {
					alternateDay = true;
				}
				trDay.addView(day);

				TableRow.LayoutParams tableParams = (TableRow.LayoutParams) day.getLayoutParams();
				tableParams.span = toSpan;
				day.setLayoutParams(tableParams); // causes layout update

			}

		}

		tlColHeadings.addView(trDay);
		tlColHeadings.addView(trDelays);
		llMainContent.addView(tlColHeadings);

		// now lets add the main content
		LinkedScrollView lsvMainVertical = new LinkedScrollView(getSherlockActivity());
		lsvMainVertical.setVerticalScrollBarEnabled(false);

		tlMainContent = new TableLayout(getSherlockActivity());
		tlMainContent.setId(R.id.ttTimeTable);
		Handler handler = new MyHandler();
		MyThread thr = new MyThread(handler, day);
		thr.start();

		lsvMainVertical.addView(tlMainContent);

		llMainContent.addView(lsvMainVertical);

		hsvMainContent.addView(llMainContent);

		leftlayout.addView(lsvLeftCol);
		TextView lineNumber = (TextView) getSherlockActivity().findViewById(R.id.lineNumber);
		lineNumber.setText(params.getLine());
		lineNumber.setTextColor(getSherlockActivity().getResources().getColor(R.color.transparent_white));
		lineNumber.setBackgroundColor(params.getColor());
		layout.addView(leftlayout);
		layout.addView(hsvMainContent);

		// the magic
		lsvMainVertical.others.add(lsvLeftCol);
		lsvLeftCol.others.add(lsvMainVertical);

	}

	/* Handler for managing refresh in more steps */
	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			Bundle bundle = msg.getData();
			if (bundle.containsKey("refresh")) {
				Integer dayIndex = bundle.getInt("refresh");
				endColumn = firstColumn + actualTimeTable.getDelays().get(dayIndex).size() - 1;
				for (int i = 0; i < rows.length; i++) {
					TableRow tr;
					if (dayIndex == 0) {
						tr = new TableRow(getSherlockActivity());
					} else {
						tr = (TableRow) ((TableLayout) getSherlockActivity().findViewById(R.id.ttTimeTable)).getChildAt(i);
					}
					tr.setMinimumHeight(ROW_HEIGHT);
					for (int j = firstColumn; j <= endColumn; j++) {
						if (data[i][j] != null) {
							TextView tv = new TextView(getSherlockActivity());
							tv.setMinimumHeight(ROW_HEIGHT);
							if (data[i][j].length() > 0)
								tv.setText(data[i][j].substring(0, 5));
							else
								tv.setText(data[i][j]);
							tv.setMinWidth(COL_WIDTH);
							tv.setTextAppearance(getSherlockActivity(), R.style.hour_tt_jp);
							tv.setGravity(Gravity.CENTER);
							tv.setBackgroundResource(R.drawable.cell_hour);
							tr.addView(tv);
						}
					}
					if (dayIndex == 0) {
						tlMainContent.addView(tr);

					} else if ((progress != null) && (progress.isShowing()))
						progress.dismiss();

				}
			}
			firstColumn = endColumn + 1;
			day = day + 1;
			if (day <= DAYS_WINDOWS)
				new MyThread(this, day).start();
		}

	}

	class MyThread extends Thread {
		private Handler handler;

		public MyThread(Handler handler, int days) {
			this.handler = handler;
		}

		public void run() {

			Message msg = handler.obtainMessage();
			Bundle b = new Bundle();
			b.putInt("refresh", day);
			msg.setData(b);
			handler.sendMessage(msg);

		}

	}
}
