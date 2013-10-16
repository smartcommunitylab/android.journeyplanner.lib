package eu.trentorise.smartcampus.jp.helper;

import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import eu.trentorise.smartcampus.jp.model.RoutesDatabase;
import eu.trentorise.smartcampus.jp.timetable.CTTTCacheUpdaterAsyncTask;

public class RoutesDBHelper {

	public static RoutesDatabase routesDB;

	private static RoutesDBHelper instance = null;
	private static Context mApplicationContext;

	protected RoutesDBHelper(Context context) {
		// TODO: temp
		//context.deleteDatabase(Environment.getExternalStorageDirectory() + "/" + RoutesDatabase.DB_NAME);
		//
		mApplicationContext= context.getApplicationContext();
		routesDB = new RoutesDatabase(context);
	}

	public static void init(Context applicationContext) {
		instance = new RoutesDBHelper(applicationContext);
		
		// Log.e(RoutesDBHelper.class.getCanonicalName(),
		// routesDB.getReadableDatabase().getPath());

		CTTTCacheUpdaterAsyncTask ctttCacheUpdaterAsyncTask = new CTTTCacheUpdaterAsyncTask();
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

		// delete all
		for (AgencyDescriptor agency : agencies) {
			removeHashesForAgency(agency, db);
		}
		// populate again and update version
		for (AgencyDescriptor agency : agencies) {
			addHashesAndDateForAgency(agency, db);

			// update the version
			ContentValues cv = agency.toContentValues();
			db.insert(RoutesDatabase.DB_TABLE_VERSION, RoutesDatabase.VERSION_KEY, cv);

			Log.e(RoutesDBHelper.class.getCanonicalName(), "Agency " + agency.agencyId + " updated.");
		}
		db.close();
	}

	public static CompressedTransitTimeTable getTimeTable(String date, String agencyId, String routeId) {
		CompressedTransitTimeTable out = new CompressedTransitTimeTable();
		SQLiteDatabase db = routesDB.getReadableDatabase();
		String hash = getHash(db, date, agencyId);
		fillCTT(db, routeId, hash, out);
		db.close();
		return out;
	}

	private static void fillCTT(SQLiteDatabase db, String line, String hash, CompressedTransitTimeTable tt) {
		String whereClause = RoutesDatabase.LINEHASH_KEY + "=?";
		Cursor c = db.query(RoutesDatabase.DB_TABLE_ROUTE, new String[] { RoutesDatabase.STOPS_IDS_KEY,
				RoutesDatabase.STOPS_NAMES_KEY, RoutesDatabase.TRIPS_IDS_KEY, RoutesDatabase.COMPRESSED_TIMES_KEY },
				whereClause, new String[] { line + "_" + hash }, null, null, null, "1");
		c.moveToFirst();
		String stops = c.getString(c.getColumnIndex(RoutesDatabase.STOPS_NAMES_KEY));
		tt.setStops(stops != null ? Arrays.asList(stops.split(",")) : Collections.<String> emptyList());
		String stopsIds = c.getString(c.getColumnIndex(RoutesDatabase.STOPS_IDS_KEY));
		tt.setStopsId(stopsIds != null ? Arrays.asList(stopsIds.split(",")) : Collections.<String> emptyList());
		String tripIds = c.getString(c.getColumnIndex(RoutesDatabase.TRIPS_IDS_KEY));
		tt.setTripIds(tripIds != null ? Arrays.asList(tripIds.split(",")) : Collections.<String> emptyList());
		String compressedTimes = c.getString(c.getColumnIndex(RoutesDatabase.COMPRESSED_TIMES_KEY));
		tt.setCompressedTimes(compressedTimes != null ? compressedTimes : "");
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
		return versionsMap;
	}

	private static String getHash(SQLiteDatabase db, String date, String agencyId) {

		//String whereClause = RoutesDatabase.AGENCY_ID_KEY + "=? AND "+RoutesDatabase.DATE_KEY +"=?";
		String whereClause = RoutesDatabase.AGENCY_ID_KEY + "=? AND "+RoutesDatabase.DATE_KEY +" LIKE '%"+date+"%'";
		//String whereClause = RoutesDatabase.AGENCY_ID_KEY + "=?";
		String sql = "SELECT " + RoutesDatabase.LINEHASH_KEY + " FROM "
				+ RoutesDatabase.DB_TABLE_CALENDAR + " WHERE " + whereClause;
		Cursor c = db.rawQuery(sql,new String[]{agencyId});
//		Cursor c= db.query(RoutesDatabase.DB_TABLE_CALENDAR, new String[]{RoutesDatabase.LINEHASH_KEY}, whereClause,
//			new String[]{agencyId}, null, null, null);
		c.moveToFirst();
		return c.getString(c.getColumnIndex(RoutesDatabase.LINEHASH_KEY));
	}

	private static void addHashesAndDateForAgency(AgencyDescriptor agency, SQLiteDatabase db) {
		if (agency.isCalendarModified()) {
			db.beginTransaction();
			List<String> support = new ArrayList<String>();
			for (String hash : agency.cur.getAdded()) {
				String toAddHash = hash.substring(hash.lastIndexOf('_') + 1);
				
				if (!support.contains(toAddHash)) {
					support.add(toAddHash);
				} else {
					continue; 
				}
				
				List<String> dates = new ArrayList<String>();
				for (Entry<String, String> entry : agency.getCalendar().entrySet()) {
					if (entry.getValue().equals(toAddHash)) {
						dates.add(entry.getKey());
					}
				}
				for (String date : dates) {
					db.insert(RoutesDatabase.DB_TABLE_CALENDAR, RoutesDatabase.DATE_KEY,
							agency.toContentValues(toAddHash, date));
				}
						
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			
		}
		addRoutes(agency, db);
	}

	private static void removeHashesForAgency(AgencyDescriptor agency, SQLiteDatabase db) {
		/*
		 * This part produces a String like this: hash=somehash1 OR
		 * hash=somehas2 OR hash=somehash3 ecc.. to put after the WHERE clause
		 * in SQL
		 */
		String whereClause = RoutesDatabase.AGENCY_ID_KEY + "='" + agency.agencyId+"'";

		if (!agency.cur.getRemoved().isEmpty()) {
			whereClause = whereClause + " AND ( ";
			for (String hash : agency.cur.getRemoved()) {
				whereClause += RoutesDatabase.LINEHASH_KEY + " = " + hash + " OR ";

				// delete old staff from the ROUTES table.
				String whereClause2 = RoutesDatabase.LINEHASH_KEY + "=" + hash;
				db.delete(RoutesDatabase.DB_TABLE_ROUTE, whereClause2, null);
			}
			whereClause = whereClause.substring(0, whereClause.length() - 4) + ")";
		}

		// delete old stuff from CALENDAR TABLE
		db.delete(RoutesDatabase.DB_TABLE_CALENDAR, whereClause, null);
	}

	private static void addRoutes(AgencyDescriptor agency, SQLiteDatabase db) {
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

				db.insert(RoutesDatabase.DB_TABLE_ROUTE, RoutesDatabase.COMPRESSED_TIMES_KEY, routes);
				
				i++;
			}
		} catch (Exception e) {
			Log.e(RoutesDBHelper.class.getCanonicalName(), e.getMessage());
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
			return cur.getCalendar() != null;
		}

		public Map<String, String> getCalendar() {
			return cur.getCalendar();
		}

		public void setCalendar(Map<String, String> calendar) {
			this.cur.setCalendar(calendar);
		}

		// /**
		// * This method take the json of calendar.js and create a map where
		// * hashes are used as keys and date are the value
		// */
		// private void createMap() {
		// // calendar = calendar.substring(1, calendar.length() - 2);
		// // calendar = calendar.replace("\"", "");
		// // String[] rows = calendar.split(",");
		// // for (String elements : rows) {
		// // String[] datas = elements.split(":");
		// // lines.put(datas[1], datas[0]);
		// // }
		// Map<String, String> calendar = this.cur.getCalendar();
		// for (Entry<String, String> entry : calendar.entrySet()) {
		// lines.put(entry.getValue(), entry.getKey());
		// }
		// }

		public ContentValues toContentValues() {
			ContentValues cv = new ContentValues();
			cv.put(RoutesDatabase.AGENCY_ID_KEY, agencyId);
			// cv.put(RoutesDatabase.VERSION_KEY, version);
			cv.put(RoutesDatabase.VERSION_KEY, this.cur.getVersion());
			return cv;
		}

		public ContentValues toContentValues(String toAddHash, String date) {
			ContentValues cv = new ContentValues();
			cv.put(RoutesDatabase.AGENCY_ID_KEY, this.agencyId);
			cv.put(RoutesDatabase.LINEHASH_KEY, toAddHash);
			cv.put(RoutesDatabase.DATE_KEY, date);
			return cv;
		}
	}


}
