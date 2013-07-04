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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;

public class BaseActivity extends FeedbackFragmentActivity {

	private void initDataManagement(Bundle savedInstanceState) {
		try {
			JPHelper.init(getApplicationContext());
			String token = JPHelper.getAccessProvider().getAuthToken(this, null);
			if (token != null) {
				initData();
			}
		} catch (Exception e) {
			JPHelper.endAppFailure(this, R.string.app_failure_setup);
		}
	}
	

	protected void initData() {
	}

	public void initializeSharedPreferences() {
		SharedPreferences userPrefs = getSharedPreferences(Config.USER_PREFS, Context.MODE_PRIVATE);

		if (userPrefs.getString(Config.USER_PREFS_RTYPE, "").equals("")) {
			// create default preferences
			SharedPreferences.Editor editor = userPrefs.edit();

			// transport types
			for (int i = 0; i < Config.TTYPES_ALLOWED.length; i++) {
				TType tType = Config.TTYPES_ALLOWED[i];
				boolean enabled = Arrays.asList(Config.TTYPES_DEFAULT).contains(tType) ? true : false;
				editor.putBoolean(tType.toString(), enabled);
			}

			// route type
			editor.putString(Config.USER_PREFS_RTYPE, Config.RTYPE_DEFAULT.toString());

			editor.commit();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!JPHelper.isInitialized()) {
			initDataManagement(savedInstanceState);
			initializeSharedPreferences();
		}
	}
	

	


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String token = data.getExtras().getString(AccountManager.KEY_AUTHTOKEN);
				if (token == null) {
					JPHelper.endAppFailure(this, R.string.app_failure_security);
				} else {
					initData();
				}
			} else if (resultCode == RESULT_CANCELED) {
				JPHelper.endAppFailure(this, R.string.token_required);
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
		return JPHelper.getAuthToken();
	}
}
