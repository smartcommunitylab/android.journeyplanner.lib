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

import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SmartCheckAlertRoadsProcessor extends AbstractAsyncTaskProcessor<Void, List<AlertRoad>> {

	private SherlockFragmentActivity mActivity;
	private ArrayAdapter<AlertRoad> adapter;
	private String agencyId;

	private Comparator<AlertRoad> streetNameComparator = new Comparator<AlertRoad>() {
		public int compare(AlertRoad ar1, AlertRoad ar2) {
			return ar1.getRoad().getStreet().compareTo(ar2.getRoad().getStreet());
		}
	};

	public SmartCheckAlertRoadsProcessor(SherlockFragmentActivity activity, ArrayAdapter<AlertRoad> adapter, String agencyId) {
		super(activity);
		this.mActivity = activity;
		this.adapter = adapter;
		this.agencyId = agencyId;
	}

	@Override
	public List<AlertRoad> performAction(Void... params) throws SecurityException, Exception {
		return JPHelper.getAlertRoads(agencyId);
	}

	@Override
	public void handleResult(List<AlertRoad> result) {
		Collections.sort(result, streetNameComparator);

		adapter.clear();
		for (AlertRoad parking : result) {
			adapter.add(parking);
		}

		adapter.notifyDataSetChanged();

		// // save in cache
		// ParkingsHelper.setParkingsCache(orderedList);

		mActivity.setSupportProgressBarIndeterminateVisibility(false);
	}

}
