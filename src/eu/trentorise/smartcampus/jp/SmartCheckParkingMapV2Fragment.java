package eu.trentorise.smartcampus.jp;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;


import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockMapFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
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
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.custom.map.ParkingsInfoDialog;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;
import eu.trentorise.smartcampus.jp.helper.processor.SmartCheckParkingMapProcessor;
import eu.trentorise.smartcampus.jp.model.LocatedObject;
import eu.trentorise.smartcampus.jp.model.ParkingSerial;
import eu.trentorise.smartcampus.jp.notifications.NotificationsSherlockFragmentJP;

public class SmartCheckParkingMapV2Fragment extends SherlockMapFragment implements OnCameraChangeListener, OnMarkerClickListener {

	protected static final String PARAM_AID = "parkingAgencyId";
	public final static String ARG_PARKING_FOCUSED = "parking_focused";
	public final static int REQUEST_CODE = 1986;

	private final static int FOCUSED_ZOOM = 18;

	private SherlockFragmentActivity mActivity;

	private String parkingAid;

	// private ArrayList<ParkingSerial> parkingsList;
	private ParkingSerial focusedParking;

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
			parkingAid = getArguments().getString(PARAM_AID);
		}

		if (getArguments() != null && getArguments().containsKey(ARG_PARKING_FOCUSED)) {
			focusedParking = (ParkingSerial) getArguments().getSerializable(ARG_PARKING_FOCUSED);
		}

		if (ParkingsHelper.getFocusedParking() != null && ParkingsHelper.getFocusedParking() != focusedParking) {
			focusedParking = ParkingsHelper.getFocusedParking();
			ParkingsHelper.setFocusedParking(null);
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

		if (focusedParking != null) {
			zoomLevel--;
			getSupportMap().moveCamera(
					CameraUpdateFactory.newLatLngZoom(new LatLng(focusedParking.location()[0], focusedParking.location()[1]),
							FOCUSED_ZOOM));
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
	public void onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);
		menu.clear();
		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_list, 1,
				R.string.menu_item_parking_list);
		item.setIcon(R.drawable.map);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onPrepareOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_list) {
			goToParkingsList(null);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	private void goToParkingsList(ParkingSerial focus) {
		// Intent intent = new Intent(getSherlockActivity(),
		// ParkingMapActivity.class);
		//
		// ArrayList<ParkingSerial> spl = new ArrayList<ParkingSerial>();
		// for (int i = 0; i < adapter.getCount(); i++) {
		// spl.add(adapter.getItem(i));
		// }
		// intent.putExtra(ParkingMapActivity.ARG_PARKINGS, spl);
		//
		// if (focus != null) {
		// ParkingsHelper.setFocusedParking(focus);
		// }
		//
		// getSherlockActivity().getSupportActionBar().setSelectedNavigationItem(1);

		// if (focus != null) {
		// intent.putExtra(ParkingMapActivity.ARG_PARKING_FOCUSED, focus);
		// }

		// startActivity(intent);
		FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
		SmartCheckParkingsFragment fragment = new SmartCheckParkingsFragment();
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.replace(this.getId(), fragment, "parkings");
		fragmentTransaction.addToBackStack(fragment.getTag());
		fragmentTransaction.commit();
	}
	@Override
	public void onCameraChange(CameraPosition position) {
		if (zoomLevel != position.zoom) {
			zoomLevel = position.zoom;
		}

		if (ParkingsHelper.getParkingsCache().isEmpty()) {
			new SCAsyncTask<Void, Void, List<ParkingSerial>>(mActivity, new SmartCheckParkingMapProcessor(mActivity,
					getSupportMap(), parkingAid)).execute();
		} else {
			getSupportMap().clear();
			MapManager.ClusteringHelper.render(getSupportMap(),
					MapManager.ClusteringHelper.cluster(mActivity, getSupportMap(), ParkingsHelper.getParkingsCache()));
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
			ParkingsInfoDialog parkingsInfoDialog = new ParkingsInfoDialog();
			Bundle args = new Bundle();
			args.putSerializable(ParkingsInfoDialog.ARG_PARKINGS, (ArrayList) list);
			parkingsInfoDialog.setArguments(args);
			parkingsInfoDialog.show(mActivity.getSupportFragmentManager(), "parking_selected");
		} else if (list.size() > 1) {
			// getSupportMap().animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),
			// zoomLevel + 1));
			MapManager.fitMapWithOverlays(list, getSupportMap());
		} else {
			ParkingSerial parking = (ParkingSerial) list.get(0);
			ParkingsInfoDialog parkingsInfoDialog = new ParkingsInfoDialog();
			Bundle args = new Bundle();
			args.putSerializable(ParkingsInfoDialog.ARG_PARKING, parking);
			parkingsInfoDialog.setArguments(args);
			parkingsInfoDialog.show(mActivity.getSupportFragmentManager(), "parking_selected");
		}
		// // default behavior
		// return false;
		return true;
	}

	private GoogleMap getSupportMap() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(this.getId())).getMap();
		}
		return mMap;
	}

}
