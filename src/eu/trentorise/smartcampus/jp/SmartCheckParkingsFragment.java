package eu.trentorise.smartcampus.jp;

import java.util.List;

import android.database.DataSetObserver;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.custom.SmartCheckParkingsAdapter;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;
import eu.trentorise.smartcampus.jp.helper.processor.SmartCheckParkingsProcessor;
import eu.trentorise.smartcampus.jp.model.ParkingSerial;

public class SmartCheckParkingsFragment extends SherlockListFragment {

	protected static final String PARAM_AID = "parkingAgencyId";
	private String parkingAid;

	private SmartCheckParkingsAdapter adapter;
	private ParkingsLocationListener parkingLocationListener;

	private SCAsyncTask<Void, Void, List<ParkingSerial>> loader;

	public SmartCheckParkingsFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_AID)) {
			this.parkingAid = savedInstanceState.getString(PARAM_AID);
		} else if (getArguments() != null && getArguments().containsKey(PARAM_AID)) {
			this.parkingAid = getArguments().getString(PARAM_AID);
		}

		// ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		// actionBar.setDisplayShowTitleEnabled(true); // system title
		// actionBar.setDisplayShowHomeEnabled(true); // home icon bar
		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		setHasOptionsMenu(true);

		adapter = new SmartCheckParkingsAdapter(getSherlockActivity(), R.layout.smartcheck_parking_row);
		adapter.setMyLocation(JPHelper.getLocationHelper().getLocation());
		// adapter.registerDataSetObserver(new DataSetObserver() {
		// @Override
		// public void onChanged() {
		// if (getView() != null) {
		// TextView smartcheckRoutesMsg = (TextView)
		// getView().findViewById(R.id.smartcheck_none);
		// if (adapter.getCount() == 0) {
		// smartcheckRoutesMsg.setVisibility(View.VISIBLE);
		// } else {
		// smartcheckRoutesMsg.setVisibility(View.GONE);
		// }
		// super.onChanged();
		// }
		// }
		// });

		setListAdapter(adapter);

		// LOAD
		// getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
		loader = new SCAsyncTask<Void, Void, List<ParkingSerial>>(getSherlockActivity(), new SmartCheckParkingsProcessor(
				getSherlockActivity(), adapter, JPHelper.getLocationHelper().getLocation(), parkingAid));
		loader.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheck_list, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		((TextView) getActivity().findViewById(android.R.id.empty)).setText(getString(R.string.smart_check_parkings_empty));
		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(), getView());
	}

	@Override
	public void onResume() {
		if (parkingLocationListener == null) {
			parkingLocationListener = new ParkingsLocationListener();
		}
		JPHelper.getLocationHelper().addLocationListener(parkingLocationListener);
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		JPHelper.getLocationHelper().removeLocationListener(parkingLocationListener);

		if (loader != null) {
			loader.cancel(true);
		}
		SherlockFragmentActivity sfa = getSherlockActivity();
		if (sfa != null) {
			sfa.setSupportProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ParkingSerial parking = adapter.getItem(position);
		goToMap(parking);
	}

	// @Override
	// public void onPrepareOptionsMenu(Menu menu) {
	// menu.clear();
	// MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_map, 1,
	// R.string.menu_item_parking_map);
	// item.setIcon(R.drawable.map);
	// item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	// super.onPrepareOptionsMenu(menu);
	// }

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// if (item.getItemId() == R.id.menu_item_map) {
	// goToParkingsMap(null);
	// return true;
	// } else {
	// return super.onOptionsItemSelected(item);
	// }
	// }

	private void goToMap(ParkingSerial focus) {
		if (focus != null) {
			ParkingsHelper.setFocused(focus);
		}

		getSherlockActivity().getSupportActionBar().setSelectedNavigationItem(1);
	}

	/*
	 * Custom classes
	 */
	private class ParkingsLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			if (adapter != null) {
				adapter.setMyLocation(location);
				adapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	}
}
