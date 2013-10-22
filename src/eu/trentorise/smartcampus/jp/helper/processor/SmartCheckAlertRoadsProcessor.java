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

import java.util.List;

import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.helper.AlertRoadsHelper;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SmartCheckAlertRoadsProcessor extends AbstractAsyncTaskProcessor<Void, List<AlertRoadLoc>> {

	private SherlockFragmentActivity mActivity;
	private ArrayAdapter<AlertRoadLoc> adapter;
	private String agencyId;

	public SmartCheckAlertRoadsProcessor(SherlockFragmentActivity activity, ArrayAdapter<AlertRoadLoc> adapter, String agencyId) {
		super(activity);
		this.mActivity = activity;
		this.adapter = adapter;
		this.agencyId = agencyId;
	}

	@Override
	public List<AlertRoadLoc> performAction(Void... params) throws SecurityException, Exception {
		long now = System.currentTimeMillis();
		return JPHelper.getAlertRoads(agencyId, now, now + (1000 * 60 * 60 * 24),JPHelper.getAuthToken(mActivity));
	}

	@Override
	public void handleResult(List<AlertRoadLoc> result) {
		// Collections.sort(result, AlertRoadsHelper.getStreetNameComparator());

		adapter.clear();
		for (AlertRoadLoc parking : result) {
			adapter.add(parking);
		}

		adapter.notifyDataSetChanged();

		// save in cache
		AlertRoadsHelper.setCache(AlertRoadsHelper.ALERTS_CACHE_SMARTCHECK, result);

		mActivity.setSupportProgressBarIndeterminateVisibility(false);
	}

}
