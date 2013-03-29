package eu.trentorise.smartcampus.jp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.R.layout;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.custom.SmartCheckBusAdapter;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SmartCheckBusFragment extends FeedbackFragment {
	private GridView busGridView;
	private List<SmartLine> busLines = new ArrayList<SmartLine>();
	private final static String busAgencyId = "12";
	private SmartCheckBusAdapter adapter;
	private SmartCheckStop selectedStop = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheckbus, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		// SCAsyncTask<String, Void, List<SmartLine>> task = new
		// SCAsyncTask<String, Void, List<SmartLine>>(getSherlockActivity(),
		// new GetBusDirectionsProcessor(getSherlockActivity()));
		// task.execute(busAgencyId);
		busLines = RoutesHelper.getSmartLines(getSherlockActivity(), busAgencyId);

		// get lines from array
		busGridView = (GridView) getSherlockActivity().findViewById(R.id.smart_check_grid);
		adapter = new SmartCheckBusAdapter(getSherlockActivity(), layout.smart_option_bus_element, busLines);
		busGridView.setAdapter(adapter);
		busGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// call the fragment with the bus direction
				if (busLines.get(arg2).getRoutesLong().size() != 1) {
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					Fragment fragment = new SmartCheckBusDirectionFragment();
					Bundle b = new Bundle();
					b.putParcelable(SmartCheckBusDirectionFragment.PARAMS, busLines.get(arg2));
					fragment.setArguments(b);
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					fragmentTransaction.replace(Config.mainlayout, fragment);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
				} else {
					// call directly the
					// fragment with the bus timetable
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					Fragment fragment = new SmartCheckTTFragment();
					Bundle b = new Bundle();
					SmartLine param = new SmartLine(null, busLines.get(arg2).getRoutesShorts().get(0), busLines.get(arg2)
							.getColor(), new ArrayList<String>(Arrays.asList(busLines.get(arg2).getRoutesShorts().get(0))),
							new ArrayList<String>(Arrays.asList(busLines.get(arg2).getRoutesLong().get(0))),
							new ArrayList<String>(Arrays.asList(busLines.get(arg2).getRouteID().get(0))));
					b.putParcelable(SmartCheckTTFragment.PARAM_SMARTLINE, param);
					fragment.setArguments(b);
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					fragmentTransaction.replace(Config.mainlayout, fragment);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
				}
			}

		});

		RelativeLayout chooseStop = (RelativeLayout) getSherlockActivity()
				.findViewById(R.id.smart_check_bus_choose_stop_layout);

		chooseStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getSherlockActivity(), StopSelectActivity.class);
				intent.putExtra(StopSelectActivity.ARG_AGENCY_IDS, new int[] { RoutesHelper.AGENCYID_BUS });
				startActivityForResult(intent, StopSelectActivity.REQUEST_CODE);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent result) {
		super.onActivityResult(requestCode, resultCode, result);

		if (requestCode == StopSelectActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			if (result.getExtras().containsKey(StopSelectActivity.ARG_STOP)) {
				selectedStop = (SmartCheckStop) result.getSerializableExtra(StopSelectActivity.ARG_STOP);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (selectedStop != null) {
			FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
			Fragment fragment = new SmartCheckStopFragment();
			Bundle args = new Bundle();
			args.putSerializable(SmartCheckStopFragment.ARG_STOP, selectedStop);
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(Config.mainlayout, fragment);
			fragmentTransaction.addToBackStack(null);
			// fragmentTransaction.commitAllowingStateLoss();
			fragmentTransaction.commit();
			selectedStop = null;
		}
	}

	public class GetBusDirectionsProcessor extends AbstractAsyncTaskProcessor<String, List<SmartLine>> {

		public GetBusDirectionsProcessor(SherlockFragmentActivity activity) {
			super(activity);

		}

		@Override
		public List<SmartLine> performAction(String... params) throws SecurityException, Exception {
			return JPHelper.getSmartLinesByAgencyId(params[0]);
		}

		@Override
		public void handleResult(List<SmartLine> result) {

			busLines = new ArrayList<SmartLine>(result);
			for (SmartLine sl : result) {
				System.out.println(sl.getRoutesShorts());
				System.out.println(sl.getRoutesLong());
			}
			adapter = new SmartCheckBusAdapter(getSherlockActivity(), layout.smart_option_bus_element, busLines);
			busGridView.setAdapter(adapter);
		}
	}
}
