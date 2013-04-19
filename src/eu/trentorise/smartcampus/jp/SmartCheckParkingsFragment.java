package eu.trentorise.smartcampus.jp;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.DataSetObserver;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.custom.SmartCheckParkingsAdapter;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.processor.SmartCheckParkingsProcessor;
import eu.trentorise.smartcampus.jp.model.Sparking;

public class SmartCheckParkingsFragment extends SherlockListFragment {

	private SmartCheckParkingsAdapter adapter;
	private ParkingsLocationListener parkingLocationListener;

	public SmartCheckParkingsFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true); // system title
		actionBar.setDisplayShowHomeEnabled(true); // home icon bar
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		setHasOptionsMenu(true);

		adapter = new SmartCheckParkingsAdapter(getSherlockActivity(), R.layout.smartcheckparking_row);
		adapter.setMyLocation(JPHelper.getLocationHelper().getLocation());
		adapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				TextView smartcheckRoutesMsg = (TextView) getView().findViewById(R.id.smartcheck_parkings_none);
				if (adapter.getCount() == 0) {
					smartcheckRoutesMsg.setVisibility(View.VISIBLE);
				} else {
					smartcheckRoutesMsg.setVisibility(View.GONE);
				}
				super.onChanged();
			}
		});

		setListAdapter(adapter);

		// LOAD
		new SCAsyncTask<Void, Void, List<Parking>>(getSherlockActivity(), new SmartCheckParkingsProcessor(
				getSherlockActivity(), adapter)).execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheckparkings, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
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
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_map, 1, R.string.menu_item_parking_map);
		item.setIcon(R.drawable.map);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_map) {
			ArrayList<Sparking> spl = new ArrayList<Sparking>();
			for (int i = 0; i < adapter.getCount(); i++) {
				Parking p = adapter.getItem(i);
				Sparking s = new Sparking(p);
				spl.add(s);
			}

			Intent intent = new Intent(getSherlockActivity(), ParkingMapActivity.class);
			intent.putExtra(ParkingMapActivity.ARG_PARKINGS, spl);
			startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
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
