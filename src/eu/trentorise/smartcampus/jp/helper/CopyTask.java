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
package eu.trentorise.smartcampus.jp.helper;

import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.mobilityservice.MobilityUserService;
import eu.trentorise.smartcampus.mobilityservice.model.BasicItinerary;
import eu.trentorise.smartcampus.mobilityservice.model.BasicRecurrentJourney;

/*
 * Task used for copying the data during the user's upgrading
 * */
public class CopyTask extends AsyncTask<Object, Void, String> {
	private Activity activity = null;
	SharedPreferences sharedPref = null;
	MobilityUserService userService = null;
	int resultCode;
	Intent data = null;
	private OnTaskCompleted listener;

	public CopyTask(SharedPreferences sharedPref2, MobilityUserService userService, int resultCode, Intent data,
			Activity activity, OnTaskCompleted listener) {
		this.sharedPref = sharedPref2;
		this.userService = userService;
		this.resultCode = resultCode;
		this.data = data;
		this.activity = activity;
		this.listener = listener;
	}

	@Override
	protected String doInBackground(Object... params) {
		{
			try {

				String mToken = null;
				if (resultCode == activity.RESULT_OK) {
					mToken = data.getExtras().getString(AccountManager.KEY_AUTHTOKEN);
				}
				// check if the user is logged
				if (mToken != null) {

					// check if upgrade is already done
					if (userService.getRecurrentJourneys(mToken) == null
							&& userService.getSingleJourneys(mToken) == null) {
						// copy old itinerariesin the new one
						List<BasicItinerary> listMyItinerary = Utils.convertJSONToObjects(
								sharedPref.getString(JPHelper.MY_ITINERARIES, null), BasicItinerary.class);
						List<BasicRecurrentJourney> listMyRecurrentJourneys = Utils.convertJSONToObjects(
								sharedPref.getString(JPHelper.MY_RECURRENTJOURNEYS, null), BasicRecurrentJourney.class);

						for (BasicItinerary itinerary : listMyItinerary) {
							itinerary.setClientId(null);
							userService.saveSingleJourney(itinerary, mToken);

						}
						for (BasicRecurrentJourney itinerary : listMyRecurrentJourneys) {
							itinerary.setClientId(null);
							userService.saveRecurrentJourney(itinerary, mToken);
						}
						
						//TODO remove this once push works.
						//dlete old notifications
//						NotificationsHelper.deleteAll(null);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	@Override
	protected void onPostExecute(String token) {
		if (token == null) {
			// remove the old data (Json) from sharedpref
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.remove(JPHelper.MY_ITINERARIES);
			editor.remove(JPHelper.MY_RECURRENTJOURNEYS);
			editor.putBoolean(JPHelper.IS_ANONYMOUS, false);
			editor.commit();
		}
		// and refresh the token
		if (listener != null)
			listener.onTaskCompleted(token);
		((SherlockActivity)activity).supportInvalidateOptionsMenu();
	}

}
