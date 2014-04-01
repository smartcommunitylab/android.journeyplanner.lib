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
package eu.trentorise.smartcampus.jp;

import it.sayservice.platform.smartplanner.data.message.TType;

import java.util.Arrays;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesDBHelper;
import eu.trentorise.smartcampus.jp.timetable.CTTTCacheUpdaterAsyncTask;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class BaseActivity extends FeedbackFragmentActivity {

	protected void initDataManagement(Bundle savedInstanceState) {
		JPHelper.init(getApplicationContext());
		try {
			if (!JPHelper.getAccessProvider().login(this, null)) {
				new SCAsyncTask<Void, Void, String>(this, new LoadToken(
						BaseActivity.this)).execute();
			}
			else
				JPHelper.endAppFailure(this, R.string.app_failure_security);

		} catch (AACException e) {
			JPHelper.endAppFailure(this, R.string.app_failure_security);
			e.printStackTrace();
		}
	}

	public void initializeSharedPreferences() {
		SharedPreferences userPrefs = getSharedPreferences(Config.USER_PREFS,
				Context.MODE_PRIVATE);

		if (userPrefs.getString(Config.USER_PREFS_RTYPE, "").equals("")) {
			// create default preferences
			SharedPreferences.Editor editor = userPrefs.edit();

			// transport types
			for (int i = 0; i < Config.TTYPES_ALLOWED.length; i++) {
				TType tType = Config.TTYPES_ALLOWED[i];
				boolean enabled = Arrays.asList(Config.TTYPES_DEFAULT)
						.contains(tType) ? true : false;
				editor.putBoolean(tType.toString(), enabled);
			}

			// route type
			editor.putString(Config.USER_PREFS_RTYPE,
					Config.RTYPE_DEFAULT.toString());

			editor.commit();
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!JPHelper.isInitialized()) {
//			findViewById(android.R.id.content).post(new Runnable() {
//				
//				@Override
//				public void run() {
//					initDataManagement(savedInstanceState);
//				}
//			});
			initDataManagement(savedInstanceState);
			initializeSharedPreferences();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String token = data.getExtras().getString(
						AccountManager.KEY_AUTHTOKEN);
				if (token == null) {
					JPHelper.endAppFailure(this, R.string.app_failure_security);
				}
			} else if (resultCode == RESULT_CANCELED) {
				// TODO degio look for the missing string
				// JPHelper.endAppFailure(this, R.string.token_required);
				JPHelper.endAppFailure(this, R.string.app_failure_security);

			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public String getAppToken() {
		return JPParamsHelper.getAppToken();
	}

	@Override
	public String getAuthToken() {
		try {
			return JPHelper.getAuthToken(this);
		} catch (AACException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private class LoadToken extends AbstractAsyncTaskProcessor<Void, String> {

		public LoadToken(Activity activity) {
			super(activity);
		}

		@Override
		public String performAction(Void... params) throws SecurityException,
				ConnectionException, Exception {
			return JPHelper.getAuthToken(BaseActivity.this);
		}

		@Override
		public void handleResult(String result) {
			//ok
		}

	}
}
