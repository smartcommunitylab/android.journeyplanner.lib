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

import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;
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

public class SmartCheckAlertsAdapter extends ArrayAdapter<AlertRoad> {

	private Context mContext;
	private int layoutResourceId;
	private Location myLocation;

	public SmartCheckAlertsAdapter(Context context, int layoutResourceId) {
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
		AlertRoad alertRoad = getItem(position);
		return buildAlertRoad(mContext, layoutResourceId, myLocation, alertRoad, convertView, parent);
	}

	public static View buildAlertRoad(Context mContext, int layoutResourceId, Location myLocation, AlertRoad alertRoad,
			View convertView, ViewGroup parent) {

		View row = convertView;
		AlertRoadHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new AlertRoadHolder();
			holder.alertRoadType = (TextView) row.findViewById(R.id.smartcheck_alert_type);
			holder.alertRoadStreet = (TextView) row.findViewById(R.id.smartcheck_alert_street);
			holder.alertRoadDescription = (TextView) row.findViewById(R.id.smartcheck_alert_description);

			row.setTag(holder);
		} else {
			holder = (AlertRoadHolder) row.getTag();
		}

		// type
		holder.alertRoadType.setText(alertRoad.getType().toString());

		// street
		holder.alertRoadStreet.setText(alertRoad.getRoad().getStreet());

		// description
		holder.alertRoadDescription.setText("TO DO");

		return row;
	}

	public static class AlertRoadHolder {
		TextView alertRoadType;
		TextView alertRoadStreet;
		TextView alertRoadDescription;
	}
}
