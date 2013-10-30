package eu.trentorise.smartcampus.jp.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

//import com.google.android.gms.maps.GoogleMap;

public class StopsV2AsyncTask extends AsyncTask<Object, SmartCheckStop, Boolean> {

	public interface OnStopLoadingFinished {
		public void onStopLoadingFinished(boolean result, double[] location, double diagonal);
	}

	private final String TAG = "StopsV2AsyncTask";

	private SherlockFragmentActivity mActivity;

	private OnStopLoadingFinished mOnStopLoadingFinished;

	private String[] selectedAgencyIds;
	private double[] location;
	private boolean zoomLevelChanged;
	private double diagonal;
	private GoogleMap map;
	private Context ctx;

	private long time;

	List<SmartCheckStop> stops = new ArrayList<SmartCheckStop>();

	public StopsV2AsyncTask(SherlockFragmentActivity mActivity, String[] selectedAgencyIds, LatLng latLng, double diagonal,
			GoogleMap map, boolean zoomLevelChanged, OnStopLoadingFinished listener,Context ctx) {
		super();
		this.mActivity = mActivity;
		this.selectedAgencyIds = selectedAgencyIds;
		if (latLng != null) {
			this.location = new double[] { latLng.latitude, latLng.longitude };
		} else {
			List<Double> center = JPParamsHelper.getCenterMap();
			if (center != null) {
				this.location = new double[] {center.get(0),center.get(1)};
			}

		}

		this.diagonal = diagonal;
		this.map = map;
		this.zoomLevelChanged = zoomLevelChanged;
		this.mOnStopLoadingFinished = listener;
		this.ctx=ctx;
	}

	@Override
	protected void onPreExecute() {
		mActivity.setSupportProgressBarIndeterminateVisibility(true);
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		time = System.currentTimeMillis();
		try {
//			for (int i = 0; i < selectedAgencyIds.length; i++) {
//				stops.addAll(JPHelper.getStops(selectedAgencyIds[i], location,
//						diagonal,JPHelper.getAuthToken(ctx)));
//			}
			stops.addAll(JPHelper.getStops(selectedAgencyIds, location, diagonal,JPHelper.getAuthToken(ctx)));
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.toString());
			return false;
		}
		long newtime = System.currentTimeMillis();
		time = newtime;
		return !isCancelled();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		int newStops = 0;

		for (SmartCheckStop stop : stops) {
			if (isCancelled()) {
				Log.e(TAG, "loader cancelled");
				break;
			} else {
				boolean added = MapManager.getCache().addStop(stop);
				if (added) {
					newStops++;
				}
			}
		}

		Collection<SmartCheckStop> stops = MapManager.getCache().getStopsByAgencyIds(selectedAgencyIds);
		if (!stops.isEmpty() || newStops > 0 || zoomLevelChanged) {
			map.clear();
			long newtime = System.currentTimeMillis();
			time = newtime;
			List<MarkerOptions> cluster = MapManager.ClusteringHelper.cluster(mActivity.getApplicationContext(), map, stops);
			newtime = System.currentTimeMillis();
			time = newtime;

			newtime = System.currentTimeMillis();
			time = newtime;
			MapManager.ClusteringHelper.render(map, cluster);
			newtime = System.currentTimeMillis();
			time = newtime;
		}

		if (mOnStopLoadingFinished != null) {
			mOnStopLoadingFinished.onStopLoadingFinished(result, location, diagonal);
		}

		//mActivity.setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	protected void onCancelled() {
		mActivity.setSupportProgressBarIndeterminateVisibility(false);
		super.onCancelled();
	}
}