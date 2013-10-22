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

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.ItineraryChoicesFragment;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class PlanNewJourneyProcessor extends AbstractAsyncTaskProcessor<SingleJourney, List<Itinerary>> {

	private SingleJourney singleJourney;
	private String mTag;
	private Context ctx;

	public PlanNewJourneyProcessor(SherlockFragmentActivity activity, SingleJourney singleJourney, String mTag) {
		super(activity);
		ctx=activity.getApplicationContext();
		this.singleJourney = singleJourney;
		this.mTag = mTag;
	}

	@Override
	public List<Itinerary> performAction(SingleJourney... array) throws SecurityException, Exception {
		return JPHelper.planSingleJourney(array[0],JPHelper.getAuthToken(ctx));
	}

	@Override
	public void handleResult(List<Itinerary> itineraries) {
		FragmentTransaction fragmentTransaction = ((SherlockFragmentActivity)activity).getSupportFragmentManager().beginTransaction();
		Fragment fragment = ItineraryChoicesFragment.newInstance(singleJourney, itineraries);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.replace(Config.mainlayout, fragment, mTag);
		fragmentTransaction.addToBackStack(fragment.getTag());
		fragmentTransaction.commit();
	}

}
