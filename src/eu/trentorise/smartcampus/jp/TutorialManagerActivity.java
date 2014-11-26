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

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.view.MenuItem;
import com.github.espiandev.showcaseview.TutorialHelper;
import com.github.espiandev.showcaseview.TutorialHelper.TutorialProvider;
import com.github.espiandev.showcaseview.TutorialItem;

import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;

/**
 * @author raman
 * 
 */
public class TutorialManagerActivity extends BaseActivity {

	protected TutorialHelper mTutorialHelper = null;
	private List<TutorialItem> tutorial = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTutorialHelper = new TutorialHelper(this, mTutorialProvider);
	}

	protected void showTourDialog() {
		mTutorialHelper.showTourDialog(getString(R.string.jp_first_launch), getString(R.string.begin_tut));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_tutorial) {
			JPHelper.resetTutorialPreferences(this);
			JPHelper.setWantTour(getApplicationContext(), true);
			mTutorialHelper.showTutorials();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		tutorial = null;
	}

	private TutorialProvider mTutorialProvider = new TutorialProvider() {
		private List<TutorialItem> tutorial() {
			if (tutorial == null) {
				tutorial = new ArrayList<TutorialItem>();
				String[] smartNames = getResources().getStringArray(R.array.smart_checks_list);
				List<String> smartNamesFiltered = Arrays.asList(JPParamsHelper.getSmartCheckOptions());
				String[] smartTut = getResources().getStringArray(R.array.smart_check_list_tut);
				TypedArray smartIds = getResources().obtainTypedArray(R.array.smart_check_list_ids);
				for (int i = 0; i < smartNames.length; i++) {
					if (smartNamesFiltered.contains(smartNames[i])) {
						View v = findViewById(smartIds.getResourceId(i, 0));
						if (v != null) {
							int[] pos = new int[2];
							v.getLocationOnScreen(pos);
							tutorial.add(new TutorialItem(smartNames[i], pos, v.getWidth(), smartNames[i], smartTut[i]));
						}
					}
				}
				smartIds.recycle();
				String[] allNames = getResources().getStringArray(R.array.main_list);
				String[] allTut = getResources().getStringArray(R.array.main_list_tut);
				TypedArray allIds = getResources().obtainTypedArray(R.array.main_list_ids);
				for (int i = 0; i < allNames.length; i++) {
					View v = findViewById(allIds.getResourceId(i, 0));
					if (v != null) {
						int[] pos = new int[2];
						v.getLocationOnScreen(pos);
						if (allTut[i] != null) {
							tutorial.add(new TutorialItem(allNames[i], pos, v.getWidth(), allNames[i], allTut[i]));
						}
					}
				}
				allIds.recycle();
			}
			return tutorial;
		}

		@Override
		public int size() {
			return tutorial().size();
		}

		@Override
		public void onTutorialFinished() {
		}

		@Override
		public void onTutorialCancelled() {
		}

		@Override
		public TutorialItem getItemAt(int pos) {
			return tutorial().get(pos);
		}
	};

}
