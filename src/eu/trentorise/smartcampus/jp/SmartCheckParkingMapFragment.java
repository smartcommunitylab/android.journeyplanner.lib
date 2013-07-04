package eu.trentorise.smartcampus.jp;

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.custom.BetterMapView;
import eu.trentorise.smartcampus.jp.custom.BetterMapView.OnMapChanged;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.custom.map.ParkingObjectMapItemTapListener;
import eu.trentorise.smartcampus.jp.custom.map.ParkingsInfoDialog;
import eu.trentorise.smartcampus.jp.custom.map.ParkingsItemizedOverlay;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;
import eu.trentorise.smartcampus.jp.helper.processor.SmartCheckParkingMapProcessor;
import eu.trentorise.smartcampus.jp.model.ParkingSerial;

public class SmartCheckParkingMapFragment extends FeedbackFragment implements ParkingObjectMapItemTapListener, OnMapChanged {

	protected static final String PARAM_AID = "parkingAgencyId";
	public final static String ARG_PARKING_FOCUSED = "parking_focused";
	public final static int REQUEST_CODE = 1986;

	private final static int FOCUSED_ZOOM = 18;

	protected ViewGroup mapContainer;
	protected BetterMapView mapView;
	MyLocationOverlay mMyLocationOverlay = null;
	ParkingsItemizedOverlay mItemizedoverlay = null;

	private String parkingAid;

	// private ArrayList<ParkingSerial> parkingsList;
	private ParkingSerial focusedParking;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapContainer = new RelativeLayout(getActivity());

		mapView = MapManager.getBetterMapView();
		mapView.setOnMapChanged(this);
		
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);

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

		final ViewGroup parent = (ViewGroup) mapView.getParent();
		if (parent != null) {
			parent.removeView(mapView);
		}
		mapContainer.addView(mapView);

		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		mItemizedoverlay = new ParkingsItemizedOverlay(getSherlockActivity(), mapView);
		mItemizedoverlay.setMapItemTapListener(this);
		listOfOverlays.add(mItemizedoverlay);

		mMyLocationOverlay = new MyLocationOverlay(getSherlockActivity(), mapView) {
			@Override
			protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
				Projection p = mapView.getProjection();
				float accuracy = p.metersToEquatorPixels(lastFix.getAccuracy());
				Point loc = p.toPixels(myLocation, null);
				Paint paint = new Paint();
				paint.setAntiAlias(true);
				// paint.setColor(Color.BLUE);
				paint.setColor(Color.parseColor(getSherlockActivity().getResources().getString(R.color.jpappcolor)));

				if (accuracy > 10.0f) {
					paint.setAlpha(50);
					canvas.drawCircle(loc.x, loc.y, accuracy, paint);
					// border
					paint.setAlpha(200);
					paint.setStyle(Paint.Style.STROKE);
					canvas.drawCircle(loc.x, loc.y, accuracy, paint);
				}

				Bitmap bitmap = BitmapFactory.decodeResource(getSherlockActivity().getResources(), R.drawable.me).copy(
						Bitmap.Config.ARGB_8888, true);
				canvas.drawBitmap(bitmap, loc.x - (bitmap.getWidth() / 2), loc.y - bitmap.getHeight(), null);
			}
		};
		mMyLocationOverlay.enableMyLocation();
		listOfOverlays.add(mMyLocationOverlay);

		mapView.getController().setZoom(JPParamsHelper.getZoomLevelMap() + 2);

		// LOAD
		if (ParkingsHelper.getParkingsCache().isEmpty()) {
//			new SCAsyncTask<Void, Void, List<ParkingSerial>>(getSherlockActivity(), new SmartCheckParkingMapProcessor(
//					getSherlockActivity(), mapView, mItemizedoverlay, parkingAid)).execute();
		} else {
			mItemizedoverlay.clearMarkers();
			mItemizedoverlay.addAllOverlays(ParkingsHelper.getParkingsCache());
			mItemizedoverlay.populateAll();
			mapView.postInvalidate();
		}

		if (focusedParking == null) {
			// move map to my location at first fix
			mMyLocationOverlay.runOnFirstFix(new Runnable() {
				public void run() {
					if (mapView != null && mapView.getController() != null && mMyLocationOverlay.getMyLocation() != null) {
						mapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
						// load with radius? Not for now.
					}
				}
			});
		} else {
			GeoPoint focus = new GeoPoint((int) (focusedParking.getPosition()[0] * 1E6),
					(int) (focusedParking.getPosition()[1] * 1E6));
			mapView.getController().animateTo(focus);
			mapView.getController().setZoom(FOCUSED_ZOOM);
		}

		return mapContainer;
	}

	@Override
	public void onStart() {
		super.onStart();

		// ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true); // back arrow
		// actionBar.setDisplayUseLogoEnabled(false); // system logo
		// actionBar.setDisplayShowTitleEnabled(true); // system title
		// actionBar.setDisplayShowHomeEnabled(false); // home icon bar
		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		// tabs

		// hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mapView.getWindowToken(), 0);
	}

	@Override
	public void onPause() {
		super.onPause();
//
//		final ViewGroup parent = (ViewGroup) mapView.getParent();
//		if (parent != null) {
//			parent.removeView(mapView);
//		}
	}

	@Override
	public void onParkingObjectTap(ParkingSerial parking) {
		ParkingsInfoDialog parkingsInfoDialog = new ParkingsInfoDialog();
		Bundle args = new Bundle();
		args.putSerializable(ParkingsInfoDialog.ARG_PARKING, parking);
		parkingsInfoDialog.setArguments(args);
		parkingsInfoDialog.show(getSherlockActivity().getSupportFragmentManager(), "parking_selected");
	}

	@Override
	public void onParkingObjectsTap(List<ParkingSerial> parkingsList) {
		ParkingsInfoDialog parkingsInfoDialog = new ParkingsInfoDialog();
		Bundle args = new Bundle();
		args.putSerializable(ParkingsInfoDialog.ARG_PARKINGS, (Serializable) parkingsList);
		parkingsInfoDialog.setArguments(args);
		parkingsInfoDialog.show(getSherlockActivity().getSupportFragmentManager(), "parking_selected");
	}

	@Override
	public void onCenterChanged(GeoPoint center) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onZoomChanged(GeoPoint center, double diagonal) {
		// TODO Auto-generated method stub
	}

}
