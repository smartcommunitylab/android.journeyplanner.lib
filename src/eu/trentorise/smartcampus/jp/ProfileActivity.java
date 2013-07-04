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

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.jp.custom.TabListener;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;

public class ProfileActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.empty_layout_jp);

		// getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().removeAllTabs();

		// Profile
		ActionBar.Tab tab = getSupportActionBar().newTab().setText(R.string.tab_profile);
		tab.setTabListener(new TabListener<ProfileFragment>(this, Config.PROFILE_FRAGMENT_TAG, ProfileFragment.class,
				Config.mainlayout));
		getSupportActionBar().addTab(tab);

		// Settings
		tab = getSupportActionBar().newTab().setText(R.string.tab_favorites);
		tab.setTabListener(new TabListener<FavoritesFragment>(this, Config.FAVORITES_FRAGMENT_TAG, FavoritesFragment.class,
				Config.mainlayout));
		getSupportActionBar().addTab(tab);

		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.title_prefs);
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

	public void favoriteDeletionHandler(View target) {
		TextView textView = null;
		ViewGroup row = (ViewGroup) target.getParent();
		for (int itemPos = 0; itemPos < row.getChildCount(); itemPos++) {
			View view = row.getChildAt(itemPos);
			if (view instanceof TextView) {
				textView = (TextView) view;
				break;
			}
		}
		if (textView != null) {
			String favorite = textView.getText().toString();
			Toast.makeText(this, favorite + getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
		}
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
