package eu.trentorise.smartcampus.jp.notifications;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import eu.trentorise.smartcampus.jp.notifications.NotificationBuilder.JPNotificationBean;
import eu.trentorise.smartcampus.pushservice.PushNotification;
import eu.trentorise.smartcampus.pushservice.PushNotificationBuilder;

public class JPPushNotificationBuilder implements PushNotificationBuilder {

	private final static String FIELD_TITLE = "title";
	private final static String FIELD_DESCRIPTION = "description";
	private final static String FIELD_AGENCYID = "content.agencyId";
	private final static String FIELD_ROUTEID = "content.routeId";
	private final static String FIELD_ROUTESHORTNAME = "content.routeShortName";
	private final static String FIELD_TRIPID = "content.tripId";
	private final static String FIELD_DELAY = "content.delay";
	private final static String FIELD_TYPE = "content.type";
	private final static String FIELD_ENTITY = "entities";
	private final static String FIELD_PLACES_AVAILABLE = "content.placesAvailable";
	private final static String FIELD_NUMBER_OF_VEHICLES = "content.noOfvehicles";
	private final static String FIELD_TRANSPORT = "content.transport";
	private final static String FIELD_FROMTIME = "content.from";
	private final static String FIELD_STATION = "content.stopId";

	
	private String getJourneyId(String json) {
		try {
			JSONArray jsarr = new JSONArray(json);
			JSONObject entity = new JSONObject(jsarr.get(0).toString());
			return entity.getString("id");
		} catch (JSONException ex) {

			Log.e(this.getClass().getName(), ex.toString());
			Log.e(this.getClass().getName() + "CONTENT", json.toString());
		}
		return null;
	}

	@Override
	public PushNotification buildNotification(Context ctx, Intent i) {
		String journeyId = getJourneyId(i.getStringExtra(FIELD_ENTITY));
		String route = i.hasExtra(FIELD_ROUTESHORTNAME) ? i.getStringExtra(FIELD_ROUTESHORTNAME) : i.getStringExtra(FIELD_ROUTEID);
		JPNotificationBean bean  = NotificationBuilder.buildNotification(ctx, 
				i.getStringExtra(FIELD_TYPE),
				i.getStringExtra(FIELD_TITLE), 
				i.hasExtra(FIELD_DELAY) ? Integer.parseInt(i.getStringExtra(FIELD_DELAY)) : null,
				i.getStringExtra(FIELD_AGENCYID), 
				route, 
				i.getStringExtra(FIELD_TRIPID), 
				i.hasExtra(FIELD_FROMTIME) ? Long.parseLong(i.getStringExtra(FIELD_FROMTIME)) : null, 
				i.getStringExtra(FIELD_STATION),
				i.getStringExtra(FIELD_PLACES_AVAILABLE),
				i.getStringExtra(FIELD_NUMBER_OF_VEHICLES),
				i.getStringExtra(FIELD_TRANSPORT));
		
		if (bean == null) bean = new JPNotificationBean();
		if (bean.title == null) bean.title = i.getStringExtra(FIELD_TITLE);
		if (bean.description == null) bean.description = i.getStringExtra(FIELD_DESCRIPTION);
		
		return new PushNotification(journeyId, bean.title, bean.description);
	}

}
