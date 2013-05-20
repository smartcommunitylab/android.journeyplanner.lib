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

import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.LegGeometery;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.maps.MapView;

import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.custom.map.LegsOverlay;

public class LegMapActivity extends FeedbackFragmentActivity {

	private MapView mapView = null;

	public static final String ACTIVE_POS = "aPOS";
	public static final String POLYLINES = "polylines";
	public static final String LEGS = "legs";

	private List<String> polylines;
	private int activePos;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (polylines != null) {
			outState.putSerializable(POLYLINES, new ArrayList<String>(polylines));
		}
		outState.putInt(ACTIVE_POS, activePos);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		if (savedInstanceState != null) {
			polylines = (List<String>) savedInstanceState.getSerializable(POLYLINES);
			activePos = savedInstanceState.getInt(ACTIVE_POS);
		} else if (getIntent() != null) {
			List<Leg> legs = (List<Leg>) getIntent().getSerializableExtra(LEGS);
			if (legs != null) {
				polylines = legs2polylines(legs);
			}
			activePos = getIntent().getIntExtra(ACTIVE_POS, -1);
		}

		// getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
		FeedbackFragmentInflater.inflateHandleButtonInRelativeLayout(this,
				(RelativeLayout) findViewById(R.id.map_relativelayout_jp));
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
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); 
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}


	@Override
	protected void onResume() {
		super.onResume();
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.getOverlays().clear();

		LegsOverlay legsOverlay = new LegsOverlay(polylines, activePos, getApplicationContext());
		legsOverlay.adaptMap(mapView.getController());
		mapView.getOverlays().add(legsOverlay);
	}

	protected boolean isRouteDisplayed() {
		return false;
	}

	private List<String> legs2polylines(List<Leg> legs) {
		List<String> polylines = new ArrayList<String>();

		for (Leg leg : legs) {
			LegGeometery lg = leg.getLegGeometery();
			String polyline = lg.getPoints();
			polylines.add(polyline);
		}

		return polylines;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public String getAppToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthToken() {
		// TODO Auto-generated method stub
		return null;
	}

}
