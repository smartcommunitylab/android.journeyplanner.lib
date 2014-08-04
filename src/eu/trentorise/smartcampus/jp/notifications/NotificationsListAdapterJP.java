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
package eu.trentorise.smartcampus.jp.notifications;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.jp.helper.Utils;
import eu.trentorise.smartcampus.pushservice.PushNotification;

public class NotificationsListAdapterJP extends ArrayAdapter<PushNotification> {

	private Context mContext;
	private int layoutResourceId;

	private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

	public NotificationsListAdapterJP(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.mContext = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Holder holder = null;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new Holder();
			holder.title = (TextView) row.findViewById(R.id.notification_title);
			holder.desc = (TextView) row.findViewById(R.id.notification_desc);
			holder.read = (ImageView) row.findViewById(R.id.notification_read);
			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}

		PushNotification notification = getItem(position);

		if (notification.isRead()) {
			holder.read.setVisibility(View.INVISIBLE);
		} else {
			holder.read.setVisibility(View.VISIBLE);
		}

		buildHolder(holder, notification);

		return row;
	}

	private class Holder {
		public TextView title;
		public TextView desc;
		public ImageView read;
	}

	/*
	 * Builders
	 */
	private void buildHolder(Holder holder, PushNotification notification) {

		// title
		holder.title.setText(mContext.getString(R.string.notifications_itinerary_delay_title, notification.getTitle()));

		// description
		StringBuilder description = new StringBuilder();

		// delay
		if (notification.getDelay() != null && notification.getDelay() > 0) {
			int minutes = notification.getDelay() / 60000;
			if (minutes == 1) {
				description.append(mContext.getString(R.string.notifications_itinerary_delay_min, minutes));
			} else {
				description.append(mContext.getString(R.string.notifications_itinerary_delay_mins, minutes));
			}
		} else if (notification.getDelay() == 0) {
			description.append(mContext.getString(R.string.notifications_itinerary_on_time));
		}

		// line/train (with train number) and direction
		String line = notification.getRouteShortName();
		if (line != null && line.length() > 0) {
			description.append("\n");
			
			//TODO little hack for the missing direction
			String direction="";
			if (RoutesHelper.AGENCYIDS_BUSES.contains(notification.getAgencyId())) {
				description.append(mContext.getString(R.string.notifications_itinerary_delay_bus, line, direction));
			} else if (RoutesHelper.AGENCYIDS_TRAINS.contains(notification.getAgencyId())) {
				String train = line;
				if (notification.getTripId() != null) {
					train += " " + notification.getTripId();
				}
				description.append(mContext.getString(R.string.notifications_itinerary_delay_train, train, direction));
			}
		}

		//TODO something is missing from the model
		// original data
//		if (originalFromTime != null && stopName != null) {
//			Calendar origCal = Calendar.getInstance();
//			origCal.setTimeInMillis(originalFromTime);
//			String originalFromTimeString = timeFormat.format(origCal.getTime());
//			description.append("\n");
//			description.append(mContext.getString(R.string.notifications_itinerary_delay_original_schedule,
//					originalFromTimeString, stopName));
//		}

		holder.desc.setText(description.toString());
	}

}
