package eu.trentorise.smartcampus.jp;

import java.util.Collections;
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
import eu.trentorise.smartcampus.jp.custom.SmartCheckParkingsAdapter;
import eu.trentorise.smartcampus.jp.helper.processor.SmartCheckParkingsProcessor;

public class SmartCheckParkingsFragment extends SherlockListFragment {

	private SmartCheckParkingsAdapter adapter;
	private List<Object> parkingsList = Collections.emptyList();

	public SmartCheckParkingsFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new SmartCheckParkingsAdapter(getSherlockActivity(), R.layout.smartcheckparking_row, parkingsList);
		adapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				TextView smartcheckRoutesMsg = (TextView) getView().findViewById(R.id.smartcheck_parkings_none);
				if (adapter.getCount() == 0) {
					smartcheckRoutesMsg.setVisibility(View.VISIBLE);
				} else {
					smartcheckRoutesMsg.setVisibility(View.GONE);
				}
				super.onChanged();
			}
		});

		setListAdapter(adapter);

		// LOAD
		new SCAsyncTask<Void, Void, List<Object>>(getSherlockActivity(), new SmartCheckParkingsProcessor(getSherlockActivity(),
				adapter)).execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheckparkings, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(), getView());
	}
}
