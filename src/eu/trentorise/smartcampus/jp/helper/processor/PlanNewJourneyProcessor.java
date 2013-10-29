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

import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.journey.SingleJourney;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.custom.ItinerariesListAdapter;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class PlanNewJourneyProcessor extends AbstractAsyncTaskProcessor<SingleJourney, List<Itinerary>> {

	private ItinerariesListAdapter adapter = null;
	private LinearLayout mNoItems = null;
	

	public PlanNewJourneyProcessor(Activity activity, ItinerariesListAdapter adapter, LinearLayout mNoItems) {
		super(activity);
		this.adapter = adapter;
		this.mNoItems  = mNoItems;
	}

	@Override
	public List<Itinerary> performAction(SingleJourney... array) throws SecurityException, Exception {
		return JPHelper.planSingleJourney(array[0],JPHelper.getAuthToken(activity));
	}

	@Override
	public void handleResult(List<Itinerary> itineraries) {
		adapter.clear();
		for (Itinerary myt : itineraries) {
			adapter.add(myt);
		}
		adapter.notifyDataSetChanged();
		if ((itineraries==null)||(itineraries.size()==0))
		{
		//put "empty string"
		mNoItems.setVisibility(View.VISIBLE);
		}
	else {
		mNoItems.setVisibility(View.GONE);
	}
		
	}

}
