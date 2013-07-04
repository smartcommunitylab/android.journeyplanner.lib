package eu.trentorise.smartcampus.jp.timetable;

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
import eu.trentorise.smartcampus.jp.custom.data.TimeTable;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;

public class TTHelper {
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

	private static TTHelper instance = null;
	private static Context mContext;
	private static Map<String, Map<Object, Object>> calendar;
	private static final String calendarFilename = "calendar.js";

	protected TTHelper(Context mContext) {
		super();
		TTHelper.mContext = mContext;
		calendar = loadCalendar();
	}

	private Map<String, Map<Object, Object>> loadCalendar() {
		AssetManager assetManager = mContext.getResources().getAssets();
		InputStream in;
		Map<String, Map<Object, Object>> calendarGlobal = new HashMap<String, Map<Object, Object>>();

		for (String agencyId : RoutesHelper.AGENCYIDS) {
			try {


			in = assetManager.open(agencyId+"/"+calendarFilename);
			String jsonParams = getStringFromInputStream(in);
			calendarGlobal.put(agencyId,  Utils.convertJSONToObject(jsonParams, Map.class));			
		} catch (IOException e) {
			e.printStackTrace();
		}


		}
		return calendarGlobal;

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

		// the case of empty file: means that there is no transport for that date
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

	public static TimeTable getTTwithRouteIdAndTime(String routeId, long from_time, long to_time) {
		TimeTable tt = null;
		try {
			// convert time to date
			String date = convertMsToDateFormat(from_time);
			// get correct name of file
			String nameFile = RoutesHelper.getAgencyIdByRouteId(routeId) + "/" + routeId + "_"
					+ calendar.get(RoutesHelper.getAgencyIdByRouteId(routeId)).get(date) + ".js";
			// get the new tt
			tt = getTimeTable(nameFile, routeId, from_time, to_time);
		} catch (Exception e) {
		}
		return tt;

	}

	private static TimeTable getTimeTable(String nameFile, String routeId, long from_time, long to_time) {
		AssetManager assetManager = mContext.getResources().getAssets();
		try {
			TimeTable localTT = null;
			InputStream in = assetManager.open(nameFile);
			String jsonParams = getStringFromInputStream(in);
			// file not exists or the content is not parsed; some error occurred, go for remote version
			if (jsonParams == null) return null;
			// file is empty; no transport for the date
			if (jsonParams.length() ==0) {
				localTT = new TimeTable();
				localTT.setStops(Collections.<String>emptyList());
				localTT.setTimes(Collections.<List<List<String>>>singletonList(Collections.<List<String>>emptyList()));
			} else {
				localTT = Utils.convertJSONToObject(jsonParams, TimeTable.class);
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

	private static List<List<Map<String, String>>> emptyDelay(TimeTable localTT) {
		List<List<Map<String, String>>> returnlist = new ArrayList<List<Map<String, String>>>();
		for (int day = 0; day < localTT.getTimes().size(); day++) {
			{
				List<Map<String, String>> daylist = new ArrayList<Map<String, String>>();
				for (int course = 0; course < localTT.getTimes().get(day).size(); course++) {

					Map<String, String> courselist = new HashMap<String, String>();
					daylist.add(courselist);
				}
				returnlist.add(daylist);
			}
		}
		return returnlist;
	}

	private static TimeTable changeDelay(TimeTable localTT, String routeId, long from_time, long to_time) {
		try {
			List<List<Map<String, String>>> realTimeDelay = JPHelper.getDelay(routeId, from_time, to_time);
			localTT.setDelays(realTimeDelay);
		} catch (Exception e) {
			// create empty delay
			List<List<Map<String, String>>> returnlist = new ArrayList<List<Map<String, String>>>();
			for (int day = 0; day < localTT.getTimes().size(); day++) {
				{
					List<Map<String, String>> daylist = new ArrayList<Map<String, String>>();
					for (int course = 0; course < localTT.getTimes().get(day).size(); course++) {
						{
							Map<String, String> courselist = new HashMap<String, String>();
							daylist.add(courselist);
						}
						returnlist.add(daylist);
					}
					returnlist.add(daylist);
				}
			}
			localTT.setDelays(returnlist);
		}
		return localTT;

	}

	private static String convertMsToDateFormat(long time) {
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(date);
	}

	public static void init(Context mContext) {
		instance = new TTHelper(mContext);
	}

	public static boolean isInitialized() {
		return instance != null;
	}

}
