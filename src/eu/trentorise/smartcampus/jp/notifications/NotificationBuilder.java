package eu.trentorise.smartcampus.jp.notifications;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;

public class NotificationBuilder {

	private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

	public static class JPNotificationBean {
		public String title;
		public String description;
	}
	
	public static JPNotificationBean buildNotification(
			Context mContext, 
			String type,
			String journeyName,
			Integer delay,
			String agencyId, 
			String routeId, 
			String tripId,
			Long originalFromTime,
			String stopName,
			String placesAvailable) {
		
		if ("alertDelay".equals(type)) {
			JPNotificationBean bean = createDelayNotification(
					mContext,
					journeyName, 
					delay, 
					agencyId, routeId, tripId,
					originalFromTime, 
					stopName);

			return  bean;
		}
		if ("alertParking".equals(type)) {
			JPNotificationBean bean = createParkingNotification(
					mContext, 
					journeyName,
					agencyId,
					stopName,
					placesAvailable);
		}
		return null;
	}

	private static JPNotificationBean createParkingNotification(
			Context mContext, String journeyName, String agencyId,
			String stopName, String placesAvailable) 
	{
		JPNotificationBean bean = new JPNotificationBean();
		
		return bean;
	}

	private static JPNotificationBean createDelayNotification(Context mContext,
			String journeyName, Integer delay, String agencyId, String routeId,
			String tripId, Long originalFromTime, String stopName) {
		JPNotificationBean bean = new JPNotificationBean();

		// title
		if (journeyName != null && journeyName.length() != 0) {
			bean.title = mContext.getString(R.string.notifications_itinerary_delay_title, journeyName);
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
		if (routeId != null && routeId.length() > 0) {
			description.append("\n");
			if (RoutesHelper.AGENCYIDS_BUSES.contains(agencyId)) {
				description.append(mContext.getString(R.string.notifications_itinerary_delay_bus, routeId));
			} else if (RoutesHelper.AGENCYIDS_TRAINS.contains(agencyId)) {
				String train = routeId;
				if (tripId != null) {
					train += " " + tripId;
				}
				description.append(mContext.getString(R.string.notifications_itinerary_delay_train, train));
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

		bean.description = description.toString();
		return bean;
	}
}
