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

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SmartCheckParkingsProcessor extends AbstractAsyncTaskProcessor<Void, List<Parking>> {

	private ArrayAdapter<Parking> adapter;

	public SmartCheckParkingsProcessor(SherlockFragmentActivity activity, ArrayAdapter<Parking> adapter) {
		super(activity);
		this.adapter = adapter;
	}

	@Override
	public List<Parking> performAction(Void... params) throws SecurityException, Exception {
		List<Parking> list = new ArrayList<Parking>();

		list = JPHelper.getParkings();

		// // TODO: manually create parkings
		// Parking parking = new Parking();
		//
		// parking.setName("P1");
		// parking.setDescription("Via Uno");
		// parking.setSlotsTotal(200);
		// parking.setSlotsAvailable(50);
		// list.add(parking);
		//
		// parking = new Parking();
		// parking.setName("P2");
		// parking.setDescription("Via Due");
		// parking.setSlotsTotal(100);
		// parking.setSlotsAvailable(10);
		// list.add(parking);
		//
		// parking = new Parking();
		// parking.setName("P3");
		// parking.setDescription("Via Tre");
		// parking.setSlotsTotal(70);
		// parking.setSlotsAvailable(4);
		// list.add(parking);
		//
		// parking = new Parking();
		// parking.setName("P4");
		// parking.setDescription("Via Quattro");
		// parking.setSlotsTotal(50);
		// parking.setSlotsAvailable(0);
		// list.add(parking);

		Collections.sort(list, new Comparator<Parking>() {
			public int compare(Parking p1, Parking p2) {
				return p1.getName().toString().compareTo(p2.getName().toString());
			}
		});

		return list;
	}

	@Override
	public void handleResult(List<Parking> result) {
		adapter.clear();
		for (Parking parking : result) {
			adapter.add(parking);
		}
		adapter.notifyDataSetChanged();
	}

}
