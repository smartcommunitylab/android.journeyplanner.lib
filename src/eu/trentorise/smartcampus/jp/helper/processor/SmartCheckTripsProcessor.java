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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;
import eu.trentorise.smartcampus.mobilityservice.model.TripData;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SmartCheckTripsProcessor extends AbstractAsyncTaskProcessor<SmartCheckStop, List<TripData>> {

	private ArrayAdapter<TripData> adapter;
	private Context ctx;

	public SmartCheckTripsProcessor(SherlockFragmentActivity activity, ArrayAdapter<TripData> adapter) {
		super(activity);
		this.adapter = adapter;
		ctx= activity.getApplicationContext();
	}

	@Override
	public List<TripData> performAction(SmartCheckStop... params) throws SecurityException, Exception {
		List<TripData> list = new ArrayList<TripData>();

		for (int i = 0; i < params.length; i++) {
			SmartCheckStop stop = params[i];
			if (stop != null) {
				list = JPHelper.getTrips(stop,JPHelper.getAuthToken(ctx));
				break;
			}
		}

		return list;
	}

	@Override
	public void handleResult(List<TripData> result) {
		adapter.clear();
		for (TripData tripData : result) {
			adapter.add(tripData);
		}
		adapter.notifyDataSetChanged();
	}

}
