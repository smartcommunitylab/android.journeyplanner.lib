package eu.trentorise.smartcampus.jp;

import java.util.List;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.custom.SmartCheckRoutesListAdapter;
import eu.trentorise.smartcampus.jp.helper.processor.SmartCheckTripsProcessor;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;
import eu.trentorise.smartcampus.jp.model.TripData;

public class SmartCheckStopFragment extends SherlockListFragment {

	public static final String ARG_STOP = "stop";
	// protected static final String PARAMS = "line";
	// private SmartLine params = null;
	private SmartCheckRoutesListAdapter adapter;
	private SmartCheckStop stop;

	public SmartCheckStopFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new SmartCheckRoutesListAdapter(getSherlockActivity(), R.layout.smartcheck_trip);
		adapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				TextView smartcheckRoutesMsg = (TextView) getView().findViewById(R.id.smartcheck_routes_msg);
				if (adapter.getCount() == 0) {
					smartcheckRoutesMsg.setText(R.string.smart_check_routes_empty);
					smartcheckRoutesMsg.setVisibility(View.VISIBLE);
				} else {
					smartcheckRoutesMsg.setVisibility(View.GONE);
				}

				super.onChanged();
			}
		});

		setListAdapter(adapter);

		this.stop = (SmartCheckStop) getArguments().getSerializable(ARG_STOP);

		// LOAD
		if (this.stop != null) {
			new SCAsyncTask<SmartCheckStop, Void, List<TripData>>(getSherlockActivity(), new SmartCheckTripsProcessor(
					getSherlockActivity(), adapter)).execute(stop);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheck_stop, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(), getView());
		// if (this.stop != null) {
		// }
	}

}
