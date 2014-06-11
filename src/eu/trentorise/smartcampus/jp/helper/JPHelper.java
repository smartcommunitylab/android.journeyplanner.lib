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
package eu.trentorise.smartcampus.jp.helper;

import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;
import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
import it.sayservice.platform.smartplanner.data.message.journey.RecurrentJourney;
import it.sayservice.platform.smartplanner.data.message.journey.SingleJourney;
import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;
import it.sayservice.platform.smartplanner.data.message.otpbeans.GeolocalizedStopRequest;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Stop;
import it.sayservice.platform.smartplanner.data.message.otpbeans.StopTime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.Constants;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.android.common.LocationHelper;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.data.BasicAlert;
import eu.trentorise.smartcampus.jp.custom.data.BasicRecurrentJourneyParameters;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;
import eu.trentorise.smartcampus.jp.model.ParkingSerial;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;
import eu.trentorise.smartcampus.jp.timetable.CompressedTTHelper;
import eu.trentorise.smartcampus.mobilityservice.MobilityAlertService;
import eu.trentorise.smartcampus.mobilityservice.MobilityDataService;
import eu.trentorise.smartcampus.mobilityservice.MobilityPlannerService;
import eu.trentorise.smartcampus.mobilityservice.MobilityServiceException;
import eu.trentorise.smartcampus.mobilityservice.MobilityUserService;
import eu.trentorise.smartcampus.mobilityservice.model.BasicItinerary;
import eu.trentorise.smartcampus.mobilityservice.model.BasicRecurrentJourney;
import eu.trentorise.smartcampus.mobilityservice.model.Delay;
import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;
import eu.trentorise.smartcampus.mobilityservice.model.TripData;
import eu.trentorise.smartcampus.network.RemoteConnector;
import eu.trentorise.smartcampus.network.RemoteConnector.CLIENT_TYPE;
import eu.trentorise.smartcampus.network.RemoteException;
import eu.trentorise.smartcampus.profileservice.BasicProfileService;
import eu.trentorise.smartcampus.profileservice.model.AccountProfile;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.sync.SyncStorage;
import eu.trentorise.smartcampus.storage.sync.SyncStorageWithPaging;


public class JPHelper {

	private static JPHelper instance = null;

	private static SCAccessProvider accessProvider = null;

	private static Context mContext;

	private ProtocolCarrier protocolCarrier = null;

	private static LocationHelper mLocationHelper;

	private SyncStorageWithPaging storage = null;

	public static final String MOBILITY_URL = "/core.mobility";
	private static final String TERRITORY_URL = "/core.territory";
	public static final String MY_ITINERARIES = "my itineraries";
	public static final String MY_RECURRENTJOURNEYS = "my recurrent journeys";
	public static final String IS_ANONYMOUS = "is anonymous";
	private static AccountProfile ap = null;

	// tutorial's stuff

	private static final String TUT_PREFS = "jp_tut_prefs";
	private static final String TOUR_PREFS = "jp_wantTour";
	private static final String FIRST_LAUNCH_PREFS = "jp_firstLaunch";

	public static enum Tutorial {
		PLAN("planTut"), MONITOR("monitorTut"), WATCH("watchTut"), NOTIF("notifTut"), SEND("sendTut"), INFO("infoTut"), PREFST(
				"prefsTut");
		/**
		 * @param text
		 */
		private Tutorial(final String text) {
			this.text = text;
		}

		private final String text;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return text;
		}
	}

	protected JPHelper(final Context mContext) {
		super();
		JPHelper.mContext = mContext;
		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO) {
			RemoteConnector.setClientType(CLIENT_TYPE.CLIENT_WILDCARD);
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				CompressedTTHelper.init(mContext);

			}
		}).start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				MapManager.initWithParams();

			}
		}).start();

		setProtocolCarrier(new ProtocolCarrier(mContext, JPParamsHelper.getAppToken()));

		setLocationHelper(new LocationHelper(mContext));
	}

	// public static void init(final Context mContext) {
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// JPParamsHelper.init(mContext);
	// }
	// }).start();
	// instance = new JPHelper(mContext);
	// RoutesDBHelper.init(mContext);
	// }

	public static void init(final Context mContext) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				JPParamsHelper.init(mContext);
			}
		}).start();
		instance = new JPHelper(mContext);
		RoutesDBHelper.init(mContext);
		getUserProfileInit();
	}

	public static void getUserProfileInit() {

		readAccountProfile(null);

	}

	public static boolean isInitialized() {
		return instance != null;
	}

	public static void readAccountProfile(AsyncTask task) {
		try {
			new GetAPAsncTask().execute(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class GetAPAsncTask extends AsyncTask<AsyncTask, Void, AccountProfile> {

		AsyncTask copyTask = null;

		@Override
		protected AccountProfile doInBackground(AsyncTask... params) {
			BasicProfileService bps;
			if (params[0] != null)
				copyTask = params[0];
			try {
				bps = new BasicProfileService(Constants.getAuthUrl(mContext));
				return bps.getAccountProfile(getAuthToken(mContext));

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(AccountProfile result) {
			ap = result;
			if (copyTask != null)
				copyTask.execute();
		};

	}

	public static List<Itinerary> planSingleJourney(SingleJourney sj, String authToken)
			throws MobilityServiceException, ProtocolException {

		if (sj != null) {
			MobilityPlannerService plannerService = new MobilityPlannerService(GlobalConfig.getAppUrl(mContext)
					+ MOBILITY_URL);
			return plannerService.planSingleJourney(sj, authToken);
		}

		return null;
	}

	public static void saveItinerary(BasicItinerary bi, String authToken) throws ProtocolException,
			MobilityServiceException {
		if (bi != null) {
			MobilityUserService userService = new MobilityUserService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
			userService.saveSingleJourney(bi, authToken);
		}
	}

	/**
	 * 
	 * @return a list with all saved journeys
	 */
	public static List<BasicItinerary> getMyItineraries(String authToken) throws ProtocolException,
			MobilityServiceException {
		MobilityUserService userService = new MobilityUserService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		return userService.getSingleJourneys(authToken);
	}

	public static List<Delay> getDelay(String routeId, long from_time, long to_time, String authToken)
			throws ProtocolException, MobilityServiceException {
		MobilityDataService dataService = new MobilityDataService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		return dataService.getDelays(routeId, authToken);
	}

	// TODO old code
	// public static List<List<Map<String, String>>> getDelay(String routeId,
	// long from_time, long to_time) throws ConnectionException,
	// ProtocolException, SecurityException, JSONException,
	// JsonParseException, JsonMappingException, IOException {
	// String url = Config.TARGET_ADDRESS
	// + Config.CALL_GET_DELAY_TIME_BY_ROUTE + "/" + routeId + "/"
	// + from_time + "/" + to_time;
	//
	// MessageRequest req = new MessageRequest(
	// GlobalConfig.getAppUrl(JPHelper.mContext), url);
	// req.setMethod(Method.GET);
	// req.setQuery("complex=true");
	//
	// MessageResponse res = JPHelper.instance.getProtocolCarrier()
	// .invokeSync(req, JPParamsHelper.getAppToken(), getAuthToken());
	//
	// return eu.trentorise.smartcampus.android.common.Utils
	// .convertJSONToObject(res.getBody(), TimeTable.class)
	// .getDelays();
	// }

	/**
	 * Delete single journeys
	 * 
	 * @param id
	 *            of the route
	 */
	public static void deleteMyItinerary(String id, String authToken) throws ProtocolException,
			MobilityServiceException {
		if (id != null && id.length() > 0) {
			MobilityUserService userService = new MobilityUserService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
			userService.deleteSingleJourney(id, authToken);
		}
	}

	public static boolean monitorMyItinerary(boolean monitor, String id, String authToken) throws ProtocolException,
			MobilityServiceException {
		MobilityUserService userService = new MobilityUserService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		return userService.monitorSingleJourney(id, monitor, authToken);
	}

	public static Map<String, CacheUpdateResponse> getCacheStatus(Map<String, String> agencyIdsVersions,
			String authToken) throws ProtocolException, SecurityException, RemoteException {
		MobilityDataService dataService = new MobilityDataService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		return dataService.getCacheStatus(agencyIdsVersions, authToken);
	}

	public static CompressedTransitTimeTable getCacheUpdate(String agencyId, String fileName, String authToken)
			throws ProtocolException, SecurityException, RemoteException {
		MobilityDataService dataService = new MobilityDataService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		return dataService.getCachedTimetable(agencyId, fileName, authToken);
	}

	public static boolean monitorMyRecItinerary(boolean monitor, String id, String authToken) throws ProtocolException,
			MobilityServiceException {
		MobilityUserService userService = new MobilityUserService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		return userService.monitorRecurrentJourney(id, monitor, authToken);

	}

	/*
	 * BUS
	 */
	public static List<Route> getRoutesByAgencyId(String agencyId, String authToken) throws ProtocolException,
			MobilityServiceException {
		MobilityDataService dataService = new MobilityDataService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		return dataService.getRoutes(agencyId, authToken);
	}

	public static List<SmartLine> getSmartLinesByAgencyId(String agencyId) throws ConnectionException,
			ProtocolException, SecurityException, JsonParseException, JsonMappingException, IOException {

		List<Route> list = new ArrayList<Route>();
		Resources resources = mContext.getResources();
		String[] lines = resources.getStringArray(R.array.smart_check_12_numbers);
		TypedArray icons = resources.obtainTypedArray(R.array.smart_check_12_icons);
		TypedArray colors = resources.obtainTypedArray(R.array.smart_check_12_colors);

		// get info from result (busRoutes)
		Map<String, List<String>> singleRoutesShorts = new HashMap<String, List<String>>();
		Map<String, List<String>> singleRoutesLong = new HashMap<String, List<String>>();
		Map<String, List<String>> singleRoutesId = new HashMap<String, List<String>>();
		ArrayList<SmartLine> busLines = new ArrayList<SmartLine>();
		// //prepare the request
		// MessageRequest req = new MessageRequest(
		// GlobalConfig.getAppUrl(instance.mContext),
		// Config.TARGET_ADDRESS + Config.CALL_BUS_ROUTES + "/" + agencyId);
		// req.setMethod(Method.GET);
		//
		// MessageResponse res =
		// JPHelper.instance.getProtocolCarrier().invokeSync(req,
		// JPParamsHelper.getAppToken(), getAuthToken());
		//
		// //get alle the routes and order them
		// List<?> routes =
		// JSONUtils.getFullMapper().readValue(res.getBody(),List.class);
		// for (Object r : routes) {
		// Route route = JSONUtils.getFullMapper().convertValue(r, Route.class);
		// list.add(route);
		// }
		list = RoutesHelper.getRoutesList(mContext, new String[] { agencyId });
		Collections.sort(list, Utils.getRouteComparator());

		// get all-the-routes for a smartline
		for (int index = 0; index < lines.length; index++) {
			// put them in the array
			for (Route route : list) {
				//
				if ((route.getId().getId().toUpperCase().compareTo(lines[index].toUpperCase()) == 0)
						|| route.getId().getId().toUpperCase().compareTo(lines[index].toUpperCase() + "R") == 0
						|| route.getId().getId().toUpperCase().compareTo(lines[index].toUpperCase() + "A") == 0) {
					if (singleRoutesShorts.get(lines[index]) == null) {
						singleRoutesShorts.put(lines[index], new ArrayList<String>());
						singleRoutesLong.put(lines[index], new ArrayList<String>());
						singleRoutesId.put(lines[index], new ArrayList<String>());

					}
					singleRoutesShorts.get(lines[index]).add(route.getRouteShortName());
					singleRoutesLong.get(lines[index]).add(route.getRouteLongName());
					singleRoutesId.get(lines[index]).add(route.getId().getId());

				}
			}
			SmartLine singleLine = new SmartLine(icons.getDrawable(index), lines[index], colors.getColor(index, 0),
					singleRoutesShorts.get(lines[index]), singleRoutesLong.get(lines[index]),
					singleRoutesId.get(lines[index]));
			busLines.add(singleLine);
		}
		return busLines;
	}

	public static List<Stop> getStopsByAgencyIdRouteId(String agencyId, String routeId, String authToken)
			throws ProtocolException, MobilityServiceException {
		MobilityDataService dataService = new MobilityDataService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		return dataService.getStops(agencyId, routeId, authToken);
	}

	public static List<StopTime> getStopTimesByAgencyIdRouteIdStopId(String agencyId, String routeId, String stopId,
			String authToken) throws ProtocolException, MobilityServiceException {
		MobilityDataService dataService = new MobilityDataService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		List<StopTime> res = dataService.getStopTimes(agencyId, routeId, stopId, authToken);
		for (StopTime st : res)
			st.setTime(st.getTime() * 1000);

		return res;
	}

	/*
	 * Alerts
	 */
	public static void submitAlert(BasicAlert ba, String authToken) throws ProtocolException, MobilityServiceException {
		if (ba != null) {
			MobilityAlertService alertService = new MobilityAlertService(GlobalConfig.getAppUrl(mContext)
					+ MOBILITY_URL);
			alertService.sendUserAlert(ba.getContent(), authToken);
		}
	}

	public static String getAuthToken(Context ctx) throws AACException {
		return JPHelper.getAccessProvider().readToken(ctx);
	}

	public static JPHelper getInstance() throws DataException {
		if (instance == null)
			throw new DataException("JPHelper is not initialized");
		return instance;
	}

	public static SCAccessProvider getAccessProvider() {
		if (accessProvider == null)
			accessProvider = SCAccessProvider.getInstance(mContext);
		return accessProvider;
	}

	public static void endAppFailure(Activity activity, int id) {
		Toast.makeText(activity, activity.getResources().getString(id), Toast.LENGTH_LONG).show();
		activity.finish();
	}

	public static void showFailure(Activity activity, int id) {
		Toast.makeText(activity, activity.getResources().getString(id), Toast.LENGTH_LONG).show();
	}

	public ProtocolCarrier getProtocolCarrier() {
		return protocolCarrier;
	}

	public void setProtocolCarrier(ProtocolCarrier protocolCarrier) {
		this.protocolCarrier = protocolCarrier;
	}

	public static LocationHelper getLocationHelper() {
		if (JPHelper.mLocationHelper == null) {
			setLocationHelper(new LocationHelper(mContext));
		}
		return JPHelper.mLocationHelper;
	}

	public static void setLocationHelper(LocationHelper mLocationHelper) {
		JPHelper.mLocationHelper = mLocationHelper;
	}

	public static void deleteMyRecurItinerary(String id, String authToken) throws MobilityServiceException,
			ProtocolException {
		if (id != null && id.length() > 0) {
			MobilityUserService userService = new MobilityUserService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
			userService.deleteRecurrentJourney(id, authToken);
		}
	}

	public static Object getItineraryObject(String objectId, String authToken) throws ProtocolException,
			MobilityServiceException {
		if (objectId != null) {
			MobilityUserService userService = new MobilityUserService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
			BasicItinerary journey = userService.getSingleJourney(objectId, authToken);
			if (journey == null)
				return userService.getRecurrentJourney(objectId, authToken);
			else
				return journey;

		}
		return null;
	}

	public class JPLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	public static RecurrentJourney planRecurItinerary(BasicRecurrentJourneyParameters brj, String authToken)
			throws ProtocolException, MobilityServiceException {
		if (brj != null) {
			MobilityPlannerService plannerService = new MobilityPlannerService(GlobalConfig.getAppUrl(mContext)
					+ MOBILITY_URL);
			return plannerService.planRecurrentJourney(brj.getData(), authToken);
		}
		return null;
	}

	public static Boolean saveMyRecurrentJourney(BasicRecurrentJourney brj, String authToken) throws ProtocolException,
			MobilityServiceException {
		if (brj != null) {
			MobilityUserService userService = new MobilityUserService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
			if (brj.getClientId() == null) {
				userService.saveRecurrentJourney(brj, authToken);
			} else {
				userService.updateRecurrentJourney(brj, brj.getClientId(), authToken);
			}
			return true;
		}
		return false;
	}

	public static List<BasicRecurrentJourney> getMyRecurItineraries(String authToken) throws ProtocolException,
			MobilityServiceException {
		MobilityUserService userService = new MobilityUserService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		return userService.getRecurrentJourneys(authToken);
	}

	public static TimeTable getTransitTimeTableById(long from_day, long to_day, String routeId, String authToken)
			throws ProtocolException, MobilityServiceException {
		MobilityDataService dataService = new MobilityDataService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		if (routeId != null) {
			return dataService.getTimeTable(routeId, from_day, authToken);
		}
		return null;
	}

	// da rifare

	public static List<SmartCheckStop> getStops(String[] agencyIds, double[] location, double radius, String authToken)
			throws Exception {

		if (agencyIds == null || agencyIds.length == 0) return Collections.emptyList();
		
		MobilityDataService mobilityDataService = new MobilityDataService(GlobalConfig.getAppUrl(mContext)
				+ MOBILITY_URL);

		//set filter's options
		GeolocalizedStopRequest geoLocStop = new GeolocalizedStopRequest();
		if (location != null) {
			geoLocStop.setCoordinates(new double[] { location[0], location[1] });
			// radius is in meter. As for interface, the radius unit corresponds
			// to ~ 100 km
			geoLocStop.setRadius(radius / 100000);
		}

		List<Stop> geoStops = new ArrayList<Stop>();
		for (String agencyId : agencyIds) {
			geoLocStop.setAgencyId(agencyId);
			geoStops.addAll(mobilityDataService.getGeolocalizedStops(geoLocStop, authToken));
		}

		List<SmartCheckStop> returnGeoStops = new ArrayList<SmartCheckStop>();
		for (Stop stop : geoStops) {
			SmartCheckStop singleStop = new SmartCheckStop();
			singleStop = fromSmartCheckStopToStop(stop, agencyIds[0]);
			returnGeoStops.add(singleStop);
		}
		return returnGeoStops;
	}

	private static SmartCheckStop fromSmartCheckStopToStop(Stop stop, String agencyId) {
		SmartCheckStop singleStop = new SmartCheckStop();
		singleStop.setTitle(stop.getName());
		singleStop.setId(stop.getId());
		singleStop.setLocation(new double[] { stop.getLatitude(), stop.getLongitude() });
		HashMap<String, Object> customData = new HashMap<String, Object>();
		customData.put("agencyId", agencyId);
		customData.put("id", stop.getId());
		singleStop.setCustomData(customData);
		return singleStop;
	}

	public static List<TripData> getTrips(SmartCheckStop stop, String authToken) throws Exception {

		MobilityDataService dataService = new MobilityDataService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		return dataService.getNextTrips(stop.getCustomData().get("agencyId").toString(), stop.getCustomData().get("id")
				.toString(), 3, authToken);
	}

	public static List<ParkingSerial> getParkings(String parkingAgencyId, String authToken) throws Exception {
		MobilityDataService dataService = new MobilityDataService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		return convertParkings(dataService.getParkings(parkingAgencyId, authToken));
	}

	public static List<ParkingSerial> convertParkings(List<Parking> l) {
		List<ParkingSerial> out = new ArrayList<ParkingSerial>();
		for (Parking obj : l) {
			ParkingSerial toAdd = new ParkingSerial();
			toAdd.setName(obj.getName());
			toAdd.setDescription(obj.getDescription());
			toAdd.setMonitored(obj.isMonitored());
			toAdd.setPosition(obj.getPosition());
			toAdd.setSlotsAvailable(obj.getSlotsAvailable());
			toAdd.setSlotsTotal(obj.getSlotsTotal());
			out.add(toAdd);
		}
		return out;
	}

	public static List<AlertRoadLoc> getAlertRoads(String agencyId, long fromTime, long toTime, String authToken)
			throws Exception {
		MobilityDataService dataService = new MobilityDataService(GlobalConfig.getAppUrl(mContext) + MOBILITY_URL);
		return convertAlertRoad(dataService.getRoadInfo(agencyId, fromTime, toTime, authToken));
	}

	public static List<AlertRoadLoc> convertAlertRoad(List<AlertRoad> roadInfo) {
		List<AlertRoadLoc> out = new ArrayList<AlertRoadLoc>();
		for (AlertRoad ar : roadInfo) {
			out.add(new AlertRoadLoc(ar));
		}
		return out;
	}

	public static SyncStorage getSyncStorage() throws DataException {
		return getInstance().storage;
	}

	public static SharedPreferences getTutorialPreferences(Context ctx) {
		SharedPreferences out = ctx.getSharedPreferences(TUT_PREFS, Context.MODE_PRIVATE);
		return out;
	}

	public static boolean isFirstLaunch(Context ctx) {
		return getTutorialPreferences(ctx).getBoolean(FIRST_LAUNCH_PREFS, true);
	}

	public static void disableFirstLaunch(Context ctx) {
		Editor edit = getTutorialPreferences(ctx).edit();
		edit.putBoolean(FIRST_LAUNCH_PREFS, false);
		edit.commit();
	}

	public static boolean wantTour(Context ctx) {
		return getTutorialPreferences(ctx).getBoolean(TOUR_PREFS, false);
	}

	public static void setWantTour(Context ctx, boolean want) {
		Editor edit = getTutorialPreferences(ctx).edit();
		edit.putBoolean(TOUR_PREFS, want);
		edit.commit();
	}

	public static boolean isTutorialShowed(Context ctx, Tutorial t) {
		return getTutorialPreferences(ctx).getBoolean(t.toString(), false);
	}

	public static void setTutorialVisibility(Context ctx, Tutorial t, boolean visibility) {
		Editor edit = getTutorialPreferences(ctx).edit();
		edit.putBoolean(t.toString(), visibility);
		edit.commit();
	}

	public static void resetTutorialPreferences(Context ctx) {
		for (Tutorial t : Tutorial.values()) {
			setTutorialVisibility(mContext, t, false);
		}
	}

	public static void setTutorialAsShowed(Context ctx, Tutorial t) {
		Editor edit = getTutorialPreferences(ctx).edit();
		edit.putBoolean(t.toString(), true);
		edit.commit();
	}

	/**
	 * With this method you can get the last tutorial that was not showed
	 * 
	 * @param ctx
	 *            the activity
	 * @return the last Tutorial not showed to the user otherwise null
	 */
	public static Tutorial getLastTutorialNotShowed(Context ctx) {
		for (Tutorial t : Tutorial.values()) {
			if (!isTutorialShowed(ctx, t))
				return t;
		}
		return null;
	}

	/*
	 * promote the user from anonymous
	 */
	public static void userPromote(Activity activity) {
		new PromoteTask(activity).execute();

	}

	/*
	 * check if user us anonymous -> sharedPref has IS_ANONYMOUS.By default if
	 * not present is snonymous
	 */
	public static boolean isUserAnonymous(Activity activity) {
		SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.app_name),
				Context.MODE_PRIVATE);
		if (sharedPref.contains(IS_ANONYMOUS))
			return sharedPref.getBoolean(IS_ANONYMOUS, true);
		else
			return true;

	}

	/*
	 * set anonymous value -> sharedPref has IS_ANONYMOUS
	 */
	public static void setUserAnonymous(Activity activity, boolean value) {
		SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.app_name),
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(JPHelper.IS_ANONYMOUS, value);
		editor.commit();
	}

	public static Map<String, List<String>> getLocationFromUrl(String string) {
		try {
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			String[] urlParts = string.split("\\?");
			if (urlParts.length > 1) {
				String query = urlParts[1];
				for (String param : query.split("&")) {
					String[] pair = param.split("=");
					String key = URLDecoder.decode(pair[0], "UTF-8");
					String value = "";
					if (pair.length > 1) {
						value = URLDecoder.decode(pair[1], "UTF-8");
					}

					List<String> values = params.get(key);
					if (values == null) {
						values = new ArrayList<String>();
						params.put(key, values);
					}
					values.add(value);
				}
			}

			return params;
		} catch (UnsupportedEncodingException ex) {
			throw new AssertionError(ex);
		}
	}
}
