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

	private View itinerariesView;
	private ItinerariesListAdapter itinerariesAdapter = null;
	private View itinerariesPromotedView;
	private ItinerariesListAdapter itinerariesPromotedAdapter = null;
	private LinearLayout noItemsView = null;

	public PlanNewJourneyProcessor(Activity activity, View itinerariesView, ItinerariesListAdapter itinerariesAdapter,
			View itinerariesPromotedView, ItinerariesListAdapter itinerariesPromotedAdapter, LinearLayout noItemsView) {
		super(activity);
		this.itinerariesView = itinerariesView;
		this.itinerariesAdapter = itinerariesAdapter;
		this.itinerariesPromotedView = itinerariesPromotedView;
		this.itinerariesPromotedAdapter = itinerariesPromotedAdapter;
		this.noItemsView = noItemsView;
	}

	@Override
	public List<Itinerary> performAction(SingleJourney... array) throws SecurityException, Exception {
		return JPHelper.planSingleJourney(array[0], JPHelper.getAuthToken(activity));
	}

	@Override
	public void handleResult(List<Itinerary> itineraries) {
		itinerariesPromotedAdapter.clear();
		itinerariesAdapter.clear();

		for (int i = 0; i < itineraries.size(); i++) {
			Itinerary myt = itineraries.get(i);

			// TODO: ***** TEMP ***** Put in the right adapter!
			// if (i < 2) {
			// myt.setPromoted(true);
			// } else {
			// myt.setPromoted(false);
			// }
			// TODO: ***** TEMP ***** end

			if (myt.isPromoted()) {
				itinerariesPromotedAdapter.add(myt);
			} else {
				itinerariesAdapter.add(myt);
			}
		}

		itinerariesPromotedAdapter.notifyDataSetChanged();
		itinerariesAdapter.notifyDataSetChanged();

		if (itinerariesPromotedAdapter.isEmpty()) {
			itinerariesPromotedView.setVisibility(View.GONE);
		} else {
			itinerariesPromotedView.setVisibility(View.VISIBLE);
		}

		if (itinerariesAdapter.isEmpty()) {
			itinerariesView.setVisibility(View.GONE);
		} else {
			itinerariesView.setVisibility(View.VISIBLE);
		}

		if (itinerariesPromotedAdapter.isEmpty() && itinerariesAdapter.isEmpty()) {
			noItemsView.setVisibility(View.VISIBLE);
		} else {
			noItemsView.setVisibility(View.GONE);
		}
	}
}
