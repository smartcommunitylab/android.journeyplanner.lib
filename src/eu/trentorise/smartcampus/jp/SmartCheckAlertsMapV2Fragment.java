package eu.trentorise.smartcampus.jp;

import java.util.ArrayList;
import java.util.List;

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

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.custom.map.AlertRoadsInfoDialog;
import eu.trentorise.smartcampus.jp.custom.map.AlertRoadsInfoDialog.OnDetailsClick;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.helper.AlertRoadsHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.processor.SmartCheckAlertRoadsMapProcessor;
import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;
import eu.trentorise.smartcampus.jp.model.LocatedObject;

public class SmartCheckAlertsMapV2Fragment extends SupportMapFragment implements OnCameraChangeListener, OnMarkerClickListener,
		OnDetailsClick {

	protected static final String PARAM_AID = "alertsAgencyId";
	public final static String ARG_ALERT_FOCUSED = "alert_focused";
	public final static int REQUEST_CODE = 1986;

	private final static int FOCUSED_ZOOM = 18;

	private SherlockFragmentActivity mActivity;

	private String agencyId;

	private AlertRoadLoc focusedAlert;

	private LatLng centerLatLng = new LatLng(JPParamsHelper.getCenterMap().get(0), JPParamsHelper.getCenterMap().get(1));
	private float zoomLevel = JPParamsHelper.getZoomLevelMap() - 3;

	private GoogleMap mMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (SherlockFragmentActivity) getActivity();
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

		// get arguments
		if (getArguments() != null && getArguments().containsKey(PARAM_AID)) {
			agencyId = getArguments().getString(PARAM_AID);
		}

		if (getArguments() != null && getArguments().containsKey(ARG_ALERT_FOCUSED)) {
			focusedAlert = (AlertRoadLoc) getArguments().getSerializable(ARG_ALERT_FOCUSED);
		}

		if (AlertRoadsHelper.getFocused() != null && AlertRoadsHelper.getFocused() != focusedAlert) {
			focusedAlert = AlertRoadsHelper.getFocused();
			AlertRoadsHelper.setFocused(null);
		}

		if (getSupportMap() == null)
			return;

		// features disabled waiting for a better clustering grid
		getSupportMap().getUiSettings().setRotateGesturesEnabled(false);
		getSupportMap().getUiSettings().setTiltGesturesEnabled(false);
		getSupportMap().setOnCameraChangeListener(this);
		getSupportMap().setOnMarkerClickListener(this);

		// show my location
		getSupportMap().setMyLocationEnabled(true);

		if (focusedAlert != null) {
			zoomLevel--;
			getSupportMap().moveCamera(
					CameraUpdateFactory.newLatLngZoom(
							new LatLng(Double.parseDouble(focusedAlert.getRoad().getLat()), Double.parseDouble(focusedAlert
									.getRoad().getLon())), FOCUSED_ZOOM));
		} else {
			getSupportMap().moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, zoomLevel));

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
		if (zoomLevel != position.zoom) {
			zoomLevel = position.zoom;
		}

		if (AlertRoadsHelper.getCache().isEmpty()) {
			new SCAsyncTask<Void, Void, List<AlertRoadLoc>>(mActivity, new SmartCheckAlertRoadsMapProcessor(mActivity,
					getSupportMap(), agencyId)).execute();
		} else {
			getSupportMap().clear();
			MapManager.ClusteringHelper.render(getSupportMap(),
					MapManager.ClusteringHelper.cluster(mActivity, getSupportMap(), AlertRoadsHelper.getCache()));
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		String id = marker.getTitle();

		List<LocatedObject> list = MapManager.ClusteringHelper.getFromGridId(id);

		if (list == null || list.isEmpty()) {
			return true;
		}

		if (list.size() > 1 && getSupportMap().getCameraPosition().zoom == getSupportMap().getMaxZoomLevel()) {
			AlertRoadsInfoDialog infoDialog = new AlertRoadsInfoDialog(this);
			Bundle args = new Bundle();
			args.putSerializable(AlertRoadsInfoDialog.ARG_ALERTSLIST, (ArrayList) list);
			infoDialog.setArguments(args);
			infoDialog.show(mActivity.getSupportFragmentManager(), "alert_selected");
		} else if (list.size() > 1) {
			getSupportMap().animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), zoomLevel + 1));
			MapManager.fitMapWithOverlays(list, getSupportMap());
		} else {
			AlertRoadLoc alert = (AlertRoadLoc) list.get(0);
			AlertRoadsInfoDialog infoDialog = new AlertRoadsInfoDialog(this);
			Bundle args = new Bundle();
			args.putSerializable(AlertRoadsInfoDialog.ARG_ALERT, alert);
			infoDialog.setArguments(args);
			infoDialog.show(mActivity.getSupportFragmentManager(), "alert_selected");
		}
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
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.addToBackStack(fragment.getTag());
		fragmentTransaction.replace(Config.mainlayout, fragment, "map");
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
