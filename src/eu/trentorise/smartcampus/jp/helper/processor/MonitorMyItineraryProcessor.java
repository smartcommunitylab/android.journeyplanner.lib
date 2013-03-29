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
package eu.trentorise.smartcampus.jp.helper.processor;

import android.content.Context;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class MonitorMyItineraryProcessor extends AbstractAsyncTaskProcessor<String, Void> {

	private Context ctx;

	public MonitorMyItineraryProcessor(SherlockFragmentActivity activity) {
		super(activity);
		this.ctx = activity;
	}

	@Override
	public Void performAction(String... strings) throws SecurityException, Exception {
		// 0: monitor
		// 1: id
		boolean monitor = Boolean.parseBoolean(strings[0]);
		String id = strings[1];
		JPHelper.monitorMyItinerary(monitor, id);
		return null;
	}

	@Override
	public void handleResult(Void result) {
		// Toast toast = Toast.makeText(mFragmentActivity, name +
		// ctx.getString(R.string.toast_deleted), Toast.LENGTH_SHORT);
		// toast.show();
	}

}
