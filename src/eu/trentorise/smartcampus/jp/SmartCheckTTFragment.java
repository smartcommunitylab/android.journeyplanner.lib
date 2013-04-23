package eu.trentorise.smartcampus.jp;

import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessorNoDialog;
import eu.trentorise.smartcampus.jp.custom.AsyncTaskNoDialog;
import eu.trentorise.smartcampus.jp.custom.DelaysDialogFragment;
import eu.trentorise.smartcampus.jp.custom.EndlessLinkedScrollView;
import eu.trentorise.smartcampus.jp.custom.EndlessLinkedScrollView.TimetableNavigation;
import eu.trentorise.smartcampus.jp.custom.LinkedScrollView;
import eu.trentorise.smartcampus.jp.custom.RenderListener;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.custom.data.TimeTable;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SmartCheckTTFragment extends FeedbackFragment implements RenderListener, TimetableNavigation {

	private static final int DAYS_WINDOWS = 1;
	protected static final String PARAM_SMARTLINE = "smartline";
	private SmartLine params;
	private TimeTable actualTimeTable;
	private long from_date_millisecond;
	private long to_date_millisecond;
	private String[] stops = null;
	private Map<String, String>[] delays = null;
	private String[][] times = null;
	private final int ROW_HEIGHT = 50;
	private final int COL_WIDTH = 100;
	private TableLayout tlMainContent = null;
	private int firstColumn = 0;
	private int endColumn = 0;
	private ProgressBar mProgressBar;
	private EndlessLinkedScrollView mElsvMainContent;
	private TextView tvday;
	private int displayedDay;
	private boolean firstHasNoCourses;

	private RenderTimeTableAsyncTask renderTimeTableAsyncTask;

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
		AsyncTaskNoDialog<Object, Void, TimeTable> task = new AsyncTaskNoDialog<Object, Void, TimeTable>(getSherlockActivity(),
				new GetBusTimeTableProcessor(getSherlockActivity()));
		task.execute(from_date_millisecond, to_date_millisecond, params.getRouteID().get(0));
	}

	private void create_interval() {
		Date basic_date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(basic_date);
		cal.add(Calendar.HOUR_OF_DAY, -1);
		Date from_date = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		cal.add(Calendar.DAY_OF_YEAR, DAYS_WINDOWS + 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date to_date = cal.getTime();
		from_date_millisecond = from_date.getTime();
		to_date_millisecond = to_date.getTime();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheckbustt, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		getView().setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					renderTimeTableAsyncTask.cancel(true);
					return true;
				}
				return false;
			}
		});

		mProgressBar = (ProgressBar) getView().findViewById(R.id.smartcheckbustt_content_pb);
		TextView lineNumber = (TextView) getSherlockActivity().findViewById(R.id.lineNumber);
		lineNumber.setText(params.getLine());
		lineNumber.setTextColor(getSherlockActivity().getResources().getColor(R.color.transparent_white));
		lineNumber.setBackgroundColor(params.getColor());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			renderTimeTableAsyncTask.cancel(true);
		}
		return super.onOptionsItemSelected(item);
	}

	private class GetBusTimeTableProcessor extends AbstractAsyncTaskProcessorNoDialog<Object, TimeTable> {

		public GetBusTimeTableProcessor(SherlockFragmentActivity activity) {
			super(activity);
		}

		@Override
		public TimeTable performAction(Object... params) throws SecurityException, Exception {
			long from_day = (Long) params[0];
			long to_day = (Long) params[1];
			String routeId = (String) params[2];
			toggleProgressDialog();
			return JPHelper.getTransitTimeTableById(from_day, to_day, routeId);
		}

		@Override
		public void handleResult(TimeTable result) {
			actualTimeTable = result;
			try {
				toggleProgressDialog();
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
				if (SmartCheckTTFragment.this.getSherlockActivity() != null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(SmartCheckTTFragment.this.getSherlockActivity());

					builder.setMessage("Problem loading").setPositiveButton("Back", dialogClickListener).show();
				}
				toggleProgressDialog();
			}

		}

	}

	/*
	 * big method that build in runtime the timetable using the result get from
	 * processing
	 */

	private void reloadTimeTable(final TimeTable actualBusTimeTable) throws Exception {
		final int COL_PLACE_WIDTH = 170;
		actualTimeTable = actualBusTimeTable;
		long actualDate = from_date_millisecond;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
		List<Integer> courseForDay = new ArrayList<Integer>();
		// sum of every column
		int tempNumbCol = 0;
		courseForDay.add(0);

		for (List<Map<String, String>> tt : actualBusTimeTable.getDelays()) {
			tempNumbCol += tt.size();
			courseForDay.add(tempNumbCol);
		}

		final int NUM_COLS = tempNumbCol;
		final int NUM_ROWS = actualBusTimeTable.getStops().size();

		delays = new HashMap[NUM_COLS];
		stops = new String[NUM_ROWS];
		times = new String[NUM_ROWS][NUM_COLS];

		// Initializing data
		for (int i = 0; i < NUM_ROWS; i++) {

			int indexOfDay = 0;
			int indexOfCourseInThatDay = 0;
			stops[i] = actualBusTimeTable.getStops().get(i);

			for (int j = 0; j < NUM_COLS; j++) {
				while (actualBusTimeTable.getDelays().get(indexOfDay).isEmpty()) {
					if (indexOfDay == 0) {
						firstHasNoCourses = true;
						displayedDay = 1;
					}
					indexOfDay++;
				}

				if (i == 0) {
					Map<String, String> actualDelays = actualBusTimeTable.getDelays().get(indexOfDay)
							.get(indexOfCourseInThatDay);
					/*
					 * TODO: TEST
					 */
					// if (actualDelays.isEmpty()) {
					// actualDelays.put(CreatorType.SERVICE.toString(), "1");
					// actualDelays.put(CreatorType.USER.toString(), "2");
					// }
					/*
					 * 
					 */
					delays[j] = actualDelays;
				}

				times[i][j] = actualBusTimeTable.getTimes().get(indexOfDay).get(indexOfCourseInThatDay).get(i);

				if (indexOfCourseInThatDay == actualBusTimeTable.getDelays().get(indexOfDay).size() - 1) {
					if (indexOfDay < DAYS_WINDOWS)
						indexOfDay++;
					indexOfCourseInThatDay = 0;
				} else {
					indexOfCourseInThatDay++;
				}
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
		delaysLabel.setTextAppearance(getSherlockActivity(), R.style.late_tt_jp);
		delaysLabel.setBackgroundResource(R.drawable.cell_place);
		delaysLabel.setGravity(Gravity.CENTER);
		delaysLabel.setMinHeight(ROW_HEIGHT);

		leftlayout.addView(dayLabel);
		leftlayout.addView(delaysLabel);

		LinkedScrollView lsvLeftCol = new LinkedScrollView(getSherlockActivity());
		lsvLeftCol.setVerticalScrollBarEnabled(false);

		TableLayout tlLeftCol = new TableLayout(getSherlockActivity());
		TableLayout.LayoutParams tlLeftColParams = new TableLayout.LayoutParams();
		tlLeftColParams.width = COL_PLACE_WIDTH;
		tlLeftCol.setLayoutParams(tlLeftColParams);
		for (int i = 0; i < stops.length; i++) {
			TableRow tr = new TableRow(getSherlockActivity());
			TextView tv = new TextView(getSherlockActivity());
			if (i >= 0) {

				tv.setText(stops[i]);
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
						// this is needed to view all content of the cell
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
		HorizontalScrollView hsvMainContent = new HorizontalScrollView(getSherlockActivity());

		// you could probably leave this one enabled if you want
		hsvMainContent.setHorizontalScrollBarEnabled(false);

		// Scroll view needs a single child
		LinearLayout llMainContent = new LinearLayout(getSherlockActivity());
		llMainContent.setOrientation(LinearLayout.VERTICAL);

		// add the headings
		TableLayout tlColHeadings = new TableLayout(getSherlockActivity());

		// Day row
		tvday = new TextView(getSherlockActivity());
		tvday.setBackgroundColor(getSherlockActivity().getResources().getColor(android.R.color.white));
		tvday.setTextAppearance(getSherlockActivity(), R.style.day_tt_jp);
		tvday.setMinimumHeight(ROW_HEIGHT);

		refreshDayTextView(displayedDay);

		// Delays row
		TableRow trDelays = new TableRow(getSherlockActivity());
		trDelays.setId(R.id.delays_row);
		trDelays.setGravity(Gravity.BOTTOM);
		trDelays.setMinimumHeight(ROW_HEIGHT);
		tlColHeadings.addView(trDelays);

		llMainContent.addView(tvday);
		llMainContent.addView(tlColHeadings);

		// now lets add the main content
		mElsvMainContent = new EndlessLinkedScrollView(getSherlockActivity(), SmartCheckTTFragment.this);
		mElsvMainContent.tollerance += 20;

		tlMainContent = new TableLayout(getSherlockActivity());
		tlMainContent.setId(R.id.ttTimeTable);
		tlMainContent.setVerticalScrollBarEnabled(true);

		renderTimeTableAsyncTask = new RenderTimeTableAsyncTask(this);
		renderTimeTableAsyncTask.execute(displayedDay, NUM_ROWS);

		mElsvMainContent.addView(tlMainContent);

		llMainContent.addView(mElsvMainContent);

		hsvMainContent.addView(llMainContent);

		leftlayout.addView(lsvLeftCol);

		layout.addView(leftlayout);
		layout.addView(hsvMainContent);

		// the magic
		mElsvMainContent.others.add(lsvLeftCol);
		lsvLeftCol.others.add(mElsvMainContent);

		// this is here because it needs the delays rows already visible.
		refreshDelays(0);

	}

	@Override
	public void addToTimetable(TableRow tr) {
		tlMainContent.addView(tr);
	}

	@Override
	public void onDayFinished(boolean result) {
		toggleProgressDialog();
		mElsvMainContent.setEnabled(true);
		tlMainContent.setEnabled(true);
	}

	@Override
	public void onRightOverScrolled() {
		if (displayedDay < DAYS_WINDOWS) {
			displayedDay++;
			refreshTimes(displayedDay);
		}
	}

	@Override
	public void onLeftOverScrolled() {
		if (displayedDay > 0 && !firstHasNoCourses) {
			displayedDay--;
			firstColumn = 0;
			refreshTimes(displayedDay);
		}
	}

	private void toggleProgressDialog() {
		if (mProgressBar != null) {
			if (mProgressBar.isShown())
				mProgressBar.setVisibility(View.INVISIBLE);
			else
				mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	private void refreshDayTextView(int displayDay) {
		Date tempDate = new Date(from_date_millisecond);
		Calendar cal = Calendar.getInstance();
		cal.setTime(tempDate);
		cal.add(Calendar.DAY_OF_YEAR, displayedDay);
		tempDate = cal.getTime();
		long actualDate = tempDate.getTime();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
		tvday.setText(dateFormat.format(actualDate));
	}

	private void refreshDelays(int displayedDay) {
		TableRow trDelays = (TableRow) getView().findViewById(R.id.delays_row);
		trDelays.removeAllViews();

		// Delays are available only for the current day.
		if (displayedDay == 0) {
			for (int i = 0; i < actualTimeTable.getDelays().get(displayedDay).size(); i++) {
				LinearLayout dll = new LinearLayout(getSherlockActivity());
				dll.setMinimumWidth(COL_WIDTH);
				dll.setMinimumHeight(ROW_HEIGHT);
				dll.setOrientation(LinearLayout.HORIZONTAL);
				dll.setBackgroundColor(getSherlockActivity().getResources().getColor(R.color.sc_light_gray));
				dll.setBackgroundResource(R.drawable.cell_late);
				dll.setGravity(Gravity.CENTER);

				Map<String, String> delaysStringsMap = delays[i];
				final Map<CreatorType, String> delaysCreatorTypesMap = new HashMap<CreatorType, String>();

				for (Entry<String, String> delay : delaysStringsMap.entrySet()) {
					if (!delay.getValue().equalsIgnoreCase("0")) {
						CreatorType ct = CreatorType.getAlertType(delay.getKey());

						delaysCreatorTypesMap.put(ct, delay.getValue());

						TextView tv = new TextView(getSherlockActivity());
						tv.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
								1f));
						tv.setBackgroundColor(getSherlockActivity().getResources().getColor(R.color.sc_light_gray));
						tv.setBackgroundResource(R.drawable.cell_late);
						tv.setGravity(Gravity.CENTER);
						tv.setTextAppearance(getSherlockActivity(), android.R.style.TextAppearance_Small);

						if (ct.equals(CreatorType.USER)) {
							tv.setTextColor(getSherlockActivity().getResources().getColor(R.color.blue));
							tv.setText(getSherlockActivity().getString(R.string.smart_check_tt_delay_user, delay.getValue()));
						} else {
							tv.setTextColor(getSherlockActivity().getResources().getColor(R.color.red));
							tv.setText(getSherlockActivity().getString(R.string.smart_check_tt_delay, delay.getValue()));
						}

						dll.addView(tv);
					}
				}

				if (!delaysCreatorTypesMap.isEmpty()) {
					dll.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							DelaysDialogFragment delaysDialog = new DelaysDialogFragment();
							Bundle args = new Bundle();
							args.putSerializable(DelaysDialogFragment.ARG_DELAYS, (Serializable) delaysCreatorTypesMap);
							delaysDialog.setArguments(args);
							delaysDialog.show(getSherlockActivity().getSupportFragmentManager(), "delaysdialog");
						}
					});
				}

				trDelays.addView(dll);
			}
		}
	}

	private void refreshTimes(int displayDay) {
		toggleProgressDialog();
		mElsvMainContent.setEnabled(false);
		tlMainContent.setEnabled(false);
		refreshDayTextView(displayedDay);
		refreshDelays(displayedDay);
		tlMainContent.removeAllViews();
		new RenderTimeTableAsyncTask(this).execute(displayedDay, actualTimeTable.getStops().size());
	}

	private class RenderTimeTableAsyncTask extends AsyncTask<Integer, TableRow, Boolean> {

		private RenderListener mRenderListener;
		private int mDayIndex;

		public RenderTimeTableAsyncTask(RenderListener mRenderListener) {
			super();
			this.mRenderListener = mRenderListener;
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			Integer dayIndex = params[0];
			mDayIndex = dayIndex;
			endColumn = firstColumn + actualTimeTable.getDelays().get(dayIndex).size() - 1;
			for (int i = 0; i < params[1]; i++) {
				TableRow tr = new TableRow(getSherlockActivity());
				tr.setMinimumHeight(ROW_HEIGHT);
				for (int j = firstColumn; j <= endColumn; j++) {
					if (times[i][j] != null) {
						TextView tv = new TextView(getSherlockActivity());
						if (tv != null) {
							tv.setMinimumHeight(ROW_HEIGHT);
							if (times[i][j].length() > 0)
								tv.setText(times[i][j].substring(0, 5));
							else
								tv.setText(times[i][j]);
							tv.setMinWidth(COL_WIDTH);
							tv.setTextAppearance(getSherlockActivity(), R.style.hour_tt_jp);
							tv.setGravity(Gravity.CENTER);
							tv.setBackgroundResource(R.drawable.cell_hour);
							tr.addView(tv);
						}
					}
				}
				publishProgress(tr);
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(TableRow... values) {
			super.onProgressUpdate(values);
			mRenderListener.addToTimetable(values[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			// the usage of this index is so shitty!
			// TODO find another solution!
			firstColumn = endColumn + 1;
			mRenderListener.onDayFinished(mDayIndex == 0);
		}

	}
}
