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

import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Stop;
import it.sayservice.platform.smartplanner.data.message.otpbeans.StopTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.Utils;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class GetBroadcastDataProcessor extends
		AbstractAsyncTaskProcessor<String, Map<String, List<?>>> {

	private static final String ROUTES = "routes";
	private static final String STOPS = "stops";
	private static final String STOPTIMES = "stoptimes";

	private ArrayAdapter<Route> routesAdapter;
	private ArrayAdapter<Stop> stopsAdapter;
	private ArrayAdapter<StopTime> stopTimesAdapter;

	private Context ctx;

	public GetBroadcastDataProcessor(SherlockFragmentActivity activity,
			ArrayAdapter<Route> routesAdapter, ArrayAdapter<Stop> stopsAdapter,
			ArrayAdapter<StopTime> stopTimesAdapter) {
		super(activity);
		ctx = activity.getApplicationContext();
		this.routesAdapter = routesAdapter;
		this.stopsAdapter = stopsAdapter;
		this.stopTimesAdapter = stopTimesAdapter;
	}

	@Override
	public Map<String, List<?>> performAction(String... params)
			throws SecurityException, Exception {
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
			List<Route> routesList = (List<Route>) JPHelper
					.getRoutesByAgencyId(agencyId, JPHelper.getAuthToken(ctx));
			map.put(ROUTES, routesList);
			routeId = routesList.get(0).getId().getId();
		}

		if (stopId == null) {
			List<Stop> stopsList = (List<Stop>) JPHelper
					.getStopsByAgencyIdRouteId(agencyId, routeId,
							JPHelper.getAuthToken(ctx));
			map.put(STOPS, stopsList);
			stopId = stopsList.get(0).getId();
		}

		List<StopTime> stopTimesList = (List<StopTime>) JPHelper
				.getStopTimesByAgencyIdRouteIdStopId(agencyId, routeId, stopId,
						JPHelper.getAuthToken(ctx));
		map.put(STOPTIMES, stopTimesList);

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
			for (StopTime st : (List<StopTime>) result.get(STOPTIMES)) {
				stopTimesAdapter.add(st);
			}
		}
	}
}
