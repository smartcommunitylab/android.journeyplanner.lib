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
import eu.trentorise.smartcampus.jp.custom.MyRecurItinerariesListAdapter;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.mobilityservice.model.BasicRecurrentJourney;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class GetMyRecurItinerariesProcessor extends AbstractAsyncTaskProcessor<Void, List<BasicRecurrentJourney>> {

	private MyRecurItinerariesListAdapter adapter;

	public GetMyRecurItinerariesProcessor(SherlockFragmentActivity activity, MyRecurItinerariesListAdapter adapter) {
		super(activity);
		this.adapter = adapter;
	}

	@Override
	public List<BasicRecurrentJourney> performAction(Void... params) throws SecurityException, Exception {
		return JPHelper.getMyRecurItineraries();
	}

	@Override
	public void handleResult(List<BasicRecurrentJourney> result) {
		 if (!result.isEmpty()) {
		adapter.clear();
		for (BasicRecurrentJourney myt : result) {
			adapter.add(myt);
		}
		adapter.notifyDataSetChanged();
		 }
	}
}
