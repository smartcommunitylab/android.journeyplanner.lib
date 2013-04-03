package eu.trentorise.smartcampus.jp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView.FindListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.EndlessLinkedScrollView.TimetableNavigation;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessorNoDialog;
import eu.trentorise.smartcampus.jp.custom.AsyncTaskNoDialog;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.custom.data.TimeTable;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SmartCheckTTFragment extends FeedbackFragment implements
		RenderListener, TimetableNavigation {

	private static final int DAYS_WINDOWS = 1;
	protected static final String PARAM_SMARTLINE = "smartline";
	private SmartLine params;
	private ArrayList<SmartLine> lines;
	private TimeTable actualTimeTable;
	private long from_date_milisecond;
	private long to_date_milisecond;
	private String[] stops = null;
	private String[] delays = null;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(PARAM_SMARTLINE)) {
			this.params = (SmartLine) savedInstanceState
					.getParcelable(PARAM_SMARTLINE);
		} else if (getArguments() != null
				&& getArguments().containsKey(PARAM_SMARTLINE)) {
			this.params = (SmartLine) getArguments().getParcelable(
					PARAM_SMARTLINE);
		}

		create_interval();

		// get the BusTimeTable
		AsyncTaskNoDialog<Object, Void, TimeTable> task = new AsyncTaskNoDialog<Object, Void, TimeTable>(
				getSherlockActivity(), new GetBusTimeTableProcessor(
						getSherlockActivity()), null);
		task.execute(from_date_milisecond, to_date_milisecond, params
				.getRouteID().get(0));
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
		from_date_milisecond = from_date.getTime();
		to_date_milisecond = to_date.getTime();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheckbustt, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		mProgressBar = (ProgressBar) getView().findViewById(
				R.id.smartcheckbustt_content_pb);
		TextView lineNumber = (TextView) getSherlockActivity().findViewById(
				R.id.lineNumber);
		lineNumber.setText(params.getLine());
		lineNumber.setTextColor(getSherlockActivity().getResources().getColor(
				R.color.transparent_white));
		lineNumber.setBackgroundColor(params.getColor());
	}

	private class GetBusTimeTableProcessor extends
			AbstractAsyncTaskProcessorNoDialog<Object, TimeTable> {

		public GetBusTimeTableProcessor(SherlockFragmentActivity activity) {
			super(activity);
		}

		@Override
		public TimeTable performAction(Object... parmas)
				throws SecurityException, Exception {
			long from_day = (Long) parmas[0];
			long to_day = (Long) parmas[1];
			String routeId = (String) parmas[2];
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
							SmartCheckTTFragment.this.getSherlockActivity()
									.getSupportFragmentManager().popBackStack();
							break;

						}
					}
				};
				if (SmartCheckTTFragment.this.getSherlockActivity() != null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							SmartCheckTTFragment.this.getSherlockActivity());

					builder.setMessage("Problem loading")
							.setPositiveButton("Back", dialogClickListener)
							.show();
				}
				toggleProgressDialog();
			}

		}

	}

	/*
	 * big method that build in runtime the timetable using the result get from
	 * processing
	 */

	private void reloadTimeTable(final TimeTable actualBusTimeTable)
			throws Exception {

		final int COL_PLACE_WIDTH = 170;
		actualTimeTable = actualBusTimeTable;
		long actualDate = from_date_milisecond;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
		List<Integer> courseForDay = new ArrayList<Integer>();
		// sum of every column
		int tempNumbCol = 0;
		courseForDay.add(0);

		for (List<Integer> tt : actualBusTimeTable.getDelays()) {
			tempNumbCol += tt.size();
			courseForDay.add(tempNumbCol);
		}

		final int NUM_COLS = tempNumbCol;
		final int NUM_ROWS = actualBusTimeTable.getStops().size();

		delays = new String[NUM_COLS];
		stops = new String[NUM_ROWS];
		times = new String[NUM_ROWS][NUM_COLS];

		// Initializing data
		for (int i = 0; i < NUM_ROWS; i++) {

			int indexOfDay = 0;
			int indexOfCourseInThatDay = 0;
			stops[i] = actualBusTimeTable.getStops().get(i);

			for (int j = 0; j < NUM_COLS; j++) {

				while (actualBusTimeTable.getDelays().get(indexOfDay).isEmpty()) {
					indexOfDay++;
				}
				if (i == 0) {
					String late = new String(actualBusTimeTable.getDelays()
							.get(indexOfDay).get(indexOfCourseInThatDay)
							.toString());
					if (late.compareTo("0") != 0) {
						delays[j] = late;
					}

				}

				times[i][j] = actualBusTimeTable.getTimes().get(indexOfDay)
						.get(indexOfCourseInThatDay).get(i);

				if (indexOfCourseInThatDay == actualBusTimeTable.getDelays()
						.get(indexOfDay).size() - 1) {
					indexOfDay++;
					indexOfCourseInThatDay = 0;
				} else {
					indexOfCourseInThatDay++;
				}
			}
		}

		LinearLayout layout = (LinearLayout) getSherlockActivity()
				.findViewById(R.id.layout_bustt);

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
		delaysLabel
				.setTextAppearance(getSherlockActivity(), R.style.late_tt_jp);
		delaysLabel.setBackgroundResource(R.drawable.cell_place);
		delaysLabel.setGravity(Gravity.CENTER);
		delaysLabel.setMinHeight(ROW_HEIGHT);

		leftlayout.addView(dayLabel);
		leftlayout.addView(delaysLabel);

		LinkedScrollView lsvLeftCol = new LinkedScrollView(
				getSherlockActivity());
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

			} else
				tr.addView(new TextView(getSherlockActivity()));
			tr.addView(tv);
			tr.setMinimumHeight(ROW_HEIGHT);

			tlLeftCol.addView(tr);
		}
		lsvLeftCol.addView(tlLeftCol);

		// add the main horizontal scroll
		HorizontalScrollView hsvMainContent = new HorizontalScrollView(
				getSherlockActivity());

		// you could probably leave this one enabled if you want
		hsvMainContent.setHorizontalScrollBarEnabled(false);

		// Scroll view needs a single child
		LinearLayout llMainContent = new LinearLayout(getSherlockActivity());
		llMainContent.setOrientation(LinearLayout.VERTICAL);

		// add the headings
		TableLayout tlColHeadings = new TableLayout(getSherlockActivity());

		// Day row
		tvday = new TextView(getSherlockActivity());
		tvday.setBackgroundColor(getSherlockActivity().getResources().getColor(
				android.R.color.white));
		Date tempDate = new Date(actualDate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(tempDate);
		tempDate = cal.getTime();
		actualDate = tempDate.getTime();
		tvday.setText(dateFormat.format(actualDate));
		tvday.setTextAppearance(getSherlockActivity(), R.style.day_tt_jp);

		// Delays row
		TableRow trDelays = new TableRow(getSherlockActivity());
		trDelays.setId(R.id.delays_row);
		trDelays.setGravity(Gravity.BOTTOM);
		trDelays.setMinimumHeight(ROW_HEIGHT);
		tlColHeadings.addView(trDelays);

		llMainContent.addView(tvday);
		llMainContent.addView(tlColHeadings);

		// now lets add the main content
		mElsvMainContent = new EndlessLinkedScrollView(getSherlockActivity(),
				SmartCheckTTFragment.this);
		mElsvMainContent.tollerance+=20;

		tlMainContent = new TableLayout(getSherlockActivity());
		tlMainContent.setId(R.id.ttTimeTable);

		new RenderTimeTableAsyncTask(this).execute(0, NUM_ROWS);

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
		tlMainContent.setEnabled(true);
		mElsvMainContent.post(new Runnable() {
			
			@Override
			public void run() {
				if(displayedDay==0)
					mElsvMainContent.scrollTo(+30, 0);
				else
					mElsvMainContent.scrollTo(0, 0);
			}
		});
	}

	@Override
	public void onRightOverScrolled() {
		if (displayedDay < DAYS_WINDOWS ) {
			displayedDay++;
			refreshTimes(displayedDay);
		}
	}

	@Override
	public void onLeftOverScrolled() {

		if (displayedDay > 0) {
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
		Date tempDate = new Date(from_date_milisecond);
		Calendar cal = Calendar.getInstance();
		cal.setTime(tempDate);
		cal.add(Calendar.DAY_OF_YEAR, displayedDay);
		tempDate = cal.getTime();
		long actualDate = tempDate.getTime();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
		tvday.setText(dateFormat.format(actualDate));
	}

	private void refreshDelays(int displayedDay) {
		TableRow trDelays = (TableRow) getView().findViewById(R.id.delays_row);
		trDelays.removeAllViews();

		// Delays are available only for the current day.
		if (displayedDay == 0)
			for (int i = 0; i < actualTimeTable.getDelays().get(displayedDay)
					.size(); i++) {
				TextView tv = new TextView(getSherlockActivity());
				tv.setText(delays[i]);
				tv.setMinWidth(COL_WIDTH);
				tv.setMinimumHeight(ROW_HEIGHT);
				tv.setBackgroundColor(Color.LTGRAY);
				tv.setTextAppearance(getSherlockActivity(), R.style.late_tt_jp);
				tv.setBackgroundResource(R.drawable.cell_late);
				tv.setGravity(Gravity.CENTER);
				trDelays.addView(tv);
			}
	}

	private void refreshTimes(int displayDay) {
		toggleProgressDialog();
		tlMainContent.setEnabled(false);
		refreshDayTextView(displayedDay);
		refreshDelays(displayedDay);
		tlMainContent.removeAllViews();
		new RenderTimeTableAsyncTask(this).execute(displayedDay,
				actualTimeTable.getStops().size());
	}

	private class RenderTimeTableAsyncTask extends
			AsyncTask<Integer, TableRow, Boolean> {

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
			endColumn = firstColumn
					+ actualTimeTable.getDelays().get(dayIndex).size() - 1;
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
							tv.setTextAppearance(getSherlockActivity(),
									R.style.hour_tt_jp);
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
			firstColumn = endColumn + 1;
			mRenderListener.onDayFinished(mDayIndex == 0);
		}

	}
}
