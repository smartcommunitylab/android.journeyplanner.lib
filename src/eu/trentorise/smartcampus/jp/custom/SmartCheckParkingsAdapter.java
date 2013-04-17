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

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.trentorise.smartcampus.jp.R;

public class SmartCheckParkingsAdapter extends ArrayAdapter<Parking> {

	Context mContext;
	int layoutResourceId;

	public SmartCheckParkingsAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.mContext = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Holder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new Holder();
			holder.parkingName = (TextView) row.findViewById(R.id.smart_check_parking_name);
			holder.parkingData = (TextView) row.findViewById(R.id.smart_check_parking_data);
			holder.parkingStatus = (TextView) row.findViewById(R.id.smart_check_parking_status);

			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}

		Parking parking = getItem(position);

		holder.parkingName.setText(parking.getName());
		// TODO: distance from my position
		holder.parkingData.setText(parking.getDescription());

		holder.parkingStatus.setText(mContext.getString(R.string.smart_check_parking_avail, parking.getSlotsAvailable(),
				parking.getSlotsTotal()));

		if (parking.getSlotsAvailable() > 20) {
			holder.parkingStatus.setTextColor(mContext.getResources().getColor(R.color.parking_green));
		} else if (parking.getSlotsAvailable() <= 20 && parking.getSlotsAvailable() > 5) {
			holder.parkingStatus.setTextColor(mContext.getResources().getColor(R.color.parking_orange));
		} else if (parking.getSlotsAvailable() <= 5) {
			holder.parkingStatus.setTextColor(mContext.getResources().getColor(R.color.red));
			if (parking.getSlotsAvailable() == 0) {
				holder.parkingStatus.setText(mContext.getString(R.string.smart_check_parking_full));
			}
		}

		return row;
	}

	private class Holder {
		TextView parkingName;
		TextView parkingData;
		TextView parkingStatus;
	}
}
