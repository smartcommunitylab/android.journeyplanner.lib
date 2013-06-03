/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.jp;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.content.res.Configuration;
import android.location.Address;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import eu.trentorise.smartcampus.android.common.SCGeocoder;
import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.android.map.InfoDialog;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;

public class AddressSelectActivity extends FeedbackFragmentActivity {

	private MapView mapView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapcontainer_jp);

		// getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}

		setContent();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setContent() {

		mapView = new MapView(this, getResources().getString(R.string.maps_api_key));
		// mapView = MapManager.getMapView();
		// setContentView(R.layout.mapcontainer);

		ViewGroup view = (ViewGroup) findViewById(R.id.mapcontainer);
		view.removeAllViews();
		view.addView(mapView);

		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(MapManager.ZOOM_DEFAULT);
		GeoPoint me = null;// MapManager.requestMyLocation(this);
		if (me == null) {
			me = MapManager.DEFAULT_POINT;
		}
		// TODO correct for final version
		mapView.getController().animateTo(me);

		TapOverlay mapOverlay = new TapOverlay();
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.add(mapOverlay);

		Toast.makeText(this, R.string.address_select_toast, Toast.LENGTH_LONG).show();
		FeedbackFragmentInflater.inflateHandleButtonInRelativeLayout(this,
				(RelativeLayout) findViewById(R.id.mapcontainer_relativelayout_jp));
	}

	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private Timer timer = new Timer();
	private TimerTask task = null;

	private class TapOverlay extends Overlay {

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (task != null)
					task.cancel();
				task = new TimerTask() {

					@Override
					public void run() {
						Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
						vibrator.vibrate(100);
					}
				};
				timer.schedule(task, 1000);
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (task != null)
					task.cancel();
				long duration = event.getEventTime() - event.getDownTime();
				if (duration > 1000) {
					// call the dialog box
					GeoPoint p = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
					List<Address> addresses = new SCGeocoder(mapView.getContext()).findAddressesAsync(p);

					if (addresses != null && !addresses.isEmpty()) {
						new InfoDialog(AddressSelectActivity.this, addresses.get(0)).show(getSupportFragmentManager(), "me");

					} else {
						Address address = new Address(Locale.getDefault());
						address.setLatitude(p.getLatitudeE6() / 1E6);
						address.setLongitude(p.getLongitudeE6() / 1E6);
						String addressLine = "LON " + Double.toString(address.getLongitude()) + ", LAT "
								+ Double.toString(address.getLatitude());
						address.setAddressLine(0, addressLine);
						new InfoDialog(AddressSelectActivity.this, addresses.get(0)).show(getSupportFragmentManager(), "me");
					}

				}
			}

			return false;
		}

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
