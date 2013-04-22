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

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.custom.AsyncTaskNoDialog;
import eu.trentorise.smartcampus.jp.custom.BetterMapView;
import eu.trentorise.smartcampus.jp.custom.BetterMapView.OnMapChanged;
import eu.trentorise.smartcampus.jp.custom.StopsAsyncTask;
import eu.trentorise.smartcampus.jp.custom.StopsAsyncTask.OnStopLoadingFinished;
import eu.trentorise.smartcampus.jp.custom.map.StopObjectMapItemTapListener;
import eu.trentorise.smartcampus.jp.custom.map.StopsInfoDialog;
import eu.trentorise.smartcampus.jp.custom.map.StopsItemizedOverlay;
import eu.trentorise.smartcampus.jp.custom.map.StopsMapLoadProcessor;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

public class StopSelectActivity extends FeedbackFragmentActivity implements
		StopObjectMapItemTapListener, OnMapChanged, OnStopLoadingFinished {

	public final static String ARG_AGENCY_IDS = "agencyIds";
	public final static String ARG_STOP = "stop";
	public final static int REQUEST_CODE = 1983;

	private BetterMapView mapView = null;
	private MyLocationOverlay mMyLocationOverlay = null;
	StopsItemizedOverlay mItemizedoverlay = null;

	private int[] selectedAgencyIds = null;
	private SmartCheckStop selectedStop = null;
	private StopsAsyncTask active;
	private double diagonalold;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.mapcontainer_jp);

		if (getIntent().getIntArrayExtra(ARG_AGENCY_IDS) != null) {
			selectedAgencyIds = getIntent().getIntArrayExtra(ARG_AGENCY_IDS);
		}

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true); // back arrow
		actionBar.setDisplayUseLogoEnabled(false); // system logo
		actionBar.setDisplayShowTitleEnabled(true); // system title
		actionBar.setDisplayShowHomeEnabled(false); // home icon bar
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // tabs
	
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
		setSupportProgressBarIndeterminateVisibility(true);
		FeedbackFragmentInflater
				.inflateHandleButtonInRelativeLayout(
						this,
						(RelativeLayout) findViewById(R.id.mapcontainer_relativelayout_jp));

		mapView = new BetterMapView(this, getResources().getString(
				R.string.maps_api_key), StopSelectActivity.this);
		// mapView = MapManager.getMapView();
		// setContentView(R.layout.mapcontainer);

		ViewGroup view = (ViewGroup) findViewById(R.id.mapcontainer);
		view.removeAllViews();
		view.addView(mapView);

		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(17);

		List<Overlay> listOfOverlays = mapView.getOverlays();
		mItemizedoverlay = new StopsItemizedOverlay(this, mapView);
		mItemizedoverlay.setMapItemTapListener(this);
		listOfOverlays.add(mItemizedoverlay);

		mMyLocationOverlay = new MyLocationOverlay(getApplicationContext(),
				mapView) {
			@Override
			protected void drawMyLocation(Canvas canvas, MapView mapView,
					Location lastFix, GeoPoint myLocation, long when) {
				Projection p = mapView.getProjection();
				float accuracy = p.metersToEquatorPixels(lastFix.getAccuracy());
				Point loc = p.toPixels(myLocation, null);
				Paint paint = new Paint();
				paint.setAntiAlias(true);
				// paint.setColor(Color.BLUE);
				paint.setColor(Color.parseColor(getApplicationContext()
						.getResources().getString(R.color.jpappcolor)));

				if (accuracy > 10.0f) {
					paint.setAlpha(50);
					canvas.drawCircle(loc.x, loc.y, accuracy, paint);
					// border
					paint.setAlpha(200);
					paint.setStyle(Paint.Style.STROKE);
					canvas.drawCircle(loc.x, loc.y, accuracy, paint);
				}

				Bitmap bitmap = BitmapFactory.decodeResource(
						getApplicationContext().getResources(), R.drawable.me)
						.copy(Bitmap.Config.ARGB_8888, true);
				canvas.drawBitmap(bitmap, loc.x - (bitmap.getWidth() / 2),
						loc.y - bitmap.getHeight(), null);
			}
		};

		listOfOverlays.add(mMyLocationOverlay);

		mMyLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapView.getController().animateTo(
						mMyLocationOverlay.getMyLocation());
				// load with radius? Not for now.
			}
		});
	}

	@Override
	public void onResume() {
		// centeredOnMe = false;
		mMyLocationOverlay.enableMyLocation();
		super.onResume();
	}

	@Override
	public void onPause() {
		mMyLocationOverlay.disableMyLocation();
		super.onPause();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onStopObjectTap(SmartCheckStop stopObject) {
		StopsInfoDialog stopInfoDialog = new StopsInfoDialog();
		Bundle args = new Bundle();
		args.putSerializable(StopsInfoDialog.ARG_STOP, stopObject);
		stopInfoDialog.setArguments(args);
		stopInfoDialog.show(getSupportFragmentManager(), "stopselected");
	}

	@Override
	public void onStopObjectsTap(List<SmartCheckStop> stopObjectsList) {
		StopsInfoDialog stopInfoDialog = new StopsInfoDialog();
		Bundle args = new Bundle();
		args.putSerializable(StopsInfoDialog.ARG_STOPS,
				(ArrayList<SmartCheckStop>) stopObjectsList);
		stopInfoDialog.setArguments(args);
		stopInfoDialog.show(getSupportFragmentManager(), "stopselected");
	}

	public SmartCheckStop getSelectedStop() {
		return selectedStop;
	}

	public void setSelectedStop(SmartCheckStop selectedStop) {
		this.selectedStop = selectedStop;
	}

	@Override
	public String getAppToken() {
		return Config.APP_TOKEN;
	}

	@Override
	public String getAuthToken() {
		return JPHelper.getAuthToken();
	}

	@Override
	public void onCenterChanged(GeoPoint center) {
		Log.i("where", "Center Long: " + center.getLongitudeE6() / 1e6
				+ " Lat: " + center.getLatitudeE6() / 1e6);
		final double[] location = { center.getLongitudeE6() / 1e6,
				center.getLatitudeE6() / 1e6 };
		final double diagonal = mapView.getDiagonalLenght();
		
		if (diagonal > diagonalold) {
			diagonalold = diagonal;
			if (active != null)
				active.cancel(true);
			setSupportProgressBarIndeterminateVisibility(true);
			active = new StopsAsyncTask(mItemizedoverlay, location, diagonal,
					mapView,this);
			active.execute();
		}

	}

	@Override
	public void onZoomChanged(GeoPoint center, double diagonalLenght) {
		Log.i("where", "DiagonalLenght: " + diagonalLenght / 1e6
				+ "\nCenter Long: " + center.getLongitudeE6() / 1e6 + " Lat: "
				+ center.getLatitudeE6());
		final double[] location = { center.getLongitudeE6() / 1e6,
				center.getLatitudeE6() / 1e6 };
		final double diagonal = diagonalLenght;
		if (diagonal > diagonalold) {
			diagonalold = diagonal;
			if (active != null)
				active.cancel(true);
			setSupportProgressBarIndeterminateVisibility(true);
			active = new StopsAsyncTask(mItemizedoverlay, location, diagonal,
					mapView,this);
			active.execute();
		}
	}

	@Override
	public void onStopLoadingFinished() {
		setSupportProgressBarIndeterminateVisibility(false);
	}


}
