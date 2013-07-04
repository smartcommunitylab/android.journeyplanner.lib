package eu.trentorise.smartcampus.jp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.R.layout;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.custom.SmartCheckBusAdapter;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SmartCheckBusFragment extends FeedbackFragment {

	protected static final String PARAM_AID = "agencyid";
	private String agencyId;

	private GridView busGridView;
	private List<SmartLine> busLines = new ArrayList<SmartLine>();
	private SmartCheckBusAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_AID)) {
			this.agencyId = savedInstanceState.getString(PARAM_AID);
		} else if (getArguments() != null && getArguments().containsKey(PARAM_AID)) {
			this.agencyId = getArguments().getString(PARAM_AID);
		}
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
		busLines = RoutesHelper.getSmartLines(getSherlockActivity(), agencyId);

		// get lines from array
		busGridView = (GridView) getSherlockActivity().findViewById(R.id.smart_check_grid);
		adapter = new SmartCheckBusAdapter(getSherlockActivity(), layout.smart_option_bus_element, busLines);
		busGridView.setAdapter(adapter);
		busGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// call the fragment with the bus direction
				if (busLines.get(position).getRoutesLong().size() != 1) {
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					Fragment fragment = new SmartCheckBusDirectionFragment();
					Bundle b = new Bundle();
					b.putParcelable(SmartCheckBusDirectionFragment.PARAM_LINE, busLines.get(position));
					fragment.setArguments(b);
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					fragmentTransaction.replace(Config.mainlayout, fragment, "lines");
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();
				} else {
					// call directly the
					// fragment with the bus timetable
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					Fragment fragment = new SmartCheckTTFragment();
					Bundle b = new Bundle();
					SmartLine param = new SmartLine(null, busLines.get(position).getRoutesShorts().get(0), busLines.get(
							position).getColor(), new ArrayList<String>(Arrays.asList(busLines.get(position).getRoutesShorts()
							.get(0))), new ArrayList<String>(Arrays.asList(busLines.get(position).getRoutesLong().get(0))),
							new ArrayList<String>(Arrays.asList(busLines.get(position).getRouteID().get(0))));
					b.putParcelable(SmartCheckTTFragment.PARAM_SMARTLINE, param);
					fragment.setArguments(b);
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					fragmentTransaction.replace(Config.mainlayout, fragment, "lines");
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();
				}
			}

		});

		// RelativeLayout chooseStop = (RelativeLayout) getSherlockActivity()
		// .findViewById(R.id.smart_check_bus_choose_stop_layout);
		//
		// chooseStop.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Intent intent = new Intent(getSherlockActivity(),
		// StopSelectActivity.class);
		// intent.putExtra(StopSelectActivity.ARG_AGENCY_IDS, new String[] {
		// agencyId });
		// startActivityForResult(intent, StopSelectActivity.REQUEST_CODE);
		// }
		// });
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
