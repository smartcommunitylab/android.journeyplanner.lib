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

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.notifications.BroadcastNotificationsActivity;
import eu.trentorise.smartcampus.jp.notifications.NotificationsFragmentActivityJP;

public class HomeActivity extends TutorialManagerActivity {

	private boolean mHiddenNotification;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationInfo ai;
			try {
				ai = getPackageManager().getApplicationInfo(
						getPackageName(), PackageManager.GET_META_DATA);

			Bundle aBundle = ai.metaData;
			if (aBundle.getBoolean("hidden-notification"))
					setContentView(R.layout.home_no_notif);
			else setContentView(R.layout.home);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD)
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		// DEBUG PURPOSE
		// JPHelper.getTutorialPreferences(this).edit().clear().commit();

		// Feedback
		FeedbackFragmentInflater.inflateHandleButtonInRelativeLayout(this,
				(RelativeLayout) findViewById(R.id.home_relative_layout_jp));

		setHiddenNotification();

		if (JPHelper.isFirstLaunch(this)) {
			showTourDialog();
			JPHelper.disableFirstLaunch(this);
		}
	}
	@Override
	protected void onStart() {
		super.onStart();

		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);

		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		// getSupportMenuInflater().inflate(R.menu.emptymenu, menu);
		getSupportMenuInflater().inflate(R.menu.gripmenu, menu);

		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_tutorial, Menu.NONE, R.string.menu_tutorial);
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_help, Menu.NONE, R.string.menu_help);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		} else if (item.getItemId() == R.id.menu_item_help) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(getString(R.string.url_help)));
			startActivity(i);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		JPHelper.getLocationHelper().stop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		JPHelper.getLocationHelper().start();
	}

	public void goToFunctionality(View view) {
		Intent intent;
		int viewId = view.getId();

		if (viewId == R.id.btn_planjourney) {
			intent = new Intent(this, PlanJourneyActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_monitorrecurrentjourney) {
			intent = new Intent(this, MonitorJourneyActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_broadcast) {
			intent = new Intent(this, BroadcastNotificationsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_myprofile) {
			intent = new Intent(this, ProfileActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_monitorsavedjourney) {
			intent = new Intent(this, SavedJourneyActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_smart) {
			intent = new Intent(this, SmartCheckActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_notifications) {
			intent = new Intent(this, NotificationsFragmentActivityJP.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else {
			Toast toast = Toast.makeText(getApplicationContext(), R.string.tmp, Toast.LENGTH_SHORT);
			toast.show();
			return;
		}
	}

	private void setHiddenNotification() {
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			mHiddenNotification = aBundle.getBoolean("hidden-notification");
		} catch (NameNotFoundException e) {
			mHiddenNotification = false;
			Log.e(HomeActivity.class.getName(), "you should set the hidden-notification metadata in app manifest");
		}
		if (mHiddenNotification) {
			View notificationButton = findViewById(R.id.btn_notifications);
			if (notificationButton != null)
				notificationButton.setVisibility(View.GONE);
		}
	}

}
