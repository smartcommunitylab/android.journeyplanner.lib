package eu.trentorise.smartcampus.jp.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

	public class RoutesDatabase extends SQLiteOpenHelper {

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
				+ AGENCY_ID_KEY + " text not null, " + DATE_KEY + " text not null, " + LINEHASH_KEY + " text not null);";

		private static final String CREATE_ROUTE_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_ROUTE + " (" + LINEHASH_KEY
				+ " text primary key, " + STOPS_IDS_KEY + " text, " + STOPS_NAMES_KEY + " text," + TRIPS_IDS_KEY + " text,"
				+ COMPRESSED_TIMES_KEY + " text );";

		private static final String CREATE_VERSION_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_VERSION + " ("
				+ AGENCY_ID_KEY + " integer primary key, " + VERSION_KEY + " integer not null default 0);";

		public RoutesDatabase(Context context) {
			super(context, Environment.getExternalStorageDirectory() + "/" + DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_CALENDAR_TABLE);
			db.execSQL(CREATE_ROUTE_TABLE);
			db.execSQL(CREATE_VERSION_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}

	}