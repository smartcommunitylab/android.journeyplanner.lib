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
package eu.trentorise.smartcampus.jp.notifications;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.jp.BaseActivity;
import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.helper.CopyTask;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.OnTaskCompleted;
import eu.trentorise.smartcampus.mobilityservice.MobilityUserService;

public class BroadcastNotificationsActivity extends BaseActivity implements OnTaskCompleted {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty_layout_jp);

		getSupportFragmentManager().popBackStack("notifications", FragmentManager.POP_BACK_STACK_INCLUSIVE);

		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD)
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		SherlockFragment fragment = new BroadcastNotificationsFragment();
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.replace(Config.mainlayout, fragment);
		fragmentTransaction.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.title_broadcast);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
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
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {

		try {
			SharedPreferences sharedPref = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
			MobilityUserService userService = new MobilityUserService(GlobalConfig.getAppUrl(this)
					+ JPHelper.MOBILITY_URL);
			if (sharedPref.contains(JPHelper.MY_ITINERARIES)) {
				String mToken = data.getExtras().getString(AccountManager.KEY_AUTHTOKEN);
				if (mToken == null) {
					Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
				} else {

					JPHelper.setUserAnonymous(this, false);
					invalidateOptionsMenu();
					JPHelper.readAccountProfile(new CopyTask(sharedPref, userService, resultCode, data,this,this));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	super.onActivityResult(requestCode, resultCode, data);
}
	@Override
	public String getAuthToken() {
		try {
			return JPHelper.getAuthToken(this);
		} catch (AACException e) {
			e.printStackTrace();
		}
		return null;
		
	}

	@Override
	public void onTaskCompleted(String result) {
		
	}

}
