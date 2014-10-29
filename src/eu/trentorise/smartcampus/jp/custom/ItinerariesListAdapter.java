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

import java.util.Date;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;
import eu.trentorise.smartcampus.jp.helper.Utils;

public class ItinerariesListAdapter extends ArrayAdapter<Itinerary> {

	Context context;
	int layoutResourceId;

	public ItinerariesListAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
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
			holder.timeFrom = (TextView) row.findViewById(R.id.it_time_from);
			holder.timeTo = (TextView) row.findViewById(R.id.it_time_to);
			// holder.time = (TextView) row.findViewById(R.id.it_time);
			holder.transportTypes = (LinearLayout) row.findViewById(R.id.it_transporttypes);
			holder.parkingTime = (TextView) row.findViewById(R.id.it_parkingdata_time);
			holder.parkingCost = (TextView) row.findViewById(R.id.it_parkingdata_price);
			holder.alert = (ImageView) row.findViewById(R.id.it_alert);
			row.setTag(holder);
		} else {
			holder = (RowHolder) row.getTag();
		}

		Itinerary itinerary = getItem(position);

		if (itinerary.isPromoted()) {
			row.setBackgroundColor(getContext().getResources().getColor(android.R.color.white));
		} else {
			row.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
		}

		// time from
		Date timeFrom = new Date(itinerary.getStartime());
		String timeFromString = Config.FORMAT_TIME_UI.format(timeFrom);
		holder.timeFrom.setText(timeFromString);
		// time to
		Date timeTo = new Date(itinerary.getEndtime());
		String timeToString = Config.FORMAT_TIME_UI.format(timeTo);
		holder.timeTo.setText(timeToString);

		// holder.timeFrom.setText(context.getString(R.string.itinerary_timing,
		// timeFromString, timeToString));
		// holder.time.setText("(" + ((timeTo.getTime() - timeFrom.getTime()) /
		// 60000) + "m)");

		holder.transportTypes.removeAllViews();
		ImageView imgv = null;
		// transport types & alerts
		for (int i = 0; i < itinerary.getLeg().size(); i++) {
			// TODO: ***** TEMP *****
			// if (i > 0) {
			// break;
			// }
			// TODO: ***** TEMP ***** end

			Leg leg = itinerary.getLeg().get(i);

			/*
			 * transport types
			 */
			Transport transp = leg.getTransport();
			TType tType = transp.getType();
			if (tType.equals(TType.BUS)) {
				String line = transp.getRouteShortName();
				imgv = Utils.getImageByLine(getContext(), line);
			} else {
				imgv = Utils.getImageByTType(getContext(), tType);
			}

			if (imgv != null && imgv.getBackground() != null || imgv.getDrawable() != null) {
				holder.transportTypes.addView(imgv);
			}

			// parking!
			if (tType.equals(TType.CAR) && leg.getTo().getStopId() != null) {
				String price = null;
				if (leg.getTo().getStopId().getExtra() != null && leg.getTo().getStopId().getExtra().containsKey(ParkingsHelper.PARKING_EXTRA_COST)) {
					Map<String,Object> costData = (Map<String, Object>) leg.getTo().getStopId().getExtra().get(ParkingsHelper.PARKING_EXTRA_COST);
					price = (String)costData.get(ParkingsHelper.PARKING_EXTRA_COST_FIXED);
				}
				imgv = Utils.getImageForParkingStation(getContext(), price);
				holder.transportTypes.addView(imgv);
			}

			/*
			 * parking time
			 */
			Integer parkingSearchTimeMin = null;
			Integer parkingSearchTimeMax = null;
			if (leg.getExtra() != null && leg.getExtra().containsKey(ParkingsHelper.PARKING_EXTRA_SEARCHTIME)) {
				@SuppressWarnings("unchecked")
				Map<String, Object> searchTime = (Map<String, Object>) leg.getExtra().get(
						ParkingsHelper.PARKING_EXTRA_SEARCHTIME);
				if (searchTime.containsKey(ParkingsHelper.PARKING_EXTRA_SEARCHTIME_MIN)) {
					parkingSearchTimeMin = (Integer) searchTime.get(ParkingsHelper.PARKING_EXTRA_SEARCHTIME_MIN);
				}
				if (searchTime.containsKey(ParkingsHelper.PARKING_EXTRA_SEARCHTIME_MAX)) {
					parkingSearchTimeMax = (Integer) searchTime.get(ParkingsHelper.PARKING_EXTRA_SEARCHTIME_MAX);
				}
			}
			String parkingSearchTimeString = "";
			if (parkingSearchTimeMin != null && parkingSearchTimeMin > 0) {
				parkingSearchTimeString += parkingSearchTimeMin + "'";
			}
			if (parkingSearchTimeMax != null && parkingSearchTimeMax > 0) {
				parkingSearchTimeString += (parkingSearchTimeString.length() > 0 ? "-" + parkingSearchTimeMax
						: parkingSearchTimeMax) + "'";
			}
			if (parkingSearchTimeString.length() > 0) {
				holder.parkingTime.setText(context.getString(R.string.step_parking_search, parkingSearchTimeString));
				holder.parkingTime.setVisibility(View.VISIBLE);
			} else {
				holder.parkingTime.setVisibility(View.GONE);
			}

			/*
			 * parking cost
			 */
			String parkingCost = "";
			// TODO: ***** TEMP *****
			// parkingCost = "0,80 euro/h";
			// TODO: ***** TEMP ***** end
			if (leg.getExtra() != null && leg.getExtra().containsKey(ParkingsHelper.PARKING_EXTRA_COST)) {
				Map<String,Object> costData = (Map<String, Object>) leg.getExtra().get(ParkingsHelper.PARKING_EXTRA_COST);
				parkingCost = (String)costData.get(ParkingsHelper.PARKING_EXTRA_COST_FIXED);
			}
			if (parkingCost.length() > 0) {
				holder.parkingCost.setText(context.getString(R.string.step_parking_cost, parkingCost));
				holder.parkingCost.setVisibility(View.VISIBLE);
			} else {
				holder.parkingCost.setVisibility(View.GONE);
			}

			/*
			 * alert
			 */
			if (Utils.containsAlerts(leg)) {
				holder.alert.setVisibility(View.VISIBLE);
			} else {
				holder.alert.setVisibility(View.GONE);
			}
		}

		return row;
	}

	static class RowHolder {
		TextView timeFrom;
		TextView timeTo;
		// TextView time;
		LinearLayout transportTypes;
		TextView parkingTime;
		TextView parkingCost;
		ImageView alert;
	}
}
