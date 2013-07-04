package eu.trentorise.smartcampus.jp;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.custom.SmartCheckSuburbanZonesAdapter;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;

public class SmartCheckSuburbanFragment extends FeedbackFragment {

	protected static final String PARAM_AID = "agencyid";

	private ListView routesListView;
	private SmartCheckSuburbanZonesAdapter adapter;

	private String agencyId = RoutesHelper.AGENCYID_BUS_SUBURBAN;
//
//	private SmartCheckStop selectedStop = null;

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
		return inflater.inflate(R.layout.smartcheck_simple, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		routesListView = (ListView) getSherlockActivity().findViewById(R.id.smart_check_train_list);

		// get routes from Constants
		adapter = new SmartCheckSuburbanZonesAdapter(getSherlockActivity(), android.R.layout.simple_list_item_1);
		List<SmartLine> smartZones = RoutesHelper.getSmartLines(getSherlockActivity(), agencyId);
		for (int i = 0; i < smartZones.size(); i++) {
			adapter.add(smartZones.get(i));
		}
		routesListView.setAdapter(adapter);

		routesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
				Fragment fragment = new SmartCheckBusDirectionFragment();
				Bundle b = new Bundle();
				b.putParcelable(SmartCheckBusDirectionFragment.PARAM_LINE, adapter.getItem(position));
				b.putString(SmartCheckBusDirectionFragment.PARAM_AGENCY, RoutesHelper.AGENCYID_BUS_SUBURBAN);
				fragment.setArguments(b);
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				fragmentTransaction.replace(Config.mainlayout, fragment, "lines");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		});

		// RelativeLayout chooseStop = (RelativeLayout)
		// getSherlockActivity().findViewById(
		// R.id.smart_check_train_choose_stop_layout);
		//
		// chooseStop.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Intent intent = new Intent(getSherlockActivity(),
		// StopSelectActivity.class);
		// intent.putExtra(StopSelectActivity.ARG_AGENCY_IDS, new String[] {
		// RoutesHelper.AGENCYID_BUS_SUBURBAN });
		// startActivityForResult(intent, StopSelectActivity.REQUEST_CODE);
		// }
		// });
	}

//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent result) {
//		super.onActivityResult(requestCode, resultCode, result);
//
//		if (requestCode == StopSelectActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//			if (result.getExtras().containsKey(StopSelectActivity.ARG_STOP)) {
//				selectedStop = (SmartCheckStop) result.getSerializableExtra(StopSelectActivity.ARG_STOP);
//			}
//		}
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//
//		if (selectedStop != null) {
//			FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
//			Fragment fragment = new SmartCheckStopFragment();
//			Bundle args = new Bundle();
//			args.putSerializable(SmartCheckStopFragment.ARG_STOP, selectedStop);
//			fragment.setArguments(args);
//			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//			fragmentTransaction.replace(Config.mainlayout, fragment);
//			fragmentTransaction.addToBackStack(null);
//			// fragmentTransaction.commitAllowingStateLoss();
//			fragmentTransaction.commit();
//			selectedStop = null;
//		}
//	}

}
