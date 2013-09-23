package eu.trentorise.smartcampus.jp;

import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import eu.trentorise.smartcampus.jp.custom.BetterMapView;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;

public class SmartCheckActivity extends BaseActivity {

	public static final String TAG_SMARTCHECKLIST = "smartchecklist";

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		;		
		setContentView(R.layout.empty_layout_jp);
		
//		BetterMapView mapView = new BetterMapView(this, getResources().getString(R.string.maps_api_key));
//		mapView.setClickable(true);
//		mapView.setBuiltInZoomControls(true);
//		MapManager.setBetterMapView(mapView);

		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			getSupportActionBar().removeAllTabs();
		}
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			SherlockFragment fragment = new SmartCheckListFragment();
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			fragmentTransaction.replace(Config.mainlayout, fragment, TAG_SMARTCHECKLIST);
//			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		    }
		else manageWidgetIntent(extras);
		
	}
	private void manageWidgetIntent(Bundle extras) {
	    
	    String value1 = extras.getString("FRAGMENT");
	    if (value1 != null) {
//			Intent intent = new Intent(this, SmartCheckActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString(SmartCheckBusFragment.PARAM_AID, RoutesHelper.AGENCYID_BUS_TRENTO);
//			intent.putExtra(name, value)
//			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//			startActivity(intent);
//	    	SherlockFragment fragment = new SmartCheckBusFragment();
//			fragment.setArguments(args);
//			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//			fragmentTransaction.replace(android.R.id.content, fragment, "events");
//			fragmentTransaction.addToBackStack(fragment.getTag());
//			fragmentTransaction.commit();
			android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			SherlockFragment fragment = new SmartCheckBusFragment();
			fragment.setArguments(bundle);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			fragmentTransaction.replace(Config.mainlayout, fragment, TAG_SMARTCHECKLIST);
//			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
	    	
		
	    }
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

	@Override
	public void onBackPressed() {
		SherlockFragment smartCheckFragment = (SherlockFragment) getSupportFragmentManager().findFragmentByTag(
				TAG_SMARTCHECKLIST);

		if (getSupportFragmentManager().getBackStackEntryCount() == 0 && getSupportActionBar().getNavigationMode()!=ActionBar.NAVIGATION_MODE_STANDARD ) {
//			super.onBackPressed();
			
				getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
				getSupportActionBar().removeAllTabs();
			
			
			
			android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			SherlockFragment fragment;
			if (smartCheckFragment == null ) {
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
