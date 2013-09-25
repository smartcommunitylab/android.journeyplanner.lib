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
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
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

		// BetterMapView mapView = new BetterMapView(this,
		// getResources().getString(R.string.maps_api_key));
		// mapView.setClickable(true);
		// mapView.setBuiltInZoomControls(true);
		// MapManager.setBetterMapView(mapView);

		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			getSupportActionBar().removeAllTabs();
		}
		// Bundle extras = getIntent().getExtras();
		// if (extras == null) {
		// android.support.v4.app.FragmentTransaction fragmentTransaction =
		// getSupportFragmentManager().beginTransaction();
		// SherlockFragment fragment = new SmartCheckListFragment();
		// fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		// fragmentTransaction.replace(Config.mainlayout, fragment,
		// TAG_SMARTCHECKLIST);
		// fragmentTransaction.commit();
		// }
		// else manageWidgetIntent(extras);
		String action = getIntent().getAction();
		Bundle extras = getIntent().getExtras();
		if (action != null) {
			manageWidgetIntent(action, extras);
		} else 
		{
			 android.support.v4.app.FragmentTransaction fragmentTransaction =
			 getSupportFragmentManager().beginTransaction();
			 SherlockFragment fragment = new SmartCheckListFragment();
			 fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			 fragmentTransaction.replace(Config.mainlayout, fragment,
			 TAG_SMARTCHECKLIST);
			 fragmentTransaction.commit();
		}
	}

	private void manageWidgetIntent(String action, Bundle extras) {
		String agency = null;
		String routeid = null;
		SmartLine param = null;

		Integer position = 0;
		android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		if (extras != null) {
			agency = extras.getString("AGENCY_ID");
			routeid = extras.getString("ROUTE_ID");
			position = extras.getInt("POSITION");
		}
		/* if parking load parking frame, */
		/* load smartline anch call smartttfragment with correct id */
		if (agency != null) {

			// if
			// ("eu.trentorise.smartcampus.widget.REAL_TIME_BUS".equals(action)
			// ||
			// "eu.trentorise.smartcampus.widget.REAL_TIME_TRAIN".equals(action))
			// {
			Fragment fragment = new SmartCheckTTFragment();
			Bundle b = new Bundle();
			/* autobus */
			if ("eu.trentorise.smartcampus.widget.REAL_TIME_BUS".equals(action)) {
				busLines = RoutesHelper.getSmartLines(this, agency);

				param = new SmartLine(null, busLines.get(position).getRoutesShorts().get(0), busLines.get(position)
						.getColor(), new ArrayList<String>(Arrays.asList(busLines.get(position).getRoutesShorts()
						.get(0))), new ArrayList<String>(Arrays.asList(busLines.get(position).getRoutesLong().get(0))),
						new ArrayList<String>(Arrays.asList(busLines.get(position).getRouteID().get(0))));
			} else if ("eu.trentorise.smartcampus.widget.REAL_TIME_TRAIN".equals(action)) {
				/* train */
				trainLines = RoutesHelper.getRouteDescriptorsList(agencyIds);

				param = new SmartLine(null, getString(trainLines.get(position).getNameResource()), getResources()
						.getColor(R.color.sc_gray), null, null, Arrays.asList(trainLines.get(position).getRouteId()));
			}
			b.putParcelable(SmartCheckTTFragment.PARAM_SMARTLINE, param);
			fragment.setArguments(b);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			fragmentTransaction.replace(Config.mainlayout, fragment, "lines");
			// fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		} else if ("eu.trentorise.smartcampus.widget.REAL_TIME_PARKING".equals(action)) {
			/* parking */
			Fragment fragmentparking = new SmartCheckParkingsFragment();

			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			fragmentTransaction.replace(Config.mainlayout, fragmentparking, "lines");
			// fragmentTransaction.addToBackStack(fragmentparking.getTag());
			fragmentTransaction.commit();

			//
			// Bundle bundle = new Bundle();
			// bundle.putString(SmartCheckBusFragment.PARAM_AID,
			// RoutesHelper.AGENCYID_BUS_TRENTO);
			// android.support.v4.app.FragmentTransaction fragmentTransaction =
			// getSupportFragmentManager().beginTransaction();
			// SherlockFragment fragment = new SmartCheckBusFragment();
			// fragment.setArguments(bundle);
			// fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			// fragmentTransaction.replace(Config.mainlayout, fragment,
			// TAG_SMARTCHECKLIST);
			// fragmentTransaction.commit();

		}
	}

	// private void manageWidgetIntent(Bundle extras) {
	//
	// String value1 = extras.getString("FRAGMENT");
	// if (value1 != null) {
	// Bundle bundle = new Bundle();
	// bundle.putString(SmartCheckBusFragment.PARAM_AID,
	// RoutesHelper.AGENCYID_BUS_TRENTO);
	// android.support.v4.app.FragmentTransaction fragmentTransaction =
	// getSupportFragmentManager().beginTransaction();
	// SherlockFragment fragment = new SmartCheckBusFragment();
	// fragment.setArguments(bundle);
	// fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	// fragmentTransaction.replace(Config.mainlayout, fragment,
	// TAG_SMARTCHECKLIST);
	// fragmentTransaction.commit();
	//
	//
	// }
	// }
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
