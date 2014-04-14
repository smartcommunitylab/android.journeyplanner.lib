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
package eu.trentorise.smartcampus.jp.timetable;

import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
import it.sayservice.platform.smartplanner.data.message.cache.CompressedCalendar;
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
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.jp.helper.RoutesDBHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesDBHelper.AgencyDescriptor;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.mobilityservice.model.Delay;
import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;

public class CompressedTTHelper {

	private static CompressedTTHelper instance = null;
	private static Context mContext;
	private static Map<String, Map<String, CompressedCalendar>> calendar;
	public static final String calendarFilenamePre = "calendar_";
	private static final String calendarFilenamePost = ".js";
	private static final String indexFilename = "_index.txt";

	protected CompressedTTHelper(Context mContext) {
		super();
		CompressedTTHelper.mContext = mContext;
//		calendar = loadCalendars();
	}

	public static CompressedTTHelper getInstance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
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
			cur.setCalendars(CompressedTTHelper.getInstance()
					.loadCalendarsByAgencyId(agencyId));

			AssetManager assetManager = mContext.getResources().getAssets();
			InputStream in;
			String[] fileNames = assetManager.list(agencyId);

			List<String> lineHashFiles = new ArrayList<String>();
			List<CompressedTransitTimeTable> ctttList = new ArrayList<CompressedTransitTimeTable>();

			for (int i = 0; i < fileNames.length; i++) {
				String fileName = fileNames[i];
				if (!fileName.startsWith(calendarFilenamePre)) {
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

	private Map<String,CompressedCalendar> loadCalendarsByAgencyId(String agencyId) {
		Resources resources = mContext.getResources();
		InputStream in = null;
		String jsonParams = null;
		Map<String, CompressedCalendar> calendars = new HashMap<String, CompressedCalendar>();
		AssetManager assetManager= resources.getAssets();
		try {
			String[] files = assetManager.list(agencyId);
			CompressedCalendar cc = null;
			if (files == null) throw new IOException("empty agency folder");
			for (String f : files) {
				if (f.startsWith(calendarFilenamePre)) {
					in = assetManager.open(agencyId + "/" + f);
					jsonParams = getStringFromInputStream(in);
					cc = new CompressedCalendar();
					cc.setEntries(new HashMap<String, String>());
					cc.setMapping(new HashMap<String, String>());
					Map<String,String> map = Utils.convertJSONToObject(jsonParams, Map.class);
					Map<String, Integer> tmp = new HashMap<String, Integer>();
					int i = 0;
					String hash = null, mapping = null;
					for (String date : map.keySet()) {
						hash = map.get(date);
						if (tmp.containsKey(hash)) {
							mapping = ""+tmp.get(hash);
						} else {
							mapping = ""+i;
							cc.getMapping().put(mapping, hash);
							i++;
						}
						cc.getEntries().put(date, mapping);
					}
					calendars.put(f.substring(0,f.lastIndexOf(calendarFilenamePost)), cc);
					in.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return calendars;
	}

	private Map<String, Map<String, CompressedCalendar>> loadCalendars() {
		Map<String, Map<String, CompressedCalendar>> calendarGlobal = new HashMap<String, Map<String, CompressedCalendar>>();
//		for (String agencyId : RoutesHelper.AGENCYIDS) {
//			Map<String, CompressedCalendar> calendars = loadCalendarsByAgencyId(agencyId);
//			if (!calendars.isEmpty()) {
//				calendarGlobal.put(agencyId, calendars);
//			}
//		}
		return calendarGlobal;
	}
	
	
//commented because calendar initialization crashes in android 2.2
//	public static TimeTable getTTwithRouteIdAndTime(String routeId,
//			long from_time, long to_time) {
//		TimeTable tt = null;
//		try {
//			// convert time to date
//			String date = convertMsToDateFormat(from_time);
//			// get correct name of file
//			String nameFile = RoutesHelper.getAgencyIdByRouteId(routeId)
//					+ "/"
//					+ routeId
//					+ "_"
//					+ calendar.get(RoutesHelper.getAgencyIdByRouteId(routeId))
//							.get(date) + ".js";
//			// get the new tt
//			tt = getTimeTable(nameFile, routeId, from_time, to_time);
//		} catch (Exception e) {
//		}
//		return tt;
//
//	}

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

	public static String convertMsToDateFormat(long time) {
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
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
		timeTable.setTripIds(ctt.getTripIds());
		

		List<List<String>> timesLists = new ArrayList<List<String>>();

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
				timesLists.add(column);
				column = new ArrayList<String>();
				counter = 0;
			}
		}

		timeTable.setTimes(timesLists);

		timeTable.setDelays(emptyDelay(timeTable));

		return timeTable;
	}

}
