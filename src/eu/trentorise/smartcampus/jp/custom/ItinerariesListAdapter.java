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

import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.data.message.Transport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.data.BusStop;
import eu.trentorise.smartcampus.jp.custom.draw.LineDrawView;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.jp.helper.Utils;

public class ItinerariesListAdapter extends ArrayAdapter<Itinerary> {

	Context context;
	int layoutResourceId;

	public ItinerariesListAdapter(Context context, int layoutResourceId,
			List<Itinerary> itineraries) {
		super(context, layoutResourceId, itineraries);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RowHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RowHolder();
			holder.timeFromTo = (TextView) row.findViewById(R.id.it_time_from);
			holder.time = (TextView) row.findViewById(R.id.it_time);
			holder.transportTypes = (LinearLayout) row
					.findViewById(R.id.it_transporttypes);
			holder.alert = (ImageView) row.findViewById(R.id.it_alert);

			row.setTag(holder);
		} else {
			holder = (RowHolder) row.getTag();
		}

		Itinerary itinerary = getItem(position);

		// time from
		Date timeFrom = new Date(itinerary.getStartime());
		String timeFromString = Config.FORMAT_TIME_UI.format(timeFrom);

		// time to
		Date timeTo = new Date(itinerary.getEndtime());
		String timeToString = Config.FORMAT_TIME_UI.format(timeTo);
		holder.timeFromTo.setText(context.getString(R.string.itinerary_timing, timeFromString, timeToString));

		holder.time.setText("("+((timeTo.getTime()-timeFrom.getTime())/60000)+"m)");

//		holder.line.addView(new LineDrawView(getContext()));

		holder.transportTypes.removeAllViews();
		ImageView imgv = null;
		
		// transport types & alerts
		for (Leg l : itinerary.getLeg()) {
			Transport transp = l.getTransport();
			TType t = transp.getType();
			if (t.equals(TType.BUS)) {
				String line = transp.getRouteShortName();
				imgv = Utils.getImageByLine(getContext(), line);
			} else {
				imgv = Utils.getImageByTType(getContext(), t);
			}
			if (imgv.getBackground() != null || imgv.getDrawable() != null) {

				holder.transportTypes.addView(imgv);
			}
			if (Utils.containsAlerts(l)) {
				holder.alert.setVisibility(View.VISIBLE);
			} else {
				holder.alert.setVisibility(View.GONE);
			}
		}

		return row;
	}

	static class RowHolder {
		TextView timeFromTo;
		TextView time;
		LinearLayout transportTypes;
		ImageView alert;
	}
}
