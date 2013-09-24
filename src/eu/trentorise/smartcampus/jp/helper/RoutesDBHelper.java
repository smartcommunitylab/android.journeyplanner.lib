package eu.trentorise.smartcampus.jp.helper;

import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.trentorise.smartcampus.jp.timetable.CompressedTTHelper;
import eu.trentorise.smartcampus.jp.timetable.CompressedTransitTimeTableCacheUpdaterAsyncTask;

public class RoutesDBHelper {

	public static RoutesDatabase routesDB;

	private static RoutesDBHelper instance = null;

	protected RoutesDBHelper(Context context) {
		routesDB = new RoutesDatabase(context);
	}

	public static void init(Context applicationContext) {
		instance = new RoutesDBHelper(applicationContext);

		Map<String, Long> dbVersions = getVersions();
		Map<String, Long> assetsVersions = CompressedTTHelper.getInstance().getVersionsFromAssets();

		List<AgencyDescriptor> adList = new ArrayList<AgencyDescriptor>();
		for (String agencyId : RoutesHelper.AGENCYIDS) {
			Long dbVersion = dbVersions.get(agencyId);
			Long assetsVersion = assetsVersions.get(agencyId);

			if (assetsVersion != null && (dbVersion == null || dbVersion < assetsVersion)) {
				Log.e(RoutesDBHelper.class.getCanonicalName(), "Agency update from asset: " + agencyId);
				AgencyDescriptor ad = CompressedTTHelper.buildAgencyDescriptorFromAssets(agencyId, assetsVersion);
				adList.add(ad);
			}
		}

		if (!adList.isEmpty()) {
			updateAgencys(adList.toArray(new AgencyDescriptor[] {}));
			Log.e(RoutesDBHelper.class.getCanonicalName(), "Agencies updated.");
		}

		// TODO: test
		// Map<String, String> agencyIdsVersions = new HashMap<String,
		// String>();
		// agencyIdsVersions.put(RoutesHelper.AGENCYID_BUS_TRENTO, "0");
		// agencyIdsVersions.put(RoutesHelper.AGENCYID_TRAIN_BZVR, "0");
		// agencyIdsVersions.put(RoutesHelper.AGENCYID_TRAIN_TM, "0");
		// agencyIdsVersions.put(RoutesHelper.AGENCYID_TRAIN_TNBDG, "0");
		assetsVersions = CompressedTTHelper.getInstance().getVersionsFromAssets();

		CompressedTransitTimeTableCacheUpdaterAsyncTask csat = new CompressedTransitTimeTableCacheUpdaterAsyncTask();
		csat.execute(assetsVersions);
		// TODO: /test
	}

	public static Map<String, Long> getVersions() {
		SQLiteDatabase db = RoutesDBHelper.routesDB.getReadableDatabase();
		return queryVersions(db);
	}

	public static void updateAgencys(AgencyDescriptor... agencies) {
		SQLiteDatabase db = RoutesDBHelper.routesDB.getWritableDatabase();

		// delete all
		for (AgencyDescriptor agency : agencies) {
			removeHashesForAgency(agency, db);
		}
		// populate again and update version
		for (AgencyDescriptor agency : agencies) {
			addHashesAndDateForAgency(agency, db);

			// update the version
			db.insert(RoutesDatabase.DB_TABLE_VERSION, RoutesDatabase.VERSION_KEY, agency.toContentValues());
		}
		db.close();
	}

	public static CompressedTransitTimeTable getTimeTable(String date, String agencyId, String routeId) {
		CompressedTransitTimeTable out = new CompressedTransitTimeTable();
		SQLiteDatabase db = RoutesDBHelper.routesDB.getReadableDatabase();
		String hash = getHash(db, date, agencyId);
		fillCTT(db, routeId, hash, out);
		return out;
	}

	private static void fillCTT(SQLiteDatabase db, String line, String hash, CompressedTransitTimeTable tt) {
		String whereClause = RoutesDatabase.LINEHASH_KEY + "=?";
		Cursor c = db.query(RoutesDatabase.DB_TABLE_ROUTE, new String[] { RoutesDatabase.STOPS_IDS_KEY,
				RoutesDatabase.STOPS_NAMES_KEY, RoutesDatabase.TRIPS_IDS_KEY, RoutesDatabase.COMPRESSED_TIMES_KEY },
				whereClause, new String[] { line + "_" + hash }, null, null, null, "1");
		c.moveToFirst();
		String stops = c.getString(c.getColumnIndex(RoutesDatabase.STOPS_NAMES_KEY));
		tt.setStops(Arrays.asList(stops.split(",")));
		String stopsIds = c.getString(c.getColumnIndex(RoutesDatabase.STOPS_IDS_KEY));
		tt.setStopsId(Arrays.asList(stopsIds.split(",")));
		String tripIds = c.getString(c.getColumnIndex(RoutesDatabase.TRIPS_IDS_KEY));
		tt.setTripIds(Arrays.asList(tripIds.split(",")));
		tt.setCompressedTimes(c.getString(c.getColumnIndex(RoutesDatabase.COMPRESSED_TIMES_KEY)));
	}

	private static Map<String, Long> queryVersions(SQLiteDatabase db) {
		Map<String, Long> versionsMap = new HashMap<String, Long>();
		Cursor c = db.query(RoutesDatabase.DB_TABLE_VERSION, new String[] { RoutesDatabase.AGENCY_ID_KEY,
				RoutesDatabase.VERSION_KEY }, null, null, null, null, null);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			String agencyId = c.getString(c.getColumnIndex(RoutesDatabase.AGENCY_ID_KEY));
			Long version = c.getLong(c.getColumnIndex(RoutesDatabase.VERSION_KEY));
			versionsMap.put(agencyId, version);
		}
		return versionsMap;
	}

	private static String getHash(SQLiteDatabase db, String date, String agencyId) {
		String whereClause = RoutesDatabase.DATE_KEY + "=? AND " + RoutesDatabase.AGENCY_ID_KEY + "=?";
		Cursor c = db.query(RoutesDatabase.DB_TABLE_CALENDAR, new String[] { RoutesDatabase.LINEHASH_KEY }, whereClause,
				new String[] { date, agencyId }, null, null, null, "1");
		c.moveToFirst();
		return c.getString(c.getColumnIndex(RoutesDatabase.LINEHASH_KEY));
	}

	private static void addHashesAndDateForAgency(AgencyDescriptor agency, SQLiteDatabase db) {
		if (agency.isCalendarModified()) {
			for (String hash : agency.cur.getAdded()) {
				db.insert(RoutesDatabase.DB_TABLE_CALENDAR, RoutesDatabase.DATE_KEY,
						agency.toContentValues(hash.substring(hash.lastIndexOf('_'))));
			}
		}

		addRoutes(agency, db);
	}

	private static void removeHashesForAgency(AgencyDescriptor agency, SQLiteDatabase db) {
		/*
		 * This part produces a String like this: hash=somehash1 OR
		 * hash=somehas2 OR hash=somehash3 ecc.. to put after the WHERE clause
		 * in SQL
		 */
		String whereClause = RoutesDatabase.AGENCY_ID_KEY + "=" + agency.agencyId;

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
		int i = 0;
		for (CompressedTransitTimeTable ctt : agency.ctts) {
			ContentValues routes = new ContentValues();
			routes.put(RoutesDatabase.LINEHASH_KEY, agency.cur.getAdded().get(i));
			String stopsIds = ctt.getStopsId().toString();
			routes.put(RoutesDatabase.STOPS_IDS_KEY, stopsIds.substring(1, stopsIds.length() - 1));
			String stopsNames = ctt.getStops().toString();
			routes.put(RoutesDatabase.STOPS_IDS_KEY, stopsNames.substring(1, stopsNames.length() - 1));
			String tripids = ctt.getTripIds().toString();
			routes.put(RoutesDatabase.STOPS_IDS_KEY, tripids.substring(1, tripids.length() - 1));
			db.insert(RoutesDatabase.DB_TABLE_ROUTE, RoutesDatabase.STOPS_NAMES_KEY, routes);
			i++;
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

		private HashMap<String, String> lines = new HashMap<String, String>();

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

			if (cur.getCalendar() != null) {
				createMap();
			}
		}

		public boolean isCalendarModified() {
			return cur.getCalendar() != null;
		}

		public Map<String, String> getCalendar() {
			return cur.getCalendar();
		}

		public void setCalendar(Map<String, String> calendar) {
			this.cur.setCalendar(calendar);
			if (calendar != null) {
				createMap();
			}
		}

		/**
		 * This method take the json of calendar.js and create a map where
		 * hashes are used as keys and date are the value
		 */
		private void createMap() {
			// calendar = calendar.substring(1, calendar.length() - 2);
			// calendar = calendar.replace("\"", "");
			// String[] rows = calendar.split(",");
			// for (String elements : rows) {
			// String[] datas = elements.split(":");
			// lines.put(datas[1], datas[0]);
			// }
			Map<String, String> calendar = this.cur.getCalendar();
			for (Entry<String, String> entry : calendar.entrySet()) {
				lines.put(entry.getValue(), entry.getKey());
			}
		}

		public ContentValues toContentValues() {
			ContentValues cv = new ContentValues();
			cv.put(RoutesDatabase.AGENCY_ID_KEY, agencyId);
			// cv.put(RoutesDatabase.VERSION_KEY, version);
			cv.put(RoutesDatabase.VERSION_KEY, this.cur.getVersion());
			return cv;
		}

		public ContentValues toContentValues(String toAddHash) {
			ContentValues cv = new ContentValues();
			cv.put(RoutesDatabase.AGENCY_ID_KEY, agencyId);
			cv.put(RoutesDatabase.LINEHASH_KEY, toAddHash);
			int index = toAddHash.lastIndexOf('_');
			cv.put(RoutesDatabase.DATE_KEY, lines.get(toAddHash.substring(index + 1)));
			return cv;
		}
	}

	private class RoutesDatabase extends SQLiteOpenHelper {

		// DB configurations
		private static final String DB_NAME = "routesdb";
		private static final int DB_VERSION = 1;

		// Tables
		public final static String DB_TABLE_CALENDAR = "calendar";
		public final static String DB_TABLE_ROUTE = "route";
		public final static String DB_TABLE_VERSION = "version";

		// calendar fields
		public final static String DATE_KEY = "date";
		public final static String AGENCY_ID_KEY = "agencyID"; // this is used
																// in version
																// table too.
		public final static String LINEHASH_KEY = "linehash"; // this is used in
																// routes table
																// too.

		// routes fields
		public final static String STOPS_IDS_KEY = "stopsIDs";
		public final static String STOPS_NAMES_KEY = "stopsNames";
		public final static String TRIPS_IDS_KEY = "tripIds";
		public final static String COMPRESSED_TIMES_KEY = "times";

		// version field
		public final static String VERSION_KEY = "version";

		private static final String CREATE_CALENDAR_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_CALENDAR + " ("
				+ AGENCY_ID_KEY + " integer not null, " + DATE_KEY + " text not null, " + LINEHASH_KEY + " text not null);";

		private static final String CREATE_ROUTE_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_ROUTE + " (" + LINEHASH_KEY
				+ " text primary key, " + STOPS_IDS_KEY + " text not null, " + STOPS_NAMES_KEY + " text," + TRIPS_IDS_KEY
				+ " text," + COMPRESSED_TIMES_KEY + " text not null );";

		private static final String CREATE_VERSION_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_VERSION + " ("
				+ AGENCY_ID_KEY + " integer primary key, " + VERSION_KEY + " integer not null default 0);";

		private static final String DROP_CALENDAR_TABLE = "DROP TABLE " + DB_TABLE_CALENDAR + ";";
		private static final String DROP_ROUTE_TABLE = "DROP TABLE " + DB_TABLE_ROUTE + ";";
		private static final String DROP_VERSION_TABLE = "DROP TABLE " + DB_TABLE_VERSION + ";";

		public RoutesDatabase(Context context) {
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
			db.execSQL(DROP_CALENDAR_TABLE);
			db.execSQL(DROP_ROUTE_TABLE);
			db.execSQL(DROP_VERSION_TABLE);
			db.execSQL(CREATE_CALENDAR_TABLE);
			db.execSQL(CREATE_ROUTE_TABLE);
			db.execSQL(CREATE_VERSION_TABLE);
		}

	}

}
