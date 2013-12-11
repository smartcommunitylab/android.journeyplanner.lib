package eu.trentorise.smartcampus.jp;

import java.util.ArrayList;
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
	protected static final String PARAM_LINES = "lines";

	private ListView routesListView;
	private SmartCheckSuburbanZonesAdapter adapter;

	private String agencyId = RoutesHelper.AGENCYID_BUS_SUBURBAN;

	private ArrayList<SmartLine> smartZones = new ArrayList<SmartLine>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_LINES)) {
			this.smartZones = savedInstanceState.getParcelableArrayList(PARAM_LINES);
		} else if (getArguments() != null && getArguments().containsKey(PARAM_LINES)) {
			this.smartZones = getArguments().getParcelableArrayList(PARAM_LINES);
		}
		
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
		adapter = new SmartCheckSuburbanZonesAdapter(getSherlockActivity(), android.R.layout.simple_list_item_1);

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
				b.putString(SmartCheckBusDirectionFragment.PARAM_AGENCY, agencyId);
				fragment.setArguments(b);
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				fragmentTransaction.replace(Config.mainlayout, fragment, "lines");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		});
	}
}
