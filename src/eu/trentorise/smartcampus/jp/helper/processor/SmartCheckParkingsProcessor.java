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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.location.Location;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;
import eu.trentorise.smartcampus.jp.model.ParkingSerial;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SmartCheckParkingsProcessor extends AbstractAsyncTaskProcessor<Void, List<ParkingSerial>> {

	private SherlockFragmentActivity mActivity;
	private ArrayAdapter<ParkingSerial> adapter;
	private Location myLocation;
	private String parkingAid;

	public SmartCheckParkingsProcessor(SherlockFragmentActivity activity, ArrayAdapter<ParkingSerial> adapter,
			Location myLocation, String parkingAid) {
		super(activity);
		this.mActivity = activity;
		this.adapter = adapter;

		this.parkingAid = parkingAid;

		if (myLocation != null) {
			this.myLocation = myLocation;
		}
	}

	@Override
	public List<ParkingSerial> performAction(Void... params) throws SecurityException, Exception {
		return JPHelper.getParkings(parkingAid, JPHelper.getAuthToken(mActivity));
	}

	@Override
	public void handleResult(List<ParkingSerial> result) {
		List<ParkingSerial> orderedList = new ArrayList<ParkingSerial>();

		Comparator<ParkingSerial> comparator = ParkingsHelper.getParkingNameComparator();
		if (myLocation != null) {
			comparator = ParkingsHelper.getParkingDistanceComparator(myLocation);
		}

		/*
		 * order: firsts with data
		 */
		// List<ParkingSerial> parkingsWithData = new
		// ArrayList<ParkingSerial>();
		// List<ParkingSerial> parkingsWithoutData = new
		// ArrayList<ParkingSerial>();
		//
		// for (ParkingSerial parking : result) {
		// parking.setName(ParkingsHelper.getName(parking));
		// if (parking.isMonitored() != null && parking.isMonitored()
		// && parking.getSlotsAvailable() != ParkingsHelper.PARKING_UNAVAILABLE)
		// {
		// parkingsWithData.add(parking);
		// } else {
		// parkingsWithoutData.add(parking);
		// }
		// }
		//
		// Collections.sort(parkingsWithData, comparator);
		// Collections.sort(parkingsWithoutData, comparator);
		//
		// for (ParkingSerial parking : parkingsWithData) {
		// orderedList.add(parking);
		// }
		//
		// for (ParkingSerial parking : parkingsWithoutData) {
		// orderedList.add(parking);
		// }
		//
		// adapter.clear();
		// for (ParkingSerial parking : orderedList) {
		// adapter.add(parking);
		// }

		/*
		 * order: all the same
		 */
		Collections.sort(result, comparator);

		adapter.clear();
		for (ParkingSerial parking : result) {
			adapter.add(parking);
		}
		orderedList = result;

		adapter.notifyDataSetChanged();

		Boolean allDataUnavailable = null;
		for (ParkingSerial parking : result) {
			// Check for availability
			if (parking.isMonitored()) {
				allDataUnavailable = true;
				if (parking.getSlotsAvailable() != ParkingsHelper.PARKING_UNAVAILABLE) {
					allDataUnavailable = false;
					break;
				}
			}
		}

		if (allDataUnavailable != null && allDataUnavailable == true) {
			// "Sorry, all data unavailable" toast
			Toast.makeText(mActivity, R.string.smart_check_parking_all_unavailable_toast, Toast.LENGTH_LONG).show();
		}

		// save in cache
		ParkingsHelper.setCache(orderedList);

		mActivity.setSupportProgressBarIndeterminateVisibility(false);
	}
}
