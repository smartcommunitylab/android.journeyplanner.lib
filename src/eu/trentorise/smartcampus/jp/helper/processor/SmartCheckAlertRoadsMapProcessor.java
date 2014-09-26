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
package eu.trentorise.smartcampus.jp.helper.processor;

import it.sayservice.platform.smartplanner.data.message.RoadElement;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.GoogleMap;

import eu.trentorise.smartcampus.jp.LegMapActivity;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.helper.AlertRoadsHelper;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;



public class SmartCheckAlertRoadsMapProcessor extends AsyncTask<Object, List<AlertRoadLoc>, List<AlertRoadLoc>> {

//	public interface OnAlertLoadingFinished {
//		public void onAlertLoadingFinished(boolean result, double[] location, double diagonal);
//	}

	private final String TAG = "SmartCheckAlertRoadsMapProcessor";

	private SherlockFragmentActivity mActivity;

//	private OnAlertLoadingFinished mOnAlertLoadingFinished;

	private String[] selectedAgencyIds;
	private double[] location;
	private boolean zoomLevelChanged;
	private double diagonal;
	private GoogleMap map;
	private Context ctx;
	private String agencyId;
	private Long fromTime;
	private Long period;
	private String cacheToUpdate;
	private boolean filterByProjection;
	private long time;

	List<AlertRoadLoc> alert = new ArrayList<AlertRoadLoc>();

	public SmartCheckAlertRoadsMapProcessor(SherlockFragmentActivity activity, GoogleMap map, String agencyId,
			Long fromTime, Long period, String cacheToUpdate, boolean filterByProjection) {
		super();
		this.mActivity = activity;
		this.map = map;
		this.agencyId = agencyId;
		this.fromTime = fromTime;
		this.period = period;
		this.cacheToUpdate = cacheToUpdate;
		this.filterByProjection = filterByProjection;
	}

	@Override
	protected void onPreExecute() {
		mActivity.setSupportProgressBarIndeterminateVisibility(true);
		super.onPreExecute();
	}

	@Override
	protected List<AlertRoadLoc> doInBackground(Object... params) {
		try {

			long start = fromTime != null ? fromTime : System.currentTimeMillis();
			long end = period != null ? (start + period) : (start + (1000 * 60 * 60 * 24));
			alert= JPHelper.getAlertRoads(agencyId, start, end,JPHelper.getAuthToken(mActivity));
			return alert;
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.toString());
			return null;
		}
	}

	@Override
	protected void onPostExecute(List<AlertRoadLoc> result) {
		super.onPostExecute(result);

		if (cacheToUpdate != null) {
		// force cache update
		AlertRoadsHelper.setCache(cacheToUpdate, result);
	}

	if (filterByProjection) {
		result = LegMapActivity.filterByProjection(map, result);
	}
//	AlertRoad ar = new AlertRoad();
//	ar.setFrom(1411746519000L);
//	ar.setFrom(2411746519000L);
//	AlertRoadLoc arl = new AlertRoadLoc(ar);
//	arl.setRoad(new RoadElement()).location(new double[]{45.887207,11.038882});
//	result.add(arl);
	MapManager.ClusteringHelper.render(map, MapManager.ClusteringHelper.cluster(mActivity, map, result));
//	if (mOnAlertLoadingFinished != null) {
//		OnAlertLoadingFinished.onAlertLoadingFinished(result, location, diagonal);
//	} else {
		mActivity.setProgressBarIndeterminateVisibility(false);
//	}
	}

	@Override
	protected void onCancelled() {
		mActivity.setSupportProgressBarIndeterminateVisibility(false);
		super.onCancelled();
	}
}
