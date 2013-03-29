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

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.model.TripData;

public class SmartCheckRoutesListAdapter extends ArrayAdapter<TripData> {

	Context mContext;
	int layoutResourceId;
	List<TripData> trips;

	String[] lines;
	TypedArray icons;
	TypedArray colors;

	public SmartCheckRoutesListAdapter(Context context, int layoutResourceId, List<TripData> trips) {
		super(context, layoutResourceId, trips);
		this.mContext = context;
		this.layoutResourceId = layoutResourceId;
		this.trips = trips;

		lines = mContext.getResources().getStringArray(R.array.smart_checks_bus_number);
		icons = mContext.getResources().obtainTypedArray(R.array.smart_checks_bus_icons);
		colors = mContext.getResources().obtainTypedArray(R.array.smart_checks_bus_color);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		TripData tripData = trips.get(position);

		RowHolder holder = null;
		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RowHolder();
			holder.route = (TextView) row.findViewById(R.id.smartcheck_trip_route);
			holder.time = (TextView) row.findViewById(R.id.smartcheck_trip_time);
			holder.delay = (TextView) row.findViewById(R.id.smartcheck_trip_delay);
			row.setTag(holder);
		} else {
			holder = (RowHolder) row.getTag();
		}

		// separator
		if (position == 0 || !(trips.get(position - 1).getRouteId()).equalsIgnoreCase(tripData.getRouteId())) {
			holder.route.setBackgroundColor(mContext.getResources().getColor(R.color.sc_gray));
			for (int i = 0; i < lines.length; i++) {
				if (tripData.getRouteId().startsWith(lines[i])) {
					holder.route.setBackgroundColor(colors.getColor(i, 0));
					break;
				}
			}

			holder.route.setText(tripData.getRouteShortName() + " - " + tripData.getRouteName());
			holder.route.setVisibility(View.VISIBLE);
		} else {
			holder.route.setVisibility(View.GONE);
		}

		// time
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(tripData.getTime() * 1000);
		String timeFromString = Config.FORMAT_TIME_UI.format(time.getTime());
		holder.time.setText(timeFromString);

		// delay
		if (tripData.getDelay() > 0) {
			holder.delay.setText(mContext.getString(R.string.smart_check_stops_delay) + " " + tripData.getDelay() + "'");
		}
		// else {
		// holder.delay.setText(mContext.getString(R.string.smart_check_stops_delay)
		// + " " + "5'");
		// }

		return row;
	}

	static class RowHolder {
		TextView route;
		TextView time;
		TextView delay;
	}

}
