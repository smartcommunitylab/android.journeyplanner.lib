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

import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
import it.sayservice.platform.smartplanner.data.message.cache.CompressedCalendar;
import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.trentorise.smartcampus.jp.timetable.CTTTCacheUpdaterAsyncTask;
import eu.trentorise.smartcampus.jp.timetable.CompressedTTHelper;
import eu.trentorise.smartcampus.network.JsonUtils;

public class RoutesDBHelper {

	public static RoutesDatabase routesDB;
	
	private static Map<String, Map<String,WeakReference<CompressedCalendar>>> calendarCache = new HashMap<String, Map<String,WeakReference<CompressedCalendar>>>();

	private static RoutesDBHelper instance = null;

	protected RoutesDBHelper(Context context) {
		// TODO: test
//			context.deleteDatabase(Environment.getExternalStorageDirectory() + "/" + RoutesDatabase.DB_NAME);
		//	Log.e(RoutesDBHelper.class.getCanonicalName(), "Deleting DB.... SUCCESS");
		//
		routesDB = new RoutesDatabase(context);
	}

	public static void init(Context applicationContext) {
		instance = new RoutesDBHelper(applicationContext);
		CTTTCacheUpdaterAsyncTask ctttCacheUpdaterAsyncTask = new CTTTCacheUpdaterAsyncTask(applicationContext);
		ctttCacheUpdaterAsyncTask.execute(); 
	}

	public static RoutesDBHelper getInstance() {
		return instance;
	}


	public static Map<String, Long> getVersions() {
		SQLiteDatabase db = RoutesDBHelper.routesDB.getReadableDatabase();
		return queryVersions(db);
	}

	public static void updateAgencies(AgencyDescriptor... agencies) {
		SQLiteDatabase db = RoutesDBHelper.routesDB.getWritableDatabase();
		try {
		// delete all
		for (AgencyDescriptor agency : agencies) {
			removeHashesForAgency(agency, db);
		}
		// populate again and update version
		for (AgencyDescriptor agency : agencies) {
			addHashesAndDateForAgency(agency, db);

			// update the version
			ContentValues cv = agency.toContentValues();
			
			if(db.update(RoutesDatabase.DB_TABLE_VERSION, cv, RoutesDatabase.AGENCY_ID_KEY+"='"+agency.agencyId+"'", null) == 0){
				db.insert(RoutesDatabase.DB_TABLE_VERSION, RoutesDatabase.VERSION_KEY, cv);
			}
			//db.insert(RoutesDatabase.DB_TABLE_VERSION, RoutesDatabase.VERSION_KEY, cv);
			
			Log.e(RoutesDBHelper.class.getCanonicalName(), "Agency " + agency.agencyId + " updated.");
		}
		} catch (Exception e) {
//			db.close();	
		}
	}

	public static CompressedTransitTimeTable getTimeTable(String date, String agencyId, String routeId) {
		CompressedTransitTimeTable out = new CompressedTransitTimeTable();
		SQLiteDatabase db = routesDB.getReadableDatabase();
		String hash = getHash(db, date, agencyId, routeId);
		try {
			fillCTT(db, routeId, hash, out);
		} catch (Exception e) {
			CompressedTransitTimeTable tt = new CompressedTransitTimeTable();
			tt.setStops(Collections.<String>emptyList());
			tt.setStopsId(Collections.<String>emptyList());
			tt.setTripIds(Collections.<String>emptyList());
			tt.setCompressedTimes("");
			return tt;
		} finally {
//			db.close();
		}
		return out;
	}

	private static void fillCTT(SQLiteDatabase db, String line, String hash, CompressedTransitTimeTable tt) {
		String whereClause = RoutesDatabase.LINEHASH_KEY + "=?";
		Cursor c = db.query(RoutesDatabase.DB_TABLE_ROUTE, new String[] { RoutesDatabase.STOPS_IDS_KEY,
				RoutesDatabase.STOPS_NAMES_KEY, RoutesDatabase.TRIPS_IDS_KEY, RoutesDatabase.COMPRESSED_TIMES_KEY },
				whereClause, new String[] { hash }, null, null, null, "1");
		c.moveToFirst();
		String stops = c.getString(c.getColumnIndex(RoutesDatabase.STOPS_NAMES_KEY));
		tt.setStops(stops != null ? Arrays.asList(stops.split(",")) : Collections.<String> emptyList());
		String stopsIds = c.getString(c.getColumnIndex(RoutesDatabase.STOPS_IDS_KEY));
		tt.setStopsId(stopsIds != null ? Arrays.asList(stopsIds.split(",")) : Collections.<String> emptyList());
		String tripIds = c.getString(c.getColumnIndex(RoutesDatabase.TRIPS_IDS_KEY));
		tt.setTripIds(tripIds != null ? Arrays.asList(tripIds.split(",")) : Collections.<String> emptyList());
		String compressedTimes = c.getString(c.getColumnIndex(RoutesDatabase.COMPRESSED_TIMES_KEY));
		tt.setCompressedTimes(compressedTimes != null ? compressedTimes : "");
		c.close();
	}

	private static Map<String, Long> queryVersions(SQLiteDatabase db) {
		Map<String, Long> versionsMap = new HashMap<String, Long>();
		Cursor c = db.query(RoutesDatabase.DB_TABLE_VERSION, new String[] { RoutesDatabase.AGENCY_ID_KEY,
				RoutesDatabase.VERSION_KEY }, null, null, null, null, null);
		while (c.moveToNext()) {
			String agencyId = c.getString(c.getColumnIndex(RoutesDatabase.AGENCY_ID_KEY));
			Long version = c.getLong(c.getColumnIndex(RoutesDatabase.VERSION_KEY));
			versionsMap.put(agencyId, version);
		}
		c.close();
		return versionsMap;
	}

	private static String getHash(SQLiteDatabase db, String date, String agencyId, String routeId) {
		CompressedCalendar cal = null;
		if (calendarCache.get(agencyId) == null || calendarCache.get(agencyId).get(routeId) == null || calendarCache.get(agencyId).get(routeId).get() == null) {
			try {
				cal = readCalendarFromDb(db, agencyId, routeId);
				if (cal == null) return null;
				
				if (calendarCache.get(agencyId) == null) calendarCache.put(agencyId, new HashMap<String, WeakReference<CompressedCalendar>>());
				calendarCache.get(agencyId).put(routeId, new WeakReference<CompressedCalendar>(cal));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		cal = calendarCache.get(agencyId).get(routeId).get();
		String hash = cal.getMapping().get(cal.getEntries().get(date));
		return routeId+"_"+hash;
	}

	/**
	 * @param db
	 * @param agencyId
	 * @param routeId
	 * @return
	 */
	private static CompressedCalendar readCalendarFromDb(SQLiteDatabase db,
			String agencyId, String routeId) {
		String whereClause = RoutesDatabase.AGENCY_ID_KEY + "=? AND " + RoutesDatabase.ROUTE_KEY +"=?";
		String sql = "SELECT " + RoutesDatabase.CAL_KEY + " FROM " + RoutesDatabase.DB_TABLE_CALENDAR + " WHERE " + whereClause;
		Cursor c = db.rawQuery(sql,new String[]{agencyId, routeId});
		c.moveToFirst();
		String calString = c.getString(c.getColumnIndex(RoutesDatabase.CAL_KEY));
		CompressedCalendar cal = JsonUtils.toObject(calString, CompressedCalendar.class);
		c.close();
		return cal;
	}

	private static void addHashesAndDateForAgency(AgencyDescriptor agency, SQLiteDatabase db) {
		if (agency.isCalendarModified()) {
			db.beginTransaction();
			for (String calendarId : agency.getCalendars().keySet()) {
				String route = calendarId.substring(calendarId.lastIndexOf(CompressedTTHelper.calendarFilenamePre)+CompressedTTHelper.calendarFilenamePre.length());
				String whereC = RoutesDatabase.AGENCY_ID_KEY + "='"+ agency.agencyId + "' AND "+ RoutesDatabase.ROUTE_KEY +"='"+route +"'";
				if(db.update(RoutesDatabase.DB_TABLE_CALENDAR, agency.toContentValues(route, agency.getCalendars().get(calendarId)), whereC, null) == 0){
					db.insert(RoutesDatabase.DB_TABLE_CALENDAR, null, agency.toContentValues(route, agency.getCalendars().get(calendarId)));
				}	
			}
//			for (String hash : agency.cur.getAdded()) {
//				String route = hash.substring(0,hash.lastIndexOf('_'));
//				String subhash = hash.substring(hash.lastIndexOf('_')+1);
//				
//				List<String> dates = new ArrayList<String>();
//				CompressedCalendar cc = agency.getCalendars().get(CompressedTTHelper.calendarFilenamePre + route);
//				if (cc == null) throw new IllegalArgumentException("No new calendar for route "+ route);
//				for (Entry<String,String> entry : cc.getEntries().entrySet()) {
//					String entryHash = cc.getMapping().get(entry.getValue());
//					if (entryHash.equals(subhash)) dates.add(entry.getKey());
//				}
//				for (String date : dates) {
//					String whereC = RoutesDatabase.AGENCY_ID_KEY + "='"+ agency.agencyId + "' AND "+ RoutesDatabase.ROUTE_KEY +"='"+route +"' AND "+RoutesDatabase.DATE_KEY +" LIKE '%"+date+"%'";
//					if(db.update(RoutesDatabase.DB_TABLE_CALENDAR, agency.toContentValues(hash, route, date), whereC, null) == 0){
//						db.insert(RoutesDatabase.DB_TABLE_CALENDAR, RoutesDatabase.DATE_KEY, agency.toContentValues(hash, route, date));
//					}
//				}			
//			}
//			//If I receive an update relating to the calendar only, I have to consider this case
//			if(agency.cur.getAdded().isEmpty()){	
//				for (String calendarId : agency.getCalendars().keySet()) {
//					String route = calendarId.substring(calendarId.lastIndexOf(CompressedTTHelper.calendarFilenamePre)+1);
//					for (String date : agency.getCalendars().get(calendarId).getEntries().keySet()) {
//						String entryHash = agency.getCalendars().get(calendarId).getMapping().get(agency.getCalendars().get(calendarId).getEntries().get(date));
//						String hash = route + "_" + entryHash;
//						String whereC = RoutesDatabase.AGENCY_ID_KEY + "='"+ agency.agencyId + "' AND "+ RoutesDatabase.ROUTE_KEY +"='"+route+"' AND "+RoutesDatabase.DATE_KEY +" LIKE '%"+date+"%'";
//						if(db.update(RoutesDatabase.DB_TABLE_CALENDAR, agency.toContentValues(hash, route, date), whereC, null) == 0){
//							db.insert(RoutesDatabase.DB_TABLE_CALENDAR, RoutesDatabase.DATE_KEY, agency.toContentValues(hash, route, date));
//						}	
//					}				
//				}
//			}
						
			db.setTransactionSuccessful();
			db.endTransaction();
			
		}
		addRoutes(agency, db);
	}

	private static void removeHashesForAgency(AgencyDescriptor agency, SQLiteDatabase db) {
		if (!agency.cur.getRemoved().isEmpty()) {
			for (String hash : agency.cur.getRemoved()) {
				// delete from the route table only, leave calendar dirty for efficiency
				String whereClause2 = RoutesDatabase.LINEHASH_KEY + "='" + hash + "'";
				db.delete(RoutesDatabase.DB_TABLE_ROUTE, whereClause2, null);
			}
		}
	}

	private static void addRoutes(AgencyDescriptor agency, SQLiteDatabase db) {
		String curLinehash="";
		try {
			int i = 0;
			for (CompressedTransitTimeTable ctt : agency.ctts) {
				ContentValues routes = new ContentValues();
				routes.put(RoutesDatabase.LINEHASH_KEY, agency.cur.getAdded().get(i));
				if (ctt != null) {
					String stopsIds = ctt.getStopsId().toString();
					routes.put(RoutesDatabase.STOPS_IDS_KEY, stopsIds.substring(1, stopsIds.length() - 1));
					String stopsNames = ctt.getStops().toString();
					routes.put(RoutesDatabase.STOPS_NAMES_KEY, stopsNames.substring(1, stopsNames.length() - 1));
					if (ctt.getTripIds() != null) {
						String tripids = ctt.getTripIds().toString();
						routes.put(RoutesDatabase.TRIPS_IDS_KEY, tripids.substring(1, tripids.length() - 1));
					}
					routes.put(RoutesDatabase.COMPRESSED_TIMES_KEY, ctt.getCompressedTimes());
				}

				if(routes.get(RoutesDatabase.LINEHASH_KEY) != null){
					curLinehash = routes.getAsString(RoutesDatabase.LINEHASH_KEY);
				
					if(curLinehash.compareTo("")!=0){
						if (db.update(RoutesDatabase.DB_TABLE_ROUTE, routes,RoutesDatabase.LINEHASH_KEY + "='" + curLinehash+ "'", null) == 0) {
							db.insert(RoutesDatabase.DB_TABLE_ROUTE,RoutesDatabase.COMPRESSED_TIMES_KEY, routes);
						}
					}
				}
								
				i++;
			}
		} catch (Exception e) {
			//Log.e(RoutesDBHelper.class.getCanonicalName(), e.getMessage());
			Log.e(RoutesDBHelper.class.getCanonicalName(), "Error updating or inserting data: " + curLinehash);
		}
	}

	public static AgencyDescriptor buildAgencyDescriptor(String agencyId, CacheUpdateResponse cur,
			List<CompressedTransitTimeTable> ctts) {
		AgencyDescriptor ad = instance.new AgencyDescriptor(agencyId, cur, ctts);
		return ad;
	}

	public class AgencyDescriptor {
		public String agencyId;
		public CacheUpdateResponse cur;

		// public List<String> added;
		// public List<String> removed;
		// public String version;
		// private String calendar = null;

		public List<CompressedTransitTimeTable> ctts;

		// public AgencyDescriptor(String agencyId, CacheUpdateResponse cur,
		// List<CompressedTransitTimeTable> ctts) {
		// super();
		// this.agencyId = agencyId;
		// this.cur = cur;
		// }

		public AgencyDescriptor(String agencyId, CacheUpdateResponse cur, List<CompressedTransitTimeTable> ctts) {
			super();
			this.agencyId = agencyId;
			this.cur = cur;
			this.ctts = ctts;
		}

		public boolean isCalendarModified() {
			return cur.getCalendars() != null;
		}

		public Map<String, CompressedCalendar> getCalendars() {
			return cur.getCalendars();
		}

		public void setCalendars(Map<String, CompressedCalendar> calendars) {
			this.cur.setCalendars(calendars);
		}

		/**
		 * column values for version table
		 * @return
		 */
		public ContentValues toContentValues() {
			ContentValues cv = new ContentValues();
			cv.put(RoutesDatabase.AGENCY_ID_KEY, agencyId);
			// cv.put(RoutesDatabase.VERSION_KEY, version);
			cv.put(RoutesDatabase.VERSION_KEY, this.cur.getVersion());
			return cv;
		}

		/**
		 * column values for the calendar table out of route id and JSON representation of calendar
		 * @param routeId
		 * @param calendar
		 * @return
		 */
		public ContentValues toContentValues(String routeId, CompressedCalendar calendar) {
			ContentValues cv = new ContentValues();
			cv.put(RoutesDatabase.AGENCY_ID_KEY, this.agencyId);
			cv.put(RoutesDatabase.ROUTE_KEY, routeId);
			cv.put(RoutesDatabase.CAL_KEY, JsonUtils.toJSON(calendar));
			return cv;
		}
	}
	private class RoutesDatabase extends SQLiteOpenHelper {

		// DB configurations
		private static final String DB_NAME = "routesdb";
		private static final int DB_VERSION = 15;

		// Tables
		public final static String DB_TABLE_CALENDAR = "calendar";
		public final static String DB_TABLE_ROUTE = "route";
		public final static String DB_TABLE_VERSION = "version";

		// calendar fields
//		public final static String DATE_KEY = "date";
		public final static String AGENCY_ID_KEY = "agencyID"; // this is used
																// in version
																// table too.
		public final static String LINEHASH_KEY = "linehash"; // this is used in
																// routes table
																// too.
		public final static String ROUTE_KEY = "route";
		public static final String CAL_KEY = "calendar";

		// routes fields
		public final static String STOPS_IDS_KEY = "stopsIDs";
		public final static String STOPS_NAMES_KEY = "stopsNames";
		public final static String TRIPS_IDS_KEY = "tripIds";
		public final static String COMPRESSED_TIMES_KEY = "times";

		// version field
		public final static String VERSION_KEY = "version";

//		private static final String CREATE_CALENDAR_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_CALENDAR + " ("
//				+ AGENCY_ID_KEY + " text not null, " + DATE_KEY + " text not null, " + ROUTE_KEY + " text not null, " + LINEHASH_KEY + " text not null);";
		private static final String CREATE_CALENDAR_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_CALENDAR + " ("
				+ AGENCY_ID_KEY + " text not null, " + CAL_KEY + " text not null, " + ROUTE_KEY + " text not null);";

		private static final String CREATE_ROUTE_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_ROUTE + " (" + LINEHASH_KEY
				+ " text primary key, " + STOPS_IDS_KEY + " text, " + STOPS_NAMES_KEY + " text," + TRIPS_IDS_KEY + " text,"
				+ COMPRESSED_TIMES_KEY + " text );";

		private static final String CREATE_VERSION_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_VERSION + " ("
				+ AGENCY_ID_KEY + " integer primary key, " + VERSION_KEY + " integer not null default 0);";

		private static final String DELETE_TABLE  = "DROP TABLE IF EXISTS %s";
		
		public RoutesDatabase(Context context) {
//			test
//			super(context, Environment.getExternalStorageDirectory() + "/" + DB_NAME, null, DB_VERSION);
			super(context, DB_NAME, null, DB_VERSION);

		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_CALENDAR_TABLE);
			db.execSQL(CREATE_ROUTE_TABLE);
			db.execSQL(CREATE_VERSION_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(String.format(DELETE_TABLE, DB_TABLE_CALENDAR));
			db.execSQL(String.format(DELETE_TABLE, DB_TABLE_ROUTE));
			db.execSQL(String.format(DELETE_TABLE, DB_TABLE_VERSION));
			db.execSQL(CREATE_CALENDAR_TABLE);
			db.execSQL(CREATE_ROUTE_TABLE);
			db.execSQL(CREATE_VERSION_TABLE);
		}

	}

}
