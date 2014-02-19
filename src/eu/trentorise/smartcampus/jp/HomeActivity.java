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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import eu.trentorise.smartcampus.android.common.LauncherHelper;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.notifications.BroadcastNotificationsActivity;
import eu.trentorise.smartcampus.jp.notifications.NotificationsFragmentActivityJP;

public class HomeActivity extends TutorialManagerActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD)
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		List<View> list = createButtons();
		LinearLayout ll = null;
		LinearLayout parent = (LinearLayout)findViewById(R.id.homelayout);
		for (int i = 0; i < list.size(); i++){
			if (ll == null) {
				ll = new LinearLayout(this);
				ll.setOrientation(LinearLayout.HORIZONTAL);
				ll.setGravity(Gravity.TOP | Gravity.CENTER);
				ll.setWeightSum(3);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					     LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(0, 32, 0, 0);
				parent.addView(ll, layoutParams);
			}
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.weight = 1;
			ll.addView(list.get(i), layoutParams);
			if ((i+1) % 3 == 0){
				ll = null;
			}
		}
		// DEBUG PURPOSE
		// JPHelper.getTutorialPreferences(this).edit().clear().commit();

		// Feedback
		FeedbackFragmentInflater.inflateHandleButtonInRelativeLayout(this,
				(RelativeLayout) findViewById(R.id.home_relative_layout_jp));

//		setHiddenNotification();
		
		if (LauncherHelper.isLauncherInstalled(this, true) && JPHelper.isFirstLaunch(this)) {
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
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_pref, Menu.NONE, R.string.btn_myprofile);

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
		} else if (item.getItemId() == R.id.menu_item_pref) {
			Intent intent = new Intent(this, ProfileActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
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
		} else if (viewId == R.id.btn_monitorrecurrent) {
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
		} else if (viewId == R.id.btn_monitorsaved) {
			intent = new Intent(this, SavedJourneyActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_notifications) {
			intent = new Intent(this, NotificationsFragmentActivityJP.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else {
			String[] smartNames = getResources().getStringArray(R.array.smart_checks_list);
			TypedArray smartIds = getResources().obtainTypedArray(R.array.smart_check_list_ids);
			for (int i = 0; i < smartNames.length; i++) {
				String scName = smartNames[i];
				if (smartIds.getResourceId(i, 0) == viewId) {
					SmartCheckDirectActivity.startSmartCheck(this, scName);
					return;
				}
			}
			smartIds.recycle();
			
			Toast toast = Toast.makeText(getApplicationContext(), R.string.tmp, Toast.LENGTH_SHORT);
			toast.show();
			return;
		}
	}
	
	private List<View> createButtons() {
		List<View> list = new ArrayList<View>();
		// First, set the smart check options
		String[] smartNames = getResources().getStringArray(R.array.smart_checks_list);
		List<String> smartNamesFiltered = Arrays.asList(JPParamsHelper.getSmartCheckOptions());
		TypedArray smartIds = getResources().obtainTypedArray(R.array.smart_check_list_ids);
		TypedArray smartIcons = getResources().obtainTypedArray(R.array.smart_check_list_icons);
		for (int i = 0; i < smartNames.length; i++) {
			if (smartNamesFiltered.contains(smartNames[i])) {
				Button b = (Button)getLayoutInflater().inflate(R.layout.home_btn, null);
				b.setText(smartNames[i]);
				b.setId(smartIds.getResourceId(i, 0));
				b.setCompoundDrawablesWithIntrinsicBounds(null, smartIcons.getDrawable(i), null, null);
				list.add(b);
			}
		}
		smartIcons.recycle();
		smartIds.recycle();
		String[] allNames = getResources().getStringArray(R.array.main_list);
		TypedArray allIds = getResources().obtainTypedArray(R.array.main_list_ids);
		TypedArray allIcons = getResources().obtainTypedArray(R.array.main_list_icons);
		for (int i = 0; i < allNames.length; i++) {
			Button b = (Button)getLayoutInflater().inflate(R.layout.home_btn, null);
			b.setText(allNames[i]);
			b.setId(allIds.getResourceId(i, 0));
			b.setCompoundDrawablesWithIntrinsicBounds(null, allIcons.getDrawable(i), null, null);
			list.add(b);
		}
		allIcons.recycle();
		allIds.recycle();
		return list;
	}	
}
