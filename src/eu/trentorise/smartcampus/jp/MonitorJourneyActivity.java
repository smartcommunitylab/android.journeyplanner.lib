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

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

public class MonitorJourneyActivity extends BaseActivity {

	@Override
	protected void onResume() {
		super.onResume();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.title_monitor_journey);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.empty_layout_jp);

		// getSupportActionBar().setDisplayShowTitleEnabled(false);
		// getSupportActionBar().removeAllTabs();

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		SherlockFragment fragment = new MonitorJourneyFragment();
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.replace(Config.mainlayout, fragment);
		fragmentTransaction.commit();

		// // New journey
		// ActionBar.Tab tab =
		// getSupportActionBar().newTab().setText(R.string.tab_myoneoffjourneys);
		// tab.setTabListener(new TabListener<PlanNewJourneyFragment>(this,
		// Config.PLAN_NEW_FRAGMENT_TAG,
		// PlanNewJourneyFragment.class, Config.mainlayout));
		// getSupportActionBar().addTab(tab);

		// // new recur journeys
		// tab =
		// getSupportActionBar().newTab().setText(R.string.tab_myjourneys);
		// tab.setTabListener(new TabListener<MyItinerariesFragment>(this,
		// Config.MY_JOURNEYS_FRAGMENT_TAG,
		// MyItinerariesFragment.class, Config.mainlayout));
		// getSupportActionBar().addTab(tab);

		// tab =
		// getSupportActionBar().newTab().setText(R.string.tab_myrecjourneys);
		// tab.setTabListener(new TabListener<PlanRecurJourneyFragment>(this,
		// Config.PLAN_NEW_RECUR_FRAGMENT_TAG,
		// PlanRecurJourneyFragment.class, Config.mainlayout));
		// getSupportActionBar().addTab(tab);
		//
		// if (getSupportActionBar().getNavigationMode() !=
		// ActionBar.NAVIGATION_MODE_TABS) {
		// getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// }
	}
}
