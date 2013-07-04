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
package eu.trentorise.smartcampus.jp.custom;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.Config;

public class TabListener<T extends Fragment> implements ActionBar.TabListener {
	private Fragment mFragment;
	private final SherlockFragmentActivity mActivity;
	private final String mTag;
	private final Class<T> mClass;
	private int mViewGroup = android.R.id.content;

	/**
	 * Constructor used each time a new tab is created.
	 * 
	 * @param activity
	 *            The host Activity, used to instantiate the fragment
	 * @param tag
	 *            The identifier tag for the fragment
	 * @param clz
	 *            The fragment's Class, used to instantiate the fragment
	 */
	public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz, Integer viewGroup) {
		mActivity = activity;
		mTag = tag;
		mClass = clz;
		if (viewGroup != null) {
			mViewGroup = viewGroup;
		}
	}

	/* The following are each of the ActionBar.TabListener callbacks */

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Fragment preInitializedFragment =  mActivity.getSupportFragmentManager().findFragmentByTag(
				mTag);
		if (preInitializedFragment != null && !preInitializedFragment.equals(mFragment)) {
			ft.remove(preInitializedFragment);
		}

		if (mFragment == null) {
			// If not, instantiate and add it to the activity
			mFragment = Fragment.instantiate(mActivity, mClass.getName(), tab.getTag() != null ? (Bundle) tab.getTag() : null);
			 ft.add(android.R.id.content, mFragment, mTag);
//			ft.replace(android.R.id.content, mFragment);
			
		} else {
			// If it exists, simply attach it in order to show it
			ft.attach(mFragment);
		}

		// SherlockFragment preInitializedFragment = (SherlockFragment)
		// mActivity.getSupportFragmentManager()
		// .findFragmentByTag(mTag);
		//
		// if (mFragment == null && preInitializedFragment == null) {
		// mFragment = (SherlockFragment)
		// SherlockFragment.instantiate(mActivity, mClass.getName());
		// ft.add(mViewGroup, mFragment, mTag);
		// } else if (mFragment != null) {
		// // If it exists, simply attach it in order to show it
		// ft.attach(mFragment);
		// } else if (preInitializedFragment != null) {
		// mFragment = preInitializedFragment;
		// ft.attach(mFragment);
		//
		// }
		// // Check if the fragment is already initialized
		// if (mFragment == null) {
		// // If not, instantiate and add it to the activity
		// mFragment = Fragment.instantiate(mActivity, mClass.getName());
		// ft.add(mViewGroup, mFragment, mTag);
		// } else {
		// // If it exists, simply attach it in order to show it
		// ft.attach(mFragment);
		// }

	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// if (mFragment != null) {
		// // Detach the fragment, because another one is being attached
		// ft.detach(mFragment);
		// }
		mActivity.getSupportFragmentManager().popBackStack(mTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		if (mFragment != null) {
			// Detach the fragment, because another one is being attached
			ft.detach(mFragment);
		}
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// User selected the already selected tab. Usually do nothing.
	}
}
