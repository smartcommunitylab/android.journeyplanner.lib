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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.Constants;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.ac.authorities.AuthorityHelper;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.mobilityservice.MobilityUserService;
import eu.trentorise.smartcampus.mobilityservice.model.BasicItinerary;
import eu.trentorise.smartcampus.mobilityservice.model.BasicRecurrentJourney;

/*
 * Task for user's promotion. Store the user's itineraries and monitors from the user data to the shared preferences.
 * After it does logout and login with the new autorithy
 * */

public class PromoteTask extends AsyncTask<Void, Void, SCAccessProvider> {
	private Activity activity = null;
	private ProgressDialog progress = null;

	public PromoteTask(Activity activity) {
		this.activity = activity;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progress  = ProgressDialog.show(activity, "", activity.getString(R.string.loading), true);

	}
	@Override
	protected SCAccessProvider doInBackground(Void... params) {
		SCAccessProvider accessprovider = null;
		try {
			accessprovider = SCAccessProvider.getInstance(activity);
			String oldToken = accessprovider.readToken(activity);
			MobilityUserService userService = new MobilityUserService(GlobalConfig.getAppUrl(activity)
					+ JPHelper.MOBILITY_URL);
			List<BasicItinerary> listMyItinerary = userService.getSingleJourneys(oldToken);
			List<BasicRecurrentJourney> listMyRecurrentJourneys = userService.getRecurrentJourneys(oldToken);

			// esegui login
			accessprovider.logout(activity);

			writeOldDataOnSP(listMyItinerary, listMyRecurrentJourneys);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return accessprovider;
	}

	private void writeOldDataOnSP(List<BasicItinerary> listMyItinerary,
			List<BasicRecurrentJourney> listMyRecurrentJourneys) {
		SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.app_name), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(JPHelper.MY_ITINERARIES, JSONUtils.convertToJSON(listMyItinerary));
		editor.putString(JPHelper.MY_RECURRENTJOURNEYS, JSONUtils.convertToJSON(listMyRecurrentJourneys));
		editor.commit();
	}

	@Override
	protected void onPostExecute(SCAccessProvider result) {
		super.onPostExecute(result);
		if (result != null) {
			Bundle bundle = new Bundle();
			bundle.putString(Constants.KEY_AUTHORITY, AuthorityHelper.A_GOOGLE_LOCAL);
			try {
				result.login(activity, bundle);
			} catch (AACException e) {
				e.printStackTrace();
			}
		}
		if (progress != null) {
			try {
				progress.cancel();
			} catch (Exception e) {
				Log.w(getClass().getName(),activity.getString(R.string.problem_closing)+e.getMessage());
			}
		}
	}
}
