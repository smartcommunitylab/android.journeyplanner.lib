package eu.trentorise.smartcampus.jp.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;

import com.google.android.maps.MapView;

import eu.trentorise.smartcampus.jp.custom.map.StopsItemizedOverlay;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;
import eu.trentorise.smartcampus.jp.model.Square;

public class StopsAsyncTask extends AsyncTask<Object, SmartCheckStop, Boolean> {

	public interface OnStopLoadingFinished {
		public void onStopLoadingFinished(boolean result,double[] location,double diagonal);
	}

	private OnStopLoadingFinished mOnStopLoadingFinished;

	// private Collection<SmartCheckStop> list;
	private Map<String, SmartCheckStop> smartCheckStopMap;
	private StopsItemizedOverlay overlay;
	private double diagonal;
	private double[] location;
	private MapView mapView;
	private String[] selectedAgencyIds;
	private StopsItemizedOverlay old_overlay;
	private Square cache;

	public StopsAsyncTask(String[] selectedAgencyIds, Map<String, SmartCheckStop> smartCheckStopMap,
			StopsItemizedOverlay overlay,Square cache, double[] location, double diagonal, MapView mapView, OnStopLoadingFinished listener) {
		super();
		this.overlay = overlay;
		this.mapView = mapView;
		this.diagonal = diagonal;
		this.location = location;
		this.mOnStopLoadingFinished = listener;
		// this.list = new ArrayList<SmartCheckStop>();
		this.smartCheckStopMap = smartCheckStopMap;

		this.selectedAgencyIds = selectedAgencyIds;
		this.cache=cache;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		old_overlay = overlay;

	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		this.onPostExecute(false);
	}

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
				if (!smartCheckStopMap.containsKey(stop.getId())) {
					smartCheckStopMap.put(stop.getId(), stop);
					// list.add(stop);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected void onProgressUpdate(SmartCheckStop... values) {
		super.onProgressUpdate(values);
		// overlay.addOverlay(values[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (result) {
			//overlay.clearMarkers();
			// for (SmartCheckStop o : list) {
			Square s;
			for (SmartCheckStop o : smartCheckStopMap.values()) {
				s = new Square(o.getLocation(),diagonal);
				if(cache==null ||overlay.size()<=0 || cache.compareTo(s)<0)
					overlay.addOverlay(o);
				s=null;
			}
			overlay.populateAll();
			mapView.invalidate();
			// list.clear();
		} else {
			overlay = old_overlay;
		}

		mOnStopLoadingFinished.onStopLoadingFinished(result,location,diagonal);
	}

}