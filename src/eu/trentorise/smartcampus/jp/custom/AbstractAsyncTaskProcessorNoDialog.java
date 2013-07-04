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

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.HandleExceptionHelper;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.HomeActivity;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.AsyncTaskNoDialog.SCAsyncTaskProcessorNoDialog;
import eu.trentorise.smartcampus.jp.helper.JPHelper;

public abstract class AbstractAsyncTaskProcessorNoDialog<Params, Result> implements SCAsyncTaskProcessorNoDialog<Params, Result>{

	protected SherlockFragmentActivity activity;
	
	public AbstractAsyncTaskProcessorNoDialog(SherlockFragmentActivity activity) {
		super();
		this.activity = activity;
	}

	@Override
	public void handleFailure(Exception e) {
		Log.e(activity.getClass().getName(), ""+e.getMessage());
		JPHelper.showFailure(activity, R.string.app_failure_operation);
	}

	
	
	@Override
	public void handleConnectionError() {
		HandleExceptionHelper.showDialogConnectivity(activity);
		
	}

	@Override
	public void handleSecurityError() {
		SCAccessProvider accessProvider =  JPHelper.getAccessProvider();
		try {
			accessProvider.invalidateToken(activity, null);
			accessProvider.getAuthToken(activity, null);
		} catch (Exception e) {
			Log.e(HomeActivity.class.getName(),""+ e.getMessage());
			JPHelper.showFailure(activity, R.string.app_failure_security);
		}
	}

}
