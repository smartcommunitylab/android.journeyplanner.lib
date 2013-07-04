package eu.trentorise.smartcampus.jp.custom;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import eu.trentorise.smartcampus.jp.custom.map.StopsItemizedOverlay;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

public class StopsAsyncTask extends AsyncTask<Object, SmartCheckStop, Boolean> {

	public interface OnStopLoadingFinished {
		public void onStopLoadingFinished(boolean result, double[] location, double diagonal);
	}

	private final String TAG = "StopsAsyncTask";
	private OnStopLoadingFinished mOnStopLoadingFinished;

	// private Collection<SmartCheckStop> list;
//	private Map<String, SmartCheckStop> smartCheckStopMap;
//	private StopsItemizedOverlay overlay;
	private double diagonal;
	private double[] location;
	private BetterMapView mapView;
	private String[] selectedAgencyIds;
//	private StopsItemizedOverlay old_overlay;

	public StopsAsyncTask( String[] selectedAgencyIds, StopsItemizedOverlay overlay, double[] location,
			double diagonal, BetterMapView mapView, OnStopLoadingFinished listener) {
		super();
//		this.overlay = overlay;
		this.mapView = mapView;
		this.diagonal = diagonal;
		this.location = location;
		this.mOnStopLoadingFinished = listener;
		// this.list = new ArrayList<SmartCheckStop>();
//		this.smartCheckStopMap = smartCheckStopMap;
		this.selectedAgencyIds = selectedAgencyIds;
	}

//	@Override
//	protected void onPreExecute() {
//		super.onPreExecute();
//		old_overlay = overlay;
//
//	}
//


	@Override
	protected Boolean doInBackground(Object... params) {
		// params[0]=location
		// params[1]=radius

		try {
			List<SmartCheckStop> stops = new ArrayList<SmartCheckStop>();
			if (selectedAgencyIds != null) {
				for (int i = 0; i < selectedAgencyIds.length; i++) {
					try {
						stops.addAll(JPHelper.getStops(selectedAgencyIds[i], location, diagonal));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					stops.addAll(JPHelper.getStops(null, location, diagonal));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (SmartCheckStop stop : stops) {
				if (isCancelled()) {
					Log.e(TAG, "doInbackground if cancelled==true ");
					break;
				}
				else if (mapView.getCache().addStop(stop)){
					getItemizedOverlay().addOverlay(stop);
				}
//				else if (!smartCheckStopMap.containsKey(stop.getId())) {
//					smartCheckStopMap.put(stop.getId(), stop);
//					// list.add(stop);
//					// publishProgress(stop);
//					getItemizedOverlay().addOverlay(stop);
//					Log.e(TAG, "add overlay " + stop);
//
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return !isCancelled();
	}

	private StopsItemizedOverlay getItemizedOverlay() {
		return (StopsItemizedOverlay)mapView.getOverlays().get(0);
	}



	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
//		if (!result) {
//			overlay = old_overlay;
//		}
		getItemizedOverlay().populateAll();
		mapView.postInvalidate();

		
//		String o1 = overlay.toString();
//		String o2 = ((StopsItemizedOverlay) getItemizedOverlay()).toString();
//		Log.e(TAG, "Is the same overlay? " + o2.equalsIgnoreCase(o1));

		if (mOnStopLoadingFinished != null) {
			mOnStopLoadingFinished.onStopLoadingFinished(result, location, diagonal);
		}
	}
}