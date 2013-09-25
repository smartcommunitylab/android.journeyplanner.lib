package eu.trentorise.smartcampus.jp;

import java.util.Arrays;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.custom.map.AlertRoadsInfoDialog.OnDetailsClick;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.helper.AlertRoadsHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.processor.SmartCheckAlertRoadsMapProcessor;
import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;

public class SmartCheckAlertsMapV2Fragment extends SupportMapFragment implements OnCameraChangeListener, OnMarkerClickListener,
		OnDetailsClick {

	protected static final String PARAM_AID = "alertsAgencyId";
	public final static String ARG_ALERT_FOCUSED = "alert_focused";
	public final static int REQUEST_CODE = 1986;

	private final static int ZOOM_FOCUSED = 18;
	private final static int ZOOM_DEFAULT = JPParamsHelper.getZoomLevelMap() - 3;;

	private SherlockFragmentActivity mActivity;

	private String agencyId;

	private AlertRoadLoc focusedAlert;

	private LatLng centerLatLng = new LatLng(JPParamsHelper.getCenterMap().get(0), JPParamsHelper.getCenterMap().get(1));

	private GoogleMap mMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (SherlockFragmentActivity) getActivity();

		// get arguments
		if (getArguments() != null && getArguments().containsKey(PARAM_AID)) {
			agencyId = getArguments().getString(PARAM_AID);
		}

		setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		FeedbackFragmentInflater.inflateHandleButton(getActivity(), getView());
	}

	@Override
	public void onResume() {
		super.onResume();

		if (getSupportMap() == null)
			return;

		// features disabled waiting for a better clustering grid
		getSupportMap().getUiSettings().setRotateGesturesEnabled(false);
		getSupportMap().getUiSettings().setTiltGesturesEnabled(false);
		getSupportMap().setOnCameraChangeListener(this);
		getSupportMap().setOnMarkerClickListener(this);

		// show my location
		getSupportMap().setMyLocationEnabled(true);

		if (getArguments() != null && getArguments().containsKey(ARG_ALERT_FOCUSED)) {
			focusedAlert = (AlertRoadLoc) getArguments().getSerializable(ARG_ALERT_FOCUSED);
		}

		if (AlertRoadsHelper.getFocused() != null && AlertRoadsHelper.getFocused() != focusedAlert) {
			focusedAlert = AlertRoadsHelper.getFocused();
			AlertRoadsHelper.setFocused(null);
		}

		if (focusedAlert != null) {
			getSupportMap().moveCamera(
					CameraUpdateFactory.newLatLngZoom(
							new LatLng(Double.parseDouble(focusedAlert.getRoad().getLat()), Double.parseDouble(focusedAlert
									.getRoad().getLon())), ZOOM_FOCUSED));
		} else {
			getSupportMap().moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, ZOOM_DEFAULT));

			// // move to my location
			// if (JPHelper.getLocationHelper().getLocation() != null) {
			// centerLatLng = new
			// LatLng(JPHelper.getLocationHelper().getLocation().getLatitudeE6()
			// / 1e6, JPHelper
			// .getLocationHelper().getLocation().getLongitudeE6() / 1e6);
			//
			// getSupportMap().moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng,
			// zoomLevel));
			// } else {
			// getSupportMap().moveCamera(CameraUpdateFactory.zoomTo(zoomLevel));
			// }
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getSupportMap() != null) {
			getSupportMap().setMyLocationEnabled(false);
			getSupportMap().setOnCameraChangeListener(null);
			getSupportMap().setOnMarkerClickListener(null);
		}
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		// if (zoomLevel != position.zoom) {
		// zoomLevel = position.zoom;
		// }

		if (AlertRoadsHelper.getCache(AlertRoadsHelper.ALERTS_CACHE_SMARTCHECK) == null
				|| AlertRoadsHelper.getCache(AlertRoadsHelper.ALERTS_CACHE_SMARTCHECK).isEmpty()) {
			new SmartCheckAlertRoadsMapProcessor(mActivity, getSupportMap(), agencyId, null, null, AlertRoadsHelper.ALERTS_CACHE_SMARTCHECK, false).execute();
		} else {
			getSupportMap().clear();

			if (focusedAlert != null) {
				MapManager.ClusteringHelper.render(getSupportMap(),
						MapManager.ClusteringHelper.cluster(mActivity, getSupportMap(), Arrays.asList(focusedAlert)));
				focusedAlert = null;
			} else {
				MapManager.ClusteringHelper.render(
						getSupportMap(),
						MapManager.ClusteringHelper.cluster(mActivity, getSupportMap(),
								AlertRoadsHelper.getCache(AlertRoadsHelper.ALERTS_CACHE_SMARTCHECK)));
			}
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		AlertRoadsHelper.staticOnMarkerClick(mActivity, getSupportMap(), marker, this);
		// // default behavior
		// return false;
		return true;
	}

	@Override
	public void OnDialogDetailsClick(AlertRoadLoc alert) {
		FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
		Fragment fragment = new SmartCheckAlertDetailsFragment();
		Bundle args = new Bundle();
		args.putSerializable(SmartCheckAlertDetailsFragment.ARG_ALERT, alert);
		fragment.setArguments(args);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.hide(this);
		fragmentTransaction.add(Config.mainlayout, fragment, "map");
		fragmentTransaction.addToBackStack(fragment.getTag());
		// fragmentTransaction.commitAllowingStateLoss();
		fragmentTransaction.commit();
	}

	private GoogleMap getSupportMap() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(Config.mainlayout)).getMap();
		}
		return mMap;
	}

}
