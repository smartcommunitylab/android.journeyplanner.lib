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

import android.location.Address;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.android.common.SCGeocoder;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.android.map.InfoDialog;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;

public class AddressSelectActivity extends BaseActivity implements OnMapLongClickListener {

	private GoogleMap mMap = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapcontainer_jp_v2);

		// getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
		if (((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap() != null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			mMap.setOnMapLongClickListener(this);
			mMap.setMyLocationEnabled(true);

			if (JPHelper.getLocationHelper().getLocation() != null) {
				LatLng centerLatLng = new LatLng(JPHelper.getLocationHelper().getLocation().getLatitudeE6() / 1e6,
						JPHelper.getLocationHelper().getLocation().getLongitudeE6() / 1e6);
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, JPParamsHelper.getZoomLevelMap()));
			} else {
				mMap.moveCamera(CameraUpdateFactory.zoomTo(JPParamsHelper.getZoomLevelMap()));
			}

			Toast.makeText(this, R.string.address_select_toast, Toast.LENGTH_LONG).show();

			FeedbackFragmentInflater.inflateHandleButtonInRelativeLayout(this,
					(RelativeLayout) findViewById(R.id.mapcontainer_relativelayout_jp_v2));
		}
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

	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onMapLongClick(LatLng point) {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(100);

		GeoPoint p = new GeoPoint((int) (point.latitude * 1e6), (int) (point.longitude * 1e6));
		List<Address> addresses = new SCGeocoder(getApplicationContext()).findAddressesAsync(p);

		if (addresses != null && !addresses.isEmpty()) {
			new InfoDialog(AddressSelectActivity.this, addresses.get(0)).show(getSupportFragmentManager(), "me");
		} else {
			Address address = new Address(Locale.getDefault());
			address.setLatitude(point.latitude);
			address.setLongitude(point.longitude);
			String addressLine = "LON " + Double.toString(address.getLongitude()) + ", LAT "
					+ Double.toString(address.getLatitude());
			address.setAddressLine(0, addressLine);
			new InfoDialog(AddressSelectActivity.this, addresses.get(0)).show(getSupportFragmentManager(), "me");
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
