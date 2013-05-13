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

public class NotificationsListAdapterJP extends ArrayAdapter<Notification> {

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
			holder.ttype = (ImageView) row.findViewById(R.id.notification_ttype);
			holder.read = (ImageView) row.findViewById(R.id.notification_read);
			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}

		Notification notification = getItem(position);

		if (notification.isReaded()) {
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
		public ImageView ttype;
		public ImageView read;
	}

	/*
	 * Builders
	 */
	private void buildHolder(Holder holder, Notification notification) {
		// missing custom data
		if (notification.getContent() == null) {
			String title = notification.getTitle();
			String description = notification.getDescription();
			holder.title.setText(title != null ? title : "Title");
			holder.desc.setText(description != null ? description : "Description");
			return;
		}

		Map<String, Object> content = notification.getContent();
		String journeyName = notification.getTitle();
		Integer agencyId = Integer.parseInt((String) content.get("agencyId"));
		Integer delay = (Integer) content.get("delay"); // milliseconds
		String line = "?";
		if (content.get("routeShortName") != null) {
			line = (String) content.get("routeShortName");
		} else if (content.get("routeId") != null) {
			line = (String) content.get("routeId");
		}
		String tripId = (String) content.get("tripId");
		String direction = (String) content.get("direction");
		Long originalFromTime = (Long) content.get("from"); // milliseconds
		String stopName = (String) content.get("station");

		// transport type icon
		holder.ttype.setVisibility(View.GONE);
		if (agencyId != null) {
			ImageView imgv = Utils.getImageByAgencyId(getContext(),
					Integer.parseInt((String) notification.getContent().get("agencyId")));

			if (imgv.getDrawable() != null) {
				holder.ttype.setImageDrawable(imgv.getDrawable());
				holder.ttype.setVisibility(View.VISIBLE);
			}
		}

		// title
		if (journeyName != null && journeyName.length() != 0) {
			holder.title.setText(mContext.getString(R.string.notifications_itinerary_delay_title, journeyName));
		}

		// description
		StringBuilder description = new StringBuilder();

		// delay
		if (delay != null && delay > 0) {
			int minutes = delay / 60000;
			if (minutes == 1) {
				description.append(mContext.getString(R.string.notifications_itinerary_delay_min, minutes));
			} else {
				description.append(mContext.getString(R.string.notifications_itinerary_delay_mins, minutes));
			}
		} else if (delay == 0) {
			description.append(mContext.getString(R.string.notifications_itinerary_on_time));
		}

		// line/train (with train number) and direction
		if (line != null && line.length() > 0 && direction != null && direction.length() > 0) {
			description.append("\n");
			if (agencyId == RoutesHelper.AGENCYID_BUS) {
				description.append(mContext.getString(R.string.notifications_itinerary_delay_bus, line, direction));
			} else if (agencyId == RoutesHelper.AGENCYID_TRAIN_TM) {
				String train = line;
				if (tripId != null) {
					train += " " + tripId;
				}
				description.append(mContext.getString(R.string.notifications_itinerary_delay_train, train, direction));
			}
		}

		// original data
		if (originalFromTime != null && stopName != null) {
			Calendar origCal = Calendar.getInstance();
			origCal.setTimeInMillis(originalFromTime);
			String originalFromTimeString = timeFormat.format(origCal.getTime());
			description.append("\n");
			description.append(mContext.getString(R.string.notifications_itinerary_delay_original_schedule,
					originalFromTimeString, stopName));
		}

		holder.desc.setText(description.toString());
	}

}
