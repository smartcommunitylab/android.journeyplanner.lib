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

import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.custom.MyItinerariesListAdapter;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.mobilityservice.model.BasicItinerary;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class GetMyItinerariesProcessor extends AbstractAsyncTaskProcessor<Void, List<BasicItinerary>> {

	private MyItinerariesListAdapter adapter;

	public GetMyItinerariesProcessor(SherlockFragmentActivity activity, MyItinerariesListAdapter adapter) {
		super(activity);
		this.adapter = adapter;
	}

	@Override
	public List<BasicItinerary> performAction(Void... params) throws SecurityException, Exception {
		return JPHelper.getMyItineraries();
	}

	@Override
	public void handleResult(List<BasicItinerary> result) {
		// if (!result.isEmpty()) {
		adapter.clear();
		for (BasicItinerary myt : result) {
			adapter.add(myt);
		}
		adapter.notifyDataSetChanged();
		// }
	}
}
