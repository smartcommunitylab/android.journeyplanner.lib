package eu.trentorise.smartcampus.jp;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;

import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.custom.TabListener;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;

public class SmartCheckListFragment extends FeedbackFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getSherlockActivity().getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSherlockActivity().getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheck, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (getSherlockActivity().getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSherlockActivity().getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		ListView optionsListView = (ListView) getSherlockActivity().findViewById(R.id.smart_check_list);
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getSherlockActivity(),
				android.R.layout.simple_list_item_1, JPParamsHelper.getSmartCheckOptions());
		optionsListView.setAdapter(adapter);

		optionsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				String optionName = adapter.getItem(position);
				// Fragment fragment = null;
				ActionBar actionBar = getSherlockActivity().getSupportActionBar();
				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
				fragmentTransaction.remove(SmartCheckListFragment.this);
				fragmentTransaction.commit();
				if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_bus_trento_timetable))) {
					// actionBar.setDisplayShowTitleEnabled(true); // system
					// title
					// actionBar.setDisplayShowHomeEnabled(true); // home icon
					// bar
					actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					actionBar.removeAllTabs();

					// Lines
					ActionBar.Tab tab = actionBar.newTab();
					tab.setText(R.string.tab_lines);
					tab.setTabListener(new TabListener<SmartCheckBusFragment>(getSherlockActivity(), "lines",
							SmartCheckBusFragment.class, null));
					Bundle bundle = new Bundle();
					bundle.putString(SmartCheckBusFragment.PARAM_AID, RoutesHelper.AGENCYID_BUS_TRENTO);
					tab.setTag(bundle);
					actionBar.addTab(tab);

					// Map
					tab = actionBar.newTab();
					tab.setText(R.string.tab_map);
					tab.setTabListener(new TabListener<SmartCheckMapV2Fragment>(getSherlockActivity(), "map",
							SmartCheckMapV2Fragment.class, null));
					bundle = new Bundle();
					bundle.putStringArray(SmartCheckMapV2Fragment.ARG_AGENCY_IDS,
							new String[] { RoutesHelper.AGENCYID_BUS_TRENTO });
					tab.setTag(bundle);
					actionBar.addTab(tab);

					actionBar.selectTab(actionBar.getTabAt(0));

					// // Trento bus timetable
					// fragment = new SmartCheckBusFragment();
					// Bundle bundle = new Bundle();
					// bundle.putString(SmartCheckBusFragment.PARAM_AID,
					// RoutesHelper.AGENCYID_BUS_TRENTO);
					// fragment.setArguments(bundle);
				} else if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_bus_rovereto_timetable))) {
					// Rovereto bus timetable
					// fragment = new SmartCheckBusFragment();
					// Bundle bundle = new Bundle();
					// bundle.putString(SmartCheckBusFragment.PARAM_AID,
					// RoutesHelper.AGENCYID_BUS_ROVERETO);
					// fragment.setArguments(bundle);

					actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					actionBar.removeAllTabs();

					// Lines
					ActionBar.Tab tab = actionBar.newTab();
					tab.setText(R.string.tab_lines);
					tab.setTabListener(new TabListener<SmartCheckBusFragment>(getSherlockActivity(), "lines",
							SmartCheckBusFragment.class, null));
					Bundle bundle = new Bundle();
					bundle.putString(SmartCheckBusFragment.PARAM_AID, RoutesHelper.AGENCYID_BUS_ROVERETO);
					tab.setTag(bundle);
					actionBar.addTab(tab);

					// Map
					tab = actionBar.newTab();
					tab.setText(R.string.tab_map);
					tab.setTabListener(new TabListener<SmartCheckMapV2Fragment>(getSherlockActivity(), "map",
							SmartCheckMapV2Fragment.class, null));
					bundle = new Bundle();
					bundle.putStringArray(SmartCheckMapV2Fragment.ARG_AGENCY_IDS,
							new String[] { RoutesHelper.AGENCYID_BUS_ROVERETO });
					tab.setTag(bundle);
					actionBar.addTab(tab);

					actionBar.selectTab(actionBar.getTabAt(0));
				} else if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_suburban_timetable))) {
					// Suburban bus timetable
					// fragment = new SmartCheckSuburbanFragment();

					actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					actionBar.removeAllTabs();

					// Zones
					ActionBar.Tab tab = actionBar.newTab();
					tab.setText(R.string.tab_zones);
					tab.setTabListener(new TabListener<SmartCheckSuburbanFragment>(getSherlockActivity(), "lines",
							SmartCheckSuburbanFragment.class, null));
					Bundle bundle = new Bundle();
					bundle.putString(SmartCheckSuburbanFragment.PARAM_AID, RoutesHelper.AGENCYID_BUS_SUBURBAN);
					tab.setTag(bundle);
					actionBar.addTab(tab);

					// Map
					tab = actionBar.newTab();
					tab.setText(R.string.tab_map);
					tab.setTabListener(new TabListener<SmartCheckMapV2Fragment>(getSherlockActivity(), "map",
							SmartCheckMapV2Fragment.class, null));
					bundle = new Bundle();
					bundle.putStringArray(SmartCheckMapV2Fragment.ARG_AGENCY_IDS,
							new String[] { RoutesHelper.AGENCYID_BUS_SUBURBAN });
					tab.setTag(bundle);
					actionBar.addTab(tab);

					actionBar.selectTab(actionBar.getTabAt(0));
				} else if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_train_timetable))) {
					// Trains timetable
					// fragment = new SmartCheckTrainFragment();

					actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					actionBar.removeAllTabs();

					// Lines
					ActionBar.Tab tab = actionBar.newTab();
					tab.setText(R.string.tab_lines);
					tab.setTabListener(new TabListener<SmartCheckTrainFragment>(getSherlockActivity(), "lines",
							SmartCheckTrainFragment.class, null));
					Bundle bundle = new Bundle();
					bundle.putStringArray(SmartCheckBusFragment.PARAM_AID, new String[] { RoutesHelper.AGENCYID_TRAIN_BZVR,
							RoutesHelper.AGENCYID_TRAIN_TM, RoutesHelper.AGENCYID_TRAIN_TNBDG });
					tab.setTag(bundle);
					actionBar.addTab(tab);

					// Map
					tab = actionBar.newTab();
					tab.setText(R.string.tab_map);
					tab.setTabListener(new TabListener<SmartCheckMapV2Fragment>(getSherlockActivity(), "map",
							SmartCheckMapV2Fragment.class, null));
					bundle = new Bundle();
					bundle.putStringArray(SmartCheckMapV2Fragment.ARG_AGENCY_IDS,
							new String[] { RoutesHelper.AGENCYID_TRAIN_BZVR, RoutesHelper.AGENCYID_TRAIN_TM,
									RoutesHelper.AGENCYID_TRAIN_TNBDG });
					tab.setTag(bundle);
					actionBar.addTab(tab);

					actionBar.selectTab(actionBar.getTabAt(0));
				} else if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_parking_trento))) {
					// Trento parking availability
					// fragment = new SmartCheckParkingsFragment();
					// Bundle bundle = new Bundle();
					// bundle.putString(SmartCheckParkingsFragment.PARAM_AID,
					// ParkingsHelper.PARKING_AID_TRENTO);
					// fragment.setArguments(bundle);

					actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					actionBar.removeAllTabs();

					// Lines
					ActionBar.Tab tab = actionBar.newTab();
					tab.setText(R.string.tab_parkings);
					tab.setTabListener(new TabListener<SmartCheckParkingsFragment>(getSherlockActivity(), "lines",
							SmartCheckParkingsFragment.class, null));
					Bundle bundle = new Bundle();
					bundle.putString(SmartCheckParkingsFragment.PARAM_AID, ParkingsHelper.PARKING_AID_TRENTO);
					tab.setTag(bundle);
					actionBar.addTab(tab);

					// Map
					tab = actionBar.newTab();
					tab.setText(R.string.tab_map);
					tab.setTabListener(new TabListener<SmartCheckParkingMapV2Fragment>(getSherlockActivity(), "map",
							SmartCheckParkingMapV2Fragment.class, null));
					bundle = new Bundle();
					bundle.putString(SmartCheckParkingMapV2Fragment.PARAM_AID, ParkingsHelper.PARKING_AID_TRENTO);
					tab.setTag(bundle);
					actionBar.addTab(tab);

					actionBar.selectTab(actionBar.getTabAt(0));
				} else if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_parking_rovereto))) {
					// Rovereto parking availability
					// fragment = new SmartCheckParkingsFragment();
					// Bundle bundle = new Bundle();
					// bundle.putString(SmartCheckParkingsFragment.PARAM_AID,
					// ParkingsHelper.PARKING_AID_ROVERETO);
					// fragment.setArguments(bundle);

					actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					actionBar.removeAllTabs();

					// Lines
					ActionBar.Tab tab = actionBar.newTab();
					tab.setText(R.string.tab_lines);
					tab.setTabListener(new TabListener<SmartCheckParkingsFragment>(getSherlockActivity(), "lines",
							SmartCheckParkingsFragment.class, null));
					Bundle bundle = new Bundle();
					bundle.putString(SmartCheckParkingsFragment.PARAM_AID, ParkingsHelper.PARKING_AID_ROVERETO);
					tab.setTag(bundle);
					actionBar.addTab(tab);

					// Map
					tab = actionBar.newTab();
					tab.setText(R.string.tab_map);
					tab.setTabListener(new TabListener<SmartCheckParkingMapV2Fragment>(getSherlockActivity(), "map",
							SmartCheckParkingMapV2Fragment.class, null));
					bundle = new Bundle();
					bundle.putString(SmartCheckParkingMapV2Fragment.PARAM_AID, ParkingsHelper.PARKING_AID_ROVERETO);
					tab.setTag(bundle);
					actionBar.addTab(tab);

					actionBar.selectTab(actionBar.getTabAt(0));
				} else {
					// Toast available soon
					Toast.makeText(getSherlockActivity(), R.string.tmp, Toast.LENGTH_SHORT).show();
				}

				// if (fragment != null) {
				// FragmentTransaction fragmentTransaction =
				// getSherlockActivity().getSupportFragmentManager()
				// .beginTransaction();
				// fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				// fragmentTransaction.replace(Config.mainlayout, fragment);
				// fragmentTransaction.addToBackStack(null);
				// fragmentTransaction.commit();
				// }

			}
		});
	}
}
