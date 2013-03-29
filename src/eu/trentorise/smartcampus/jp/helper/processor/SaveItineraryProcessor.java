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

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.MyItinerariesFragment;
import eu.trentorise.smartcampus.jp.MyRecurItineraryFragment;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.SavedJourneyActivity;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.custom.data.BasicItinerary;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SaveItineraryProcessor extends AbstractAsyncTaskProcessor<BasicItinerary, Void> {

	public SaveItineraryProcessor(SherlockFragmentActivity activity) {
		super(activity);
	}

	@Override
	public Void performAction(BasicItinerary... array) throws SecurityException, Exception {
		JPHelper.saveItinerary(array[0]);
		return null;
	}

	@Override
	public void handleResult(Void result) {
		activity.finish();
		/*call activity with list of one off itinerary*/
		Intent intent = new Intent(activity, SavedJourneyActivity.class);
		Bundle b = new Bundle();
		intent.putExtra(SavedJourneyActivity.PARAMS, MyItinerariesFragment.class.getName());
		activity.startActivity(intent);
		Toast toast = Toast.makeText(activity, R.string.journey_saved_alert, Toast.LENGTH_SHORT);
		toast.show();
	}

}
