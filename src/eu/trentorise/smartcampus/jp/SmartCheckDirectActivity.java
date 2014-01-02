package eu.trentorise.smartcampus.jp;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.jp.custom.TabListener;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.helper.AlertRoadsHelper;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;

public class SmartCheckDirectActivity extends BaseActivity {

	public static final String TAG_SMARTCHECKLIST = "smartchecklist";
	public static final String SC_NAME = "SC_NAME";

	/**
	 * Open the activity with the specific smart check option
	 * @param ctx
	 * @param scName
	 */
	public static void startSmartCheck(Context ctx, String scName) {
		Intent i = new Intent(ctx, SmartCheckDirectActivity.class);
		i.putExtra(SC_NAME, scName);
		ctx.startActivity(i);
	}
	
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty_layout_jp);
		
		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			getSupportActionBar().removeAllTabs();
		}
		startFragment(getIntent().getStringExtra(SC_NAME));
	}

	@Override
	protected void onResume() {
		super.onResume();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.title_smart_check);
		
		//Pre HoneyComb hot-fix
		setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

//	@Override
//	public void onBackPressed() {
//		SherlockFragment smartCheckFragment = (SherlockFragment) getSupportFragmentManager().findFragmentByTag(
//				TAG_SMARTCHECKLIST);
//
//		if (getSupportFragmentManager().getBackStackEntryCount() == 0
//				&& getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
//			
//				getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//				getSupportActionBar().removeAllTabs();
//			
//			
//			
//			android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//			SherlockFragment fragment;
//			if (smartCheckFragment == null ) {
//				fragment = new SmartCheckListFragment();
//			} else {
//				fragment = smartCheckFragment;
//			}
//			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//			fragmentTransaction.replace(Config.mainlayout, fragment, TAG_SMARTCHECKLIST);
//			fragmentTransaction.commit();
//			
//		} else {
//			super.onBackPressed();
//		}
//	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public String getAppToken() {
		return JPParamsHelper.getAppToken();
	}

	@Override
	public String getAuthToken() {
		try {
			return JPHelper.getAuthToken(this);
		} catch (AACException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void startFragment(String optionName) {
		ActionBar actionBar = getSupportActionBar();
		if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_bus_trento_timetable))) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			actionBar.removeAllTabs();

			// Lines
			ActionBar.Tab tab = actionBar.newTab();
			tab.setText(R.string.tab_lines);
			tab.setTabListener(new TabListener<SmartCheckBusFragment>(this, "lines",
					SmartCheckBusFragment.class, null));
			Bundle bundle = new Bundle();
			bundle.putString(SmartCheckBusFragment.PARAM_AID, RoutesHelper.AGENCYID_BUS_TRENTO);
			tab.setTag(bundle);
			actionBar.addTab(tab);

			// Map
			tab = actionBar.newTab();
			tab.setText(R.string.tab_map);
			tab.setTabListener(new TabListener<SmartCheckMapV2Fragment>(this, "map",
					SmartCheckMapV2Fragment.class, null));
			bundle = new Bundle();
			bundle.putStringArray(SmartCheckMapV2Fragment.ARG_AGENCY_IDS,
					new String[] { RoutesHelper.AGENCYID_BUS_TRENTO });
			tab.setTag(bundle);
			actionBar.addTab(tab);

			actionBar.selectTab(actionBar.getTabAt(0));
		} else if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_bus_rovereto_timetable))) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			actionBar.removeAllTabs();

			// Lines
			ActionBar.Tab tab = actionBar.newTab();
			tab.setText(R.string.tab_lines);
			tab.setTabListener(new TabListener<SmartCheckBusFragment>(this, "lines",
					SmartCheckBusFragment.class, null));
			Bundle bundle = new Bundle();
			bundle.putString(SmartCheckBusFragment.PARAM_AID, RoutesHelper.AGENCYID_BUS_ROVERETO);
			tab.setTag(bundle);
			actionBar.addTab(tab);

			// Map
			tab = actionBar.newTab();
			tab.setText(R.string.tab_map);
			tab.setTabListener(new TabListener<SmartCheckMapV2Fragment>(this, "map",
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

			ArrayList<SmartLine> smartZones = (ArrayList<SmartLine>) RoutesHelper.getSmartLines(this,
					RoutesHelper.AGENCYID_BUS_SUBURBAN);

			ActionBar.Tab tab = actionBar.newTab();
			tab.setText(R.string.tab_zones);
			Bundle bundle = new Bundle();
			if (smartZones.size() == 1) {
				// Directions
				tab.setTabListener(new TabListener<SmartCheckBusDirectionFragment>(this, "lines",
						SmartCheckBusDirectionFragment.class, null));
				bundle.putParcelable(SmartCheckBusDirectionFragment.PARAM_LINE, smartZones.get(0));
				bundle.putString(SmartCheckBusDirectionFragment.PARAM_AGENCY, RoutesHelper.AGENCYID_BUS_SUBURBAN);
			} else {
				// Zones
			tab.setTabListener(new TabListener<SmartCheckSuburbanFragment>(this, "lines",
					SmartCheckSuburbanFragment.class, null));
				bundle.putParcelableArrayList(SmartCheckSuburbanFragment.PARAM_LINES, smartZones);
			bundle.putString(SmartCheckSuburbanFragment.PARAM_AID, RoutesHelper.AGENCYID_BUS_SUBURBAN);
			}
			tab.setTag(bundle);
			actionBar.addTab(tab);

			// Map
			tab = actionBar.newTab();
			tab.setText(R.string.tab_map);
			tab.setTabListener(new TabListener<SmartCheckMapV2Fragment>(this, "map",
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
			tab.setTabListener(new TabListener<SmartCheckTrainFragment>(this, "lines",
					SmartCheckTrainFragment.class, null));
			Bundle bundle = new Bundle();
			bundle.putStringArray(SmartCheckBusFragment.PARAM_AID, new String[] { RoutesHelper.AGENCYID_TRAIN_BZVR,
					RoutesHelper.AGENCYID_TRAIN_TM, RoutesHelper.AGENCYID_TRAIN_TNBDG });
			tab.setTag(bundle);
			actionBar.addTab(tab);

			// Map
			tab = actionBar.newTab();
			tab.setText(R.string.tab_map);
			tab.setTabListener(new TabListener<SmartCheckMapV2Fragment>(this, "map",
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
			tab.setTabListener(new TabListener<SmartCheckParkingsFragment>(this, "lines",
					SmartCheckParkingsFragment.class, null));
			Bundle bundle = new Bundle();
			bundle.putString(SmartCheckParkingsFragment.PARAM_AID, ParkingsHelper.PARKING_AID_TRENTO);
			tab.setTag(bundle);
			actionBar.addTab(tab);

			// Map
			tab = actionBar.newTab();
			tab.setText(R.string.tab_map);
			tab.setTabListener(new TabListener<SmartCheckParkingMapV2Fragment>(this, "map",
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
			tab.setText(R.string.tab_parkings);
			tab.setTabListener(new TabListener<SmartCheckParkingsFragment>(this, "lines",
					SmartCheckParkingsFragment.class, null));
			Bundle bundle = new Bundle();
			bundle.putString(SmartCheckParkingsFragment.PARAM_AID, ParkingsHelper.PARKING_AID_ROVERETO);
			tab.setTag(bundle);
			actionBar.addTab(tab);

			// Map
			tab = actionBar.newTab();
			tab.setText(R.string.tab_map);
			tab.setTabListener(new TabListener<SmartCheckParkingMapV2Fragment>(this, "map",
					SmartCheckParkingMapV2Fragment.class, null));
			bundle = new Bundle();
			bundle.putString(SmartCheckParkingMapV2Fragment.PARAM_AID, ParkingsHelper.PARKING_AID_ROVERETO);
			tab.setTag(bundle);
			actionBar.addTab(tab);

			actionBar.selectTab(actionBar.getTabAt(0));
		} else if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_alerts_rovereto))) {
			// Rovereto alerts
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			actionBar.removeAllTabs();

			// List
			ActionBar.Tab tab = actionBar.newTab();
			tab.setText(R.string.tab_alerts);
			tab.setTabListener(new TabListener<SmartCheckAlertsFragment>(this, "lines",
					SmartCheckAlertsFragment.class, null));
			Bundle bundle = new Bundle();
			bundle.putString(SmartCheckAlertsFragment.PARAM_AID, AlertRoadsHelper.ALERTS_AID_ROVERETO);
			tab.setTag(bundle);
			actionBar.addTab(tab);

			// Map
			tab = actionBar.newTab();
			tab.setText(R.string.tab_map);
			tab.setTabListener(new TabListener<SmartCheckAlertsMapV2Fragment>(this, "map",
					SmartCheckAlertsMapV2Fragment.class, null));
			bundle = new Bundle();
			bundle.putString(SmartCheckAlertsMapV2Fragment.PARAM_AID, AlertRoadsHelper.ALERTS_AID_ROVERETO);
			tab.setTag(bundle);
			actionBar.addTab(tab);

			actionBar.selectTab(actionBar.getTabAt(0));
		} else {
			Toast.makeText(this, R.string.tmp, Toast.LENGTH_SHORT).show();
		}

	}
}
