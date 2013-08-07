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

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;
import eu.trentorise.smartcampus.jp.model.ParkingSerial;

public class SmartCheckParkingsAdapter extends ArrayAdapter<ParkingSerial> {

	private Context mContext;
	private int layoutResourceId;
	private Location myLocation;

	public SmartCheckParkingsAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.mContext = context;
		this.layoutResourceId = layoutResourceId;
	}

	public void setMyLocation(Location location) {
		this.myLocation = location;
	}

	public void setMyLocation(GeoPoint geoPoint) {
		if (geoPoint != null) {
			Location location = new Location("");
			location.setLatitude(geoPoint.getLatitudeE6() / 1e6);
			location.setLongitude(geoPoint.getLongitudeE6() / 1e6);
			this.myLocation = location;
		} else {
			this.myLocation = null;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ParkingSerial parking = getItem(position);
		return buildParking(mContext, layoutResourceId, myLocation, parking, convertView, parent);
	}

	public static View buildParking(Context mContext, int layoutResourceId, Location myLocation, ParkingSerial parking,
			View convertView, ViewGroup parent) {

		View row = convertView;
		ParkingHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new ParkingHolder();
			holder.parkingName = (TextView) row.findViewById(R.id.smart_check_parking_name);
			holder.parkingData = (TextView) row.findViewById(R.id.smart_check_parking_data);
			holder.parkingStatus = (TextView) row.findViewById(R.id.smart_check_parking_status);
			holder.parkingDistance = (TextView) row.findViewById(R.id.smart_check_parking_distance);

			row.setTag(holder);
		} else {
			holder = (ParkingHolder) row.getTag();
		}

		// name
		String parkingName = ParkingsHelper.getName(parking);
		holder.parkingName.setText(parkingName);

		// description
		if (!parkingName.equalsIgnoreCase(parking.getDescription())) {
			holder.parkingData.setText(parking.getDescription());
			holder.parkingData.setVisibility(View.VISIBLE);
		} else {
			holder.parkingData.setVisibility(View.GONE);
		}

		// status
		holder.parkingStatus.setText(mContext.getString(R.string.smart_check_parking_avail, parking.getSlotsAvailable(),
				parking.getSlotsTotal()));

		if (parking.isMonitored()) {
			if (parking.getSlotsAvailable() == ParkingsHelper.PARKING_FULL) {
				holder.parkingStatus.setText(mContext.getString(R.string.smart_check_parking_full));
			} else if (parking.getSlotsAvailable() == ParkingsHelper.PARKING_UNAVAILABLE) {
				// data unavailable
				// holder.parkingStatus.setText(mContext.getString(R.string.smart_check_parking_avail,
				// "?",
				// parking.getSlotsTotal()));
				holder.parkingStatus.setText(Integer.toString(parking.getSlotsTotal()));
			}
		} else {
			// not monitored
			holder.parkingStatus.setText(Integer.toString(parking.getSlotsTotal()));
		}

		holder.parkingStatus.setTextColor(mContext.getResources().getColor(ParkingsHelper.getColor(parking)));

		// distance
		if (myLocation != null) {
			Location parkingLocation = new Location("");
			parkingLocation.setLatitude(parking.getPosition()[0]);
			parkingLocation.setLongitude(parking.getPosition()[1]);
			float distance = myLocation.distanceTo(parkingLocation);
			String distanceString = String.format("%.2f", distance / 1000) + " km";
			holder.parkingDistance.setText(distanceString);
			holder.parkingDistance.setVisibility(View.VISIBLE);
		} else {
			holder.parkingDistance.setVisibility(View.GONE);
		}

		return row;
	}

	public static class ParkingHolder {
		TextView parkingName;
		TextView parkingData;
		TextView parkingStatus;
		TextView parkingDistance;
	}
}
