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
package eu.trentorise.smartcampus.jp.custom.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.android.common.navigation.NavigationHelper;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.SmartCheckParkingsAdapter;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.model.ParkingSerial;

public class ParkingsInfoDialog extends SherlockDialogFragment {

	public static final String ARG_PARKING = "parking";
	public static final String ARG_PARKINGS = "parkings";
	private ParkingSerial parking;
	private Location myLocation;
	private List<ParkingSerial> parkingsList;

	// private RadioGroup parkingsRadioGroup;

	public ParkingsInfoDialog() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.parking = (ParkingSerial) this.getArguments().getSerializable(ARG_PARKING);
		parkingsList = (ArrayList<ParkingSerial>) this.getArguments().getSerializable(ARG_PARKINGS);

		GeoPoint myGeoPoint = JPHelper.getLocationHelper().getLocation();
		myLocation = new Location("");
		myLocation.setLatitude(myGeoPoint.getLatitudeE6() / 1e6);
		myLocation.setLongitude(myGeoPoint.getLongitudeE6() / 1e6);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().setTitle(R.string.smart_check_parking_dialog_title);
		View view = null;

		if (parkingsList != null) {
			view = inflater.inflate(R.layout.smartcheck_parkings_dialog_multi, container, false);
		} else {
			view = inflater.inflate(R.layout.smartcheck_parkings_dialog, container, false);
		}

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (parkingsList != null) {
			// multiple stops
			ParkingSerial firstParking = parkingsList.get(0);
			View parkingView = SmartCheckParkingsAdapter.buildParking(getSherlockActivity(), R.layout.smartcheck_parking_row,
					myLocation, firstParking, null, null);

			LinearLayout entryLayout = (LinearLayout) getDialog().findViewById(R.id.parkings_dialog_entry);
			entryLayout.addView(parkingView, 0);
		} else if (parking != null) {
			// single stop
			View parkingView = SmartCheckParkingsAdapter.buildParking(getSherlockActivity(), R.layout.smartcheck_parking_row,
					myLocation, parking, null, null);

			LinearLayout entryLayout = (LinearLayout) getDialog().findViewById(R.id.parkings_dialog_entry);
			entryLayout.addView(parkingView, 0);
		}

		Button btn_cancel = (Button) getDialog().findViewById(R.id.parkings_dialog_close);
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});

		Button btn_ok = (Button) getDialog().findViewById(R.id.parkings_dialog_directions);
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Address from = new Address(Locale.getDefault());
				from.setLatitude(myLocation.getLatitude());
				from.setLongitude(myLocation.getLongitude());

				Address to = new Address(Locale.getDefault());
				to.setLatitude(parking.getPosition()[0]);
				to.setLongitude(parking.getPosition()[1]);

				NavigationHelper.bringMeThere(getActivity(), from, to);

				getDialog().dismiss();
			}
		});

	}
}
