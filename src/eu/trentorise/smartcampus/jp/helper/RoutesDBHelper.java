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

import it.sayservice.platform.smartplanner.data.message.cache.CompressedCalendar;
import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import eu.trentorise.smartcampus.network.JsonUtils;

public class RoutesDBHelper {

	public static RoutesDatabase routesDB;
	
	private static Map<String, Map<String,WeakReference<CompressedCalendar>>> calendarCache = new HashMap<String, Map<String,WeakReference<CompressedCalendar>>>();

	protected RoutesDBHelper(Context context) throws IOException {
		routesDB = RoutesDatabase.createDataBase(context);
	}

	public static void init(Context applicationContext) throws IOException {
		new UpdaterAsyncTask(applicationContext).execute();
	}

	private static Map<String, Long> getVersions() {
		SQLiteDatabase db = RoutesDBHelper.routesDB.getReadableDatabase();
		return queryVersions(db);
	}

	public static CompressedTransitTimeTable getTimeTable(String date, String agencyId, String routeId) {
		CompressedTransitTimeTable out = new CompressedTransitTimeTable();
		SQLiteDatabase db = routesDB.getReadableDatabase();
		String hash = getHash(db, date, agencyId, routeId);
		try {
			fillCTT(db, agencyId, routeId, hash, out);
		} catch (Exception e) {
			CompressedTransitTimeTable tt = new CompressedTransitTimeTable();
			tt.setStops(Collections.<String>emptyList());
			tt.setStopsId(Collections.<String>emptyList());
			tt.setTripIds(Collections.<String>emptyList());
			tt.setRoutesIds(Collections.<String>emptyList());
			tt.setCompressedTimes("");
			return tt;
		} finally {
//			db.close();
		}
		return out;
	}

	private static void fillCTT(SQLiteDatabase db, String agencyId, String line, String hash, CompressedTransitTimeTable tt) {
		String whereClause = RoutesDatabase.LINEHASH_KEY + "=? AND "+RoutesDatabase.AGENCY_ID_KEY +"=?";
		Cursor c = db.query(RoutesDatabase.DB_TABLE_ROUTE, new String[] { RoutesDatabase.STOPS_IDS_KEY,
				RoutesDatabase.STOPS_NAMES_KEY, RoutesDatabase.TRIPS_IDS_KEY, RoutesDatabase.COMPRESSED_TIMES_KEY, RoutesDatabase.ROUTES_IDS_KEY },
				whereClause, new String[] { hash, agencyId}, null, null, null, "1");
		c.moveToFirst();
		String stops = c.getString(c.getColumnIndex(RoutesDatabase.STOPS_NAMES_KEY));
		tt.setStops(toTrimmedList(stops));
		
		String stopsIds = c.getString(c.getColumnIndex(RoutesDatabase.STOPS_IDS_KEY));
		tt.setStopsId(toTrimmedList(stopsIds));
		String tripIds = c.getString(c.getColumnIndex(RoutesDatabase.TRIPS_IDS_KEY));
		tt.setTripIds(toTrimmedList(tripIds));
		String routeIds = c.getString(c.getColumnIndex(RoutesDatabase.ROUTES_IDS_KEY));
		tt.setRoutesIds(toTrimmedList(routeIds));
		String compressedTimes = c.getString(c.getColumnIndex(RoutesDatabase.COMPRESSED_TIMES_KEY));
		tt.setCompressedTimes(compressedTimes != null ? compressedTimes : "");
		c.close();
	}

	private static List<String> toTrimmedList(String str) {
		List<String> list = str != null ? Arrays.asList(str.split(",")) : Collections.<String> emptyList();
		List<String> copy = new ArrayList<String>();
		for (String s : list) copy.add(s.trim());
		return copy;
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

	private static class RoutesDatabase extends SQLiteOpenHelper {

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
		public static final String ROUTES_IDS_KEY = "routeIds";

		// version field
		public final static String VERSION_KEY = "version";
		
		private RoutesDatabase(Context context, Integer version) {
			super(context, dbPath(context), null, version);
		}

		/**
		 * Creates a empty database on the system and rewrites it with your own
		 * database.
		 * 
		 * @param dbVersion
		 * @param ctx
		 * */
		private static RoutesDatabase createDataBase(Context ctx) throws IOException {
			int version = checkDataBase(ctx);
			if (JPParamsHelper.getInstance() == null) {
				JPParamsHelper.init(ctx);
			}
			Integer assetVersion = JPParamsHelper.getDBVersion();
			if (assetVersion == null) assetVersion = DB_VERSION;

			RoutesDatabase helper = new RoutesDatabase(ctx, assetVersion);
			helper.openDataBase();
			helper.close();
			if (version < 0) {
				// By calling this method and empty database will be created into
				// the default system path
				// of your application so we are gonna be able to overwrite that
				// database with our database.
				// this.getReadableDatabase();
				try {
					copyDataBase(ctx);
				} catch (IOException e) {
					throw new Error("Error copying database");
				}
			} else if (version < assetVersion) {
				try {
					copyDataBase(ctx);
				} catch (IOException e) {
					throw new Error("Error copying database");
				}
			}
			return helper;
		}

		
		private static String dbPath(Context ctx) {
//			return Environment.getExternalStorageDirectory() + "/" + DB_NAME;
			return ctx.getDatabasePath(DB_NAME).getAbsolutePath();
		}

		/**
		 * Copies your database from your local assets-folder.
		 * */
		private static void copyDataBase(Context ctx) throws IOException {
			// Open your local db as the input stream
			InputStream zipInput = ctx.getAssets().open(DB_NAME+".zip");
			copyDataBase(ctx, zipInput);
		}

		/**
		 * Copies your database from the inputstream to the just created
		 * empty database in the system folder, from where it can be accessed and
		 * handled. This is done by transfering bytestream.
		 * */
		private static void copyDataBase(Context ctx, InputStream zipInput) throws IOException {
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipInput)); 
			try {
				if (zis.getNextEntry() != null) {
					// Path to the just created empty db
					String outFileName = dbPath(ctx);
					// Open the empty db as the output stream
					OutputStream myOutput = new FileOutputStream(outFileName);
					// transfer bytes from the inputfile to the outputfile
					byte[] buffer = new byte[1024];
					int length;
					while ((length = zis.read(buffer)) > 0) {
						myOutput.write(buffer, 0, length);
					}
					// Close the streams
					myOutput.flush();
					myOutput.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			} finally {
				zipInput.close();
				zis.close();
			}
			
		}
		
		/**
		 * Check if the database already exist to avoid re-copying the file each
		 * time you open the application.
		 * 
		 * @return version of a db or -1
		 */
		private static int checkDataBase(Context ctx) {
			SQLiteDatabase checkDB = null;
			int version = -1;
			try {
				String myPath = dbPath(ctx);
				checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
				version = checkDB.getVersion();
			} catch (SQLiteException e) {
				// database does't exist yet.
			}
			if (checkDB != null) {
				checkDB.close();
			}

			return version;
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
		private void openDataBase() throws SQLException {
			// Open the database
			getWritableDatabase();
		}
	}

	private static class UpdaterAsyncTask extends AsyncTask<Void, Void, Void> {

		private Context ctx;

		public UpdaterAsyncTask(Context ctx) {
			super();
			this.ctx = ctx;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				routesDB = RoutesDatabase.createDataBase(ctx);
				Map<String, Long> versions = getVersions();
				Map<String, Long> remoteVersions = JPHelper.getVersions(JPHelper.getAuthToken(ctx));
				if (remoteVersions != null && versions != null) {
					boolean update = false;
					for (String agencyId: versions.keySet()) {
						String localVersion = ""+versions.get(agencyId);
						String remoteVersion = ""+ remoteVersions.get(agencyId);
						if (remoteVersion != null && !localVersion.equals(remoteVersion)) {
							update = true; 
							break;
						}
					}
					if (update) {
						String appId = JPParamsHelper.getDBAppId();
						if (appId != null) {
							RoutesDatabase.copyDataBase(ctx, JPHelper.getDBZipStream(appId, JPHelper.getAuthToken(ctx)));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
}
