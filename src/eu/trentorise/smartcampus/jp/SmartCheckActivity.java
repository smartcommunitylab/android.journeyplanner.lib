package eu.trentorise.smartcampus.jp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import eu.trentorise.smartcampus.jp.custom.BetterMapView;
import eu.trentorise.smartcampus.jp.custom.TabListener;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.jp.model.RouteDescriptor;

public class SmartCheckActivity extends BaseActivity {

	public static final String TAG_SMARTCHECKLIST = "smartchecklist";
	protected static final String PARAM_AGENCY_ID = "AGENCY_ID";
	protected static final String PARAM_LINE = "LINE";
	protected static final String POSITION = "POSITION";

	private List<SmartLine> busLines = new ArrayList<SmartLine>();
	private String[] agencyIds = new String[] { RoutesHelper.AGENCYID_TRAIN_BZVR, RoutesHelper.AGENCYID_TRAIN_TM,
			RoutesHelper.AGENCYID_TRAIN_TNBDG };
	private List<RouteDescriptor> trainLines = new ArrayList<RouteDescriptor>();

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		;
		setContentView(R.layout.empty_layout_jp);

		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			getSupportActionBar().removeAllTabs();
		}

		String action = getIntent().getAction();
		Bundle extras = getIntent().getExtras();
		if (action != null) {
			manageWidgetIntent(action, extras);
		} else {
			android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			SherlockFragment fragment = new SmartCheckListFragment();
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			fragmentTransaction.replace(Config.mainlayout, fragment, TAG_SMARTCHECKLIST);
			fragmentTransaction.commit();
		}
	}

	private void manageWidgetIntent(String action, Bundle extras) {
		String agency = null;
		String routeid = null;
		SmartLine param = null;
		ActionBar actionBar = getSupportActionBar();
		if (!JPHelper.isInitialized())
			JPHelper.init(this);
		JPHelper.getLocationHelper().start();
		Integer position = 0;
		android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		if (extras != null) {
			agency = extras.getString("AGENCY_ID");
			routeid = extras.getString("ROUTE_ID");
			// position = extras.getInt("POSITION");
		}
		/* if parking load parking frame, */
		/* load smartline anch call smartttfragment with correct id */
		if (agency != null) {

			/* autobus */
			if ("eu.trentorise.smartcampus.widget.REAL_TIME_BUS".equals(action)) {
				navigation_to_bus(agency, routeid, actionBar);

			} else if ("eu.trentorise.smartcampus.widget.REAL_TIME_TRAIN".equals(action)) {
				/* train */
				navigate_to_train(actionBar, routeid);
			}

		} else if ("eu.trentorise.smartcampus.widget.REAL_TIME_PARKING".equals(action)) {
			/* parking */
			navigate_to_parking(actionBar);

		}
	}

	private void navigation_to_bus(String agency, String routeid, ActionBar actionBar) {
		SmartLine param;
		busLines = RoutesHelper.getSmartLines(this, agency);
		/* find position using routeid */
		int pos = 0;
		for (SmartLine smartline : busLines) {
			if (smartline.getRouteID().get(0).equals(routeid)) {
				param = new SmartLine(null, smartline.getRoutesShorts().get(0), smartline.getColor(),
						new ArrayList<String>(Arrays.asList(smartline.getRoutesShorts().get(0))),
						new ArrayList<String>(Arrays.asList(smartline.getRoutesLong().get(0))), new ArrayList<String>(
								Arrays.asList(smartline.getRouteID().get(0))));

				build_tabs(actionBar, param);
				break;
			}
		}

	}

	private void navigate_to_train(ActionBar actionBar, String routeid) {
		SmartLine param;
		trainLines = RoutesHelper.getRouteDescriptorsList(agencyIds);
		int pos = 0;
		for (RouteDescriptor routeDesc : trainLines) {
			if (routeDesc.getRouteId().equals(routeid)) {
				param = new SmartLine(null, getString(routeDesc.getNameResource()), getResources().getColor(
						R.color.sc_gray), null, null, Arrays.asList(routeDesc.getRouteId()));
				build_tabs(actionBar, param);
			}
		}
	}

	private void build_tabs(ActionBar actionBar, SmartLine param) {
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.removeAllTabs();

		// Lines
		ActionBar.Tab tab = actionBar.newTab();
		tab.setText(R.string.tab_lines);
		tab.setTabListener(new TabListener<SmartCheckTTFragment>(this, "lines", SmartCheckTTFragment.class, null));
		Bundle bundle = new Bundle();
		bundle.putParcelable(SmartCheckTTFragment.PARAM_SMARTLINE, param);
		tab.setTag(bundle);
		actionBar.addTab(tab);

		// Map
		tab = actionBar.newTab();
		tab.setText(R.string.tab_map);
		tab.setTabListener(new TabListener<SmartCheckMapV2Fragment>(this, "map", SmartCheckMapV2Fragment.class, null));
		bundle = new Bundle();
		bundle.putStringArray(SmartCheckMapV2Fragment.ARG_AGENCY_IDS, agencyIds);
		tab.setTag(bundle);
		actionBar.addTab(tab);

		actionBar.selectTab(actionBar.getTabAt(0));
	}

	private void navigate_to_parking(ActionBar actionBar) {
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.removeAllTabs();

		// Lines
		ActionBar.Tab tab = actionBar.newTab();
		tab.setText(R.string.tab_lines);
		tab.setTabListener(new TabListener<SmartCheckParkingsFragment>(this, "lines", SmartCheckParkingsFragment.class,
				null));
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.title_smart_check);

		// Pre HoneyComb hot-fix
		setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		SherlockFragment smartCheckFragment = (SherlockFragment) getSupportFragmentManager().findFragmentByTag(
				TAG_SMARTCHECKLIST);

		if (getSupportFragmentManager().getBackStackEntryCount() == 0
				&& getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			// super.onBackPressed();

			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			getSupportActionBar().removeAllTabs();

			android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			SherlockFragment fragment;
			if (smartCheckFragment == null) {
				fragment = new SmartCheckListFragment();
			} else {
				fragment = smartCheckFragment;
			}
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			fragmentTransaction.replace(Config.mainlayout, fragment, TAG_SMARTCHECKLIST);
			fragmentTransaction.commit();

		} else {
			super.onBackPressed();
		}

		// } else {
		// android.support.v4.app.FragmentTransaction fragmentTransaction =
		// getSupportFragmentManager().beginTransaction();
		// fragmentTransaction.attach(smartCheckFragment);
		// fragmentTransaction.commit();
		//
		// }

	}

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
		return JPHelper.getAuthToken();
	}

}
