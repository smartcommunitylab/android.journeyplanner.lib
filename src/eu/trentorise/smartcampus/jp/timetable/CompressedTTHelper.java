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
import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import eu.trentorise.smartcampus.mobilityservice.model.Delay;
import eu.trentorise.smartcampus.mobilityservice.model.TimeTable;

public class CompressedTTHelper {


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

	public static TimeTable ctt2tt(CompressedTransitTimeTable ctt) {
		TimeTable timeTable = new TimeTable();
		timeTable.setStops(ctt.getStops());
		timeTable.setStopsId(ctt.getStopsId());
		timeTable.setTripIds(ctt.getTripIds());
		timeTable.setRouteIds(ctt.getRoutesIds());

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
