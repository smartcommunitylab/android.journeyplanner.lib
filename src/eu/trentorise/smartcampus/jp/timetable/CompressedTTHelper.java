package eu.trentorise.smartcampus.jp.timetable;

import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesDBHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesDBHelper.AgencyDescriptor;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.mobilityservice.model.Delay;

public class CompressedTTHelper {
	/*******************************************************************************
	 * Copyright 2012-2013 Trento RISE
	 * 
	 * Licensed under the Apache License, Version 2.0 (the "License"); you may
	 * not use this file except in compliance with the License. You may obtain a
	 * copy of the License at
	 * 
	 * http://www.apache.org/licenses/LICENSE-2.0
	 * 
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	 * License for the specific language governing permissions and limitations
	 * under the License.
	 ******************************************************************************/

	private static CompressedTTHelper instance = null;
	private static Context mContext;
	private static Map<String, Map<String, String>> calendar;
	private static final String calendarFilename = "calendar.js";
	private static final String indexFilename = "_index.txt";

	protected CompressedTTHelper(Context mContext) {
		super();
		CompressedTTHelper.mContext = mContext;
		calendar = loadCalendars();
	}

	public static CompressedTTHelper getInstance() {
		return instance;
	}

	public static Map<String, Long> getVersionsFromAssets() {
		AssetManager assetManager = mContext.getResources().getAssets();
		InputStream in;
		Map<String, Long> versions = new HashMap<String, Long>();

		for (String agencyId : RoutesHelper.AGENCYIDS) {
			try {
				in = assetManager.open(agencyId + indexFilename);
				String jsonParams = getStringFromInputStream(in);
				Map<String, Object> indexMap = (Map<String, Object>) Utils
						.convertJSONToObject(jsonParams, Map.class);
				versions.put(agencyId,
						Long.parseLong(indexMap.get("version").toString()));
			} catch (IOException e) {
				Log.w(CompressedTTHelper.class.getCanonicalName(),
						e.getMessage());
			}
		}

		return versions;
	}

	public static AgencyDescriptor buildAgencyDescriptorFromAssets(
			String agencyId, long version) {
		AgencyDescriptor agencyDescriptor = null;
		try {
			CacheUpdateResponse cur = new CacheUpdateResponse();
			cur.setVersion(version);
			cur.setCalendar(CompressedTTHelper.getInstance()
					.loadCalendarByAgencyId(agencyId));

			AssetManager assetManager = mContext.getResources().getAssets();
			InputStream in;
			String[] fileNames = assetManager.list(agencyId);

			List<String> lineHashFiles = new ArrayList<String>();
			List<CompressedTransitTimeTable> ctttList = new ArrayList<CompressedTransitTimeTable>();

			for (int i = 0; i < fileNames.length; i++) {
				String fileName = fileNames[i];
				if (!fileName.equals(calendarFilename)) {
					lineHashFiles.add(fileName.replace(".js", ""));
					in = assetManager.open(agencyId + "/" + fileName);
					String jsonParams = getStringFromInputStream(in);
					CompressedTransitTimeTable cttt = Utils
							.convertJSONToObject(jsonParams,
									CompressedTransitTimeTable.class);
					ctttList.add(cttt);
				}
			}
			cur.setAdded(lineHashFiles);

			agencyDescriptor = RoutesDBHelper.buildAgencyDescriptor(agencyId,
					cur, ctttList);
		} catch (Exception e) {
			Log.e(CompressedTTHelper.class.getCanonicalName(), e.getMessage());
		}

		return agencyDescriptor;
	}

	private Map<String, String> loadCalendarByAgencyId(String agencyId) {
		AssetManager assetManager = mContext.getResources().getAssets();
		InputStream in;
		Map<String, String> calendar = new HashMap<String, String>();

		try {
			in = assetManager.open(agencyId + "/" + calendarFilename);
			String jsonParams = getStringFromInputStream(in);
			calendar = Utils.convertJSONToObject(jsonParams, Map.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return calendar;
	}

	private Map<String, Map<String, String>> loadCalendars() {
		Map<String, Map<String, String>> calendarGlobal = new HashMap<String, Map<String, String>>();
		for (String agencyId : RoutesHelper.AGENCYIDS) {
			Map<String, String> calendar = loadCalendarByAgencyId(agencyId);
			if (!calendar.isEmpty()) {
				calendarGlobal.put(agencyId, calendar);
			}
		}
		return calendarGlobal;
	}

	public static TimeTable getTTwithRouteIdAndTime(String routeId,
			long from_time, long to_time) {
		TimeTable tt = null;
		try {
			// convert time to date
			String date = convertMsToDateFormat(from_time);
			// get correct name of file
			String nameFile = RoutesHelper.getAgencyIdByRouteId(routeId)
					+ "/"
					+ routeId
					+ "_"
					+ calendar.get(RoutesHelper.getAgencyIdByRouteId(routeId))
							.get(date) + ".js";
			// get the new tt
			tt = getTimeTable(nameFile, routeId, from_time, to_time);
		} catch (Exception e) {
		}
		return tt;

	}

	private static TimeTable getTimeTable(String nameFile, String routeId,
			long from_time, long to_time) {
		AssetManager assetManager = mContext.getResources().getAssets();
		try {
			TimeTable localTT = null;
			InputStream in = assetManager.open(nameFile);
			String jsonParams = getStringFromInputStream(in);
			// file not exists or the content is not parsed; some error
			// occurred, go for remote version
			if (jsonParams == null)
				return null;
			// file is empty; no transport for the date
			if (jsonParams.length() == 0) {
				localTT = new TimeTable();
				localTT.setStops(Collections.<String> emptyList());
				localTT.setTimes(Collections.<List<String>>emptyList());
//				localTT.setTimes(Collections
//						.<List<List<String>>> singletonList(Collections
//								.<List<String>> emptyList()));
			} else {
				localTT = Utils
						.convertJSONToObject(jsonParams, TimeTable.class);
			}

			localTT.setDelays(emptyDelay(localTT));
			// TimeTable returnTT = changeDelay(localTT, routeId, from_time,
			// to_time);
			return localTT;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static List<Delay> emptyDelay(TimeTable localTT) {
		List<Delay> delays = new ArrayList<Delay>();
		for (int day = 0; day < localTT.getTimes().size(); day++) {
			Delay d = new Delay();
			Map<CreatorType, String> courselist = new HashMap<CreatorType, String>();
			d.setValues(courselist);
			delays.add(d);
		}
		return delays;
	}

	private static TimeTable changeDelay(TimeTable localTT, String routeId,
			long from_time, long to_time) {
		try {
			List<Delay> realTimeDelay = JPHelper.getDelay(routeId, from_time,
					to_time,JPHelper.getAuthToken(mContext));
			localTT.setDelays(realTimeDelay);
		} catch (Exception e) {
			//TODO old code
			// List<List<Map<String, String>>> returnlist = new
			// ArrayList<List<Map<String, String>>>();
			// for (int day = 0; day < localTT.getTimes().size(); day++) {
			// {
			// List<Map<String, String>> daylist = new ArrayList<Map<String,
			// String>>();
			// for (int course = 0; course < localTT.getTimes().get(day).size();
			// course++) {
			// {
			// Map<String, String> courselist = new HashMap<String, String>();
			// daylist.add(courselist);
			// }
			// returnlist.add(daylist);
			// }
			// returnlist.add(daylist);
			// }
			// }
			// create empty delay
			localTT.setDelays(emptyDelay(localTT));
		}
		return localTT;

	}

	public static String convertMsToDateFormat(long time) {
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(date);
	}

	public static void init(Context mContext) {
		instance = new CompressedTTHelper(mContext);
	}

	public static boolean isInitialized() {
		return instance != null;
	}

	private static String getStringFromInputStream(InputStream is) {
		String output = new String();

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;

		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// the case of empty file: means that there is no transport for that
		// date
		if (sb.length() == 0) {
			return "";
		}

		String json = sb.toString();

		try {
			JSONObject jsonObject = new JSONObject(json);
			output = jsonObject.toString();
		} catch (JSONException e) {
			Log.e("TTHelper", e.getMessage());
		}

		return output;
	}

	public static TimeTable ctt2tt(CompressedTransitTimeTable ctt) {
		TimeTable timeTable = new TimeTable();
		timeTable.setStops(ctt.getStops());
		timeTable.setStopsId(ctt.getStopsId());
		
		
		//TODO Ask raman maybe inconsistent datas
//		List<List<String>> tripIdsLists = new ArrayList<List<String>>();
//		tripIdsLists.add(ctt.getTripIds());
//		timeTable.setTripIds(tripIdsLists);
		List<String> tripIdsLists = new ArrayList<String>();
		tripIdsLists.addAll(ctt.getTripIds());
		timeTable.setTripIds(tripIdsLists);
		

		List<List<String>> timesLists = new ArrayList<List<String>>();

		ArrayList<String> times = new ArrayList<String>();

		int counter = 0;
		int index = 0;
		ArrayList<String> column = new ArrayList<String>();

		char[] ctArray = ctt.getCompressedTimes().toCharArray();

		while (index < ctArray.length) {
			if (ctArray[index] == (" ").charAt(0)) {
				index += 1;
				continue;
			}

			if (ctArray[index] == ("|").charAt(0)) {
				column.add("     ");
				index += 1;
			} else {
				StringBuilder sb = new StringBuilder();
				for (int cc = 0; cc < 4; cc++) {
					sb.append(ctArray[index + cc]);
					if (cc == 1) {
						sb.append(":");
					}
				}
				column.add(sb.toString());
				index += 4;
			}
			counter++;

			if (counter == ctt.getStopsId().size()) {
				//TODO ASK RAMAN
				//times.add(column);
				times.addAll(column);
				column = new ArrayList<String>();
				counter = 0;
			}
		}

		timesLists.add(times);
		timeTable.setTimes(timesLists);

		timeTable.setDelays(TTHelper.emptyDelay(timeTable));

		return timeTable;
	}
}
