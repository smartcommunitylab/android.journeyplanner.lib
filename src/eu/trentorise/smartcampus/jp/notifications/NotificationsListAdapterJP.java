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
import eu.trentorise.smartcampus.jp.notifications.NotificationBuilder.JPNotificationBean;

public class NotificationsListAdapterJP extends ArrayAdapter<Notification> {

	private int layoutResourceId;
	private Context mContext;

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
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new Holder();
			holder.title = (TextView) row.findViewById(R.id.notification_title);
			holder.desc = (TextView) row.findViewById(R.id.notification_desc);
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

	public class Holder {
		public TextView title;
		public TextView desc;
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
		String type = (String)content.get("type");
		String journeyName = notification.getTitle();
		String agencyId = (String) content.get("agencyId");
		Integer delay = (Integer) content.get("delay"); // milliseconds
		String routeId = "?";
		if (content.get("routeShortName") != null) {
			routeId = (String) content.get("routeShortName");
		} else if (content.get("routeId") != null) {
			routeId = (String) content.get("routeId");
		}
		String tripId = (String) content.get("tripId");
//		String direction = (String) content.get("direction");
		Long originalFromTime = (Long) content.get("from"); // milliseconds
		String stopName = (String) content.get("station");
		String placesAvailable = (String) content.get("placesAvailable");
		
		JPNotificationBean bean = NotificationBuilder.buildNotification(mContext, 
				type,
				journeyName, 
				delay, 
				agencyId, 
				routeId, 
				tripId, 
//				direction, 
				originalFromTime, 
				stopName,
				placesAvailable);
		if (bean == null) {
			bean = new JPNotificationBean();
			bean.title = notification.getTitle();
			bean.description = notification.getDescription();
		}
		
		// transport type icon
//		holder.ttype.setVisibility(View.GONE);
//		if (agencyId != null) {
//			ImageView imgv = Utils.getImageByAgencyId(getContext(),
//					Integer.parseInt((String) notification.getContent().get("agencyId")));
//
//			if (imgv.getDrawable() != null) {
//				holder.ttype.setImageDrawable(imgv.getDrawable());
//				holder.ttype.setVisibility(View.VISIBLE);
//			}
//		}

		// title
		if (bean.title != null && bean.title.length() != 0) {
			holder.title.setText(bean.title);
		}
		if (bean.description != null && bean.description.length() != 0) {
			holder.desc.setText(bean.description);
		}
	}

}
