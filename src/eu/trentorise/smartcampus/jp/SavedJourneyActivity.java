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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.jp.custom.TabListener;
import eu.trentorise.smartcampus.jp.custom.data.BasicRecurrentJourney;

public class SavedJourneyActivity extends BaseActivity {
	
	public static final String PARAMS = "params";
	public static String param = "";
	
	@Override
	protected void onResume() {
		super.onResume();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.title_saved_journey);
		
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

	    if (getIntent() != null && getIntent().getStringExtra(PARAMS) != null ) {
	    	param = getIntent().getStringExtra(PARAMS);
 
	    }
		// New my one off journey
		ActionBar.Tab tab = getSupportActionBar().newTab().setText(R.string.tab_myoneoffjourneys);
		tab.setTabListener(new TabListener<MyItinerariesFragment>(this, Config.MY_JOURNEYS_FRAGMENT_TAG,
				MyItinerariesFragment.class, Config.mainlayout));
		getSupportActionBar().addTab(tab);

		// My rec journeys
		tab = getSupportActionBar().newTab().setText(R.string.tab_myrecjourneys);
		tab.setTabListener(new TabListener<MyRecurItinerariesFragment>(this, Config.MY_RECUR_JOURNEYS_FRAGMENT_TAG,
				MyRecurItinerariesFragment.class, Config.mainlayout));
		getSupportActionBar().addTab(tab);
		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);	
		setTabFromParameters();
		

		}
	}


	private void setTabFromParameters() {
		if (param.compareTo("")!=0){
				if (MyItinerariesFragment.class.getName().equals(param))
					getSupportActionBar().setSelectedNavigationItem(0);
				else if (MyRecurItinerariesFragment.class.getName().equals(param))
					getSupportActionBar().setSelectedNavigationItem(1);
			
			
		}		
	}
	
}
