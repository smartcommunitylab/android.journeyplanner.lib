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
package eu.trentorise.smartcampus.jp.helper.processor;

import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Id;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Stop;
import it.sayservice.platform.smartplanner.data.message.otpbeans.StopTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.helper.RoutesDBHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.jp.model.AlertStopTime;
import eu.trentorise.smartcampus.jp.timetable.CompressedTTHelper;
import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class GetBroadcastDataProcessor extends AbstractAsyncTaskProcessor<String, Map<String, List<?>>> {

	private static final String ROUTES = "routes";
	private static final String STOPS = "stops";
	private static final String STOPTIMES = "stoptimes";

	private ArrayAdapter<Route> routesAdapter;
	private ArrayAdapter<Stop> stopsAdapter;
	private ArrayAdapter<AlertStopTime> stopTimesAdapter;

	private Context ctx;

	public GetBroadcastDataProcessor(SherlockFragmentActivity activity, ArrayAdapter<Route> routesAdapter,
			ArrayAdapter<Stop> stopsAdapter, ArrayAdapter<AlertStopTime> stopTimesAdapter) {
		super(activity);
		ctx = activity.getApplicationContext();
		this.routesAdapter = routesAdapter;
		this.stopsAdapter = stopsAdapter;
		this.stopTimesAdapter = stopTimesAdapter;
	}

	@Override
	public Map<String, List<?>> performAction(String... params) throws SecurityException, Exception {
		// 0: agencyId
		// 1: routeId
		// 2: stopId
		String agencyId = null;
		String routeId = null;
		String stopId = null;

		if (params.length == 1) {
			agencyId = params[0];
		} else if (params.length == 2) {
			agencyId = params[0];
			routeId = params[1];
		} else if (params.length == 3) {
			agencyId = params[0];
			routeId = params[1];
			stopId = params[2];
		}

		Map<String, List<?>> map = new HashMap<String, List<?>>();

		if (agencyId == null) {
			return map;
		}

		if (routeId == null) {
			List<Route> routesList = RoutesHelper.getRoutesList(activity, new String[]{agencyId});
//			List<Route> routesList = (List<Route>) JPHelper.getRoutesByAgencyId(agencyId, JPHelper.getAuthToken(ctx));
			map.put(ROUTES, routesList);
			routeId = routesList.get(0).getId().getId();
		}

		CompressedTransitTimeTable ctt = RoutesDBHelper.getTimeTable(CompressedTTHelper.convertMsToDateFormat(System.currentTimeMillis()), agencyId, routeId);
		TimeTable tt = CompressedTTHelper.ctt2tt(ctt);
		if (stopId == null) {
			List<Stop> stopsList = new ArrayList<Stop>();
			for (int i = 0; i < tt.getStopsId().size(); i++) {
				Stop stop = new Stop();
				stop.setId(tt.getStopsId().get(i));
				stop.setName(tt.getStops().get(i).trim());
				stopsList.add(stop);
			}
//			List<Stop> stopsList = (List<Stop>) JPHelper.getStopsByAgencyIdRouteId(agencyId, routeId,
//					JPHelper.getAuthToken(ctx));
			map.put(STOPS, stopsList);
			stopId = stopsList.get(0).getId();
		}
		List<AlertStopTime> stopTimesListRetun = new ArrayList<AlertStopTime>();
		Calendar c = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
		long now = System.currentTimeMillis();
		for (int i = 0; i < tt.getStopsId().size(); i++) {
			if (stopId.equals(tt.getStopsId().get(i))) {
				for (int j = 0; j < tt.getTimes().size(); j++) {
					String timeStr = tt.getTimes().get(j).get(i);
					long timeMillis = 0;
					try {
						Date time = Config.FORMAT_TIME_UI.parse(timeStr);
						c.setTime(time);
						today.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
						today.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
						today.set(Calendar.SECOND, 0);
						today.set(Calendar.MILLISECOND, 0);
						if (today.getTimeInMillis() >  now ||
							today.getTimeInMillis() < now - 1000*60*60) continue;
						timeMillis = today.getTimeInMillis();
					} catch (Exception e) {
						continue;
					}
					AlertStopTime stopTime = new AlertStopTime();
					stopTime.setAgencyId(agencyId);
					stopTime.setTripId(tt.getTripIds().get(j));
					if (tt.getRouteIds() == null || tt.getRouteIds().isEmpty()) {
						stopTime.setRouteId(routeId);
					} else  {
						stopTime.setRouteId(tt.getRouteIds().get(j));
					}
					stopTime.setTime(timeMillis);
					stopTimesListRetun.add(stopTime);
				}
			}
		}
		
//		List<StopTime> stopTimesList = (List<StopTime>) JPHelper.getStopTimesByAgencyIdRouteIdStopId(agencyId, routeId,
//				stopId, JPHelper.getAuthToken(ctx));
//		List<StopTime> stopTimesListRetun = new ArrayList<StopTime>();
//		Date now = new Date();
//		for (StopTime stoptime : stopTimesList) {
//			if (now.getTime() >= stoptime.getTime())
//			{
//				stopTimesListRetun.add(stoptime);
//
//			}
//		}
		map.put(STOPTIMES, stopTimesListRetun);

		return map;
	}

	@Override
	public void handleResult(Map<String, List<?>> result) {
		if (result.get(ROUTES) != null) {
			routesAdapter.clear();
			for (Route r : (List<Route>) result.get(ROUTES)) {
				routesAdapter.add(r);
			}
		}

		if (result.get(STOPS) != null) {
			stopsAdapter.clear();
			for (Stop s : (List<Stop>) result.get(STOPS)) {
				stopsAdapter.add(s);
			}
		}

		if (result.get(STOPTIMES) != null) {
			stopTimesAdapter.clear();
			for (AlertStopTime st : (List<AlertStopTime>) result.get(STOPTIMES)) {
				stopTimesAdapter.add(st);
			}
		}
	}
}
