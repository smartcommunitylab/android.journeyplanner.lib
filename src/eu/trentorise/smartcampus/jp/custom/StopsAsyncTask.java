package eu.trentorise.smartcampus.jp.custom;

import java.util.ArrayList;
import java.util.Collection;

import android.os.AsyncTask;

import com.google.android.maps.MapView;

import eu.trentorise.smartcampus.jp.custom.map.StopsItemizedOverlay;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

public class StopsAsyncTask extends AsyncTask<Object, SmartCheckStop, Boolean> {

	public interface OnStopLoadingFinished {
		public void onStopLoadingFinished();
	}

	private OnStopLoadingFinished mOnStopLoadingFinished;

	private Collection<SmartCheckStop> list;
	private StopsItemizedOverlay overlay;
	private double diagonal;
	private double[] location;
	private MapView mapView;
	private StopsItemizedOverlay old_overlay;

	public StopsAsyncTask(StopsItemizedOverlay overlay, double[] location,
			double diagonal, MapView mapView, OnStopLoadingFinished listener) {
		super();
		this.overlay = overlay;
		this.mapView = mapView;
		this.diagonal = diagonal;
		this.location = location;
		this.mOnStopLoadingFinished = listener;
		this.list = new ArrayList<SmartCheckStop>();
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
			list.addAll(JPHelper.getStops(RoutesHelper.AGENCYID_BUS + "",
					location, diagonal));
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected void onProgressUpdate(SmartCheckStop... values) {
		super.onProgressUpdate(values);
		//overlay.addOverlay(values[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (result) {
			overlay.clearMarkers();
			for (SmartCheckStop o : list) {
				overlay.addOverlay(o);
			}
			
			overlay.populateAll();
			mapView.invalidate();
		}
		else
			overlay=old_overlay;
		mOnStopLoadingFinished.onStopLoadingFinished();
	}

}