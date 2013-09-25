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

import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.GoogleMap;

import eu.trentorise.smartcampus.jp.LegMapActivity;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.helper.AlertRoadsHelper;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;

public class SmartCheckAlertRoadsMapProcessor extends AsyncTask<Void, Void, List<AlertRoadLoc>> {

	private SherlockFragmentActivity mActivity;
	private String agencyId;
	private GoogleMap map;
	private Long fromTime;
	private Long period;
	private String cacheToUpdate;
	private boolean filterByProjection;

	public SmartCheckAlertRoadsMapProcessor(SherlockFragmentActivity activity, GoogleMap map, String agencyId, Long fromTime,
			Long period, String cacheToUpdate, boolean filterByProjection) {
		this.mActivity = activity;
		this.map = map;
		this.agencyId = agencyId;
		this.fromTime = fromTime;
		this.period = period;
		this.cacheToUpdate = cacheToUpdate;
		this.filterByProjection = filterByProjection;
	}

	@Override
	protected void onCancelled() {
		mActivity.setSupportProgressBarIndeterminateVisibility(false);
		super.onCancelled();
	}

	@Override
	protected void onPreExecute() {
		mActivity.setSupportProgressBarIndeterminateVisibility(true);
		super.onPreExecute();
	}

	@Override
	protected List<AlertRoadLoc> doInBackground(Void... params) {
		long start = fromTime != null ? fromTime : System.currentTimeMillis();
		long end = period != null ? (start + period) : (start + (1000 * 60 * 60 * 24));
		try {
			return JPHelper.getAlertRoads(agencyId, start, end);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	@Override
	public void onPostExecute(List<AlertRoadLoc> result) {
		if (cacheToUpdate != null) {
			// force cache update
			AlertRoadsHelper.setCache(cacheToUpdate, result);
		}
		if (filterByProjection) {
			result = LegMapActivity.filterByProjection(map, result);
		}
//		map.clear();
		MapManager.ClusteringHelper
				.render(map, MapManager.ClusteringHelper.cluster(mActivity,
						map, result));
		mActivity.setSupportProgressBarIndeterminateVisibility(false);
	}

}
