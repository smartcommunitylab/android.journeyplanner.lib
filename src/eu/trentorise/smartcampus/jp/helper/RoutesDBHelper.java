package eu.trentorise.smartcampus.jp.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class RoutesDBHelper {

    
    
	
	private class RoutesDatabase extends SQLiteOpenHelper{
		
		//DB configurations
		private static final String DB_NAME = "routesdb";
	    private static final int DB_VERSION = 1;

	    //Tables
	    public final static String DB_TABLE_CALENDAR = "calendar";
	    public final static String DB_TABLE_ROUTE = "route";
	    public final static String DB_TABLE_VERSION = "version";
	    
	    //calendar fields
	    public final static String DATE_KEY = "date";
	    public final static String AGENCY_ID_KEY = "agencyID"; // this is used in version table too.
	    public final static String LINEHASH_KEY = "linehash";  //this is used in routes table too.
	    
	    //routes fields
	    public final static String STOPS_ID_KEY = "stopsID";
	    public final static String STOPS_NAMES_KEY = "stopsNames";
	    public final static String COMPRESSED_TIMES_KEY = "times";
	    
	    //version field
	    public final static String VERSION_KEY = "version";

	    
	    private static final String CREATE_CALENDAR_TABLE = "CREATE TABLE IF NOT EXISTS "
                + DB_TABLE_CALENDAR + " (" + AGENCY_ID_KEY
                + " integer primary key autoincrement, "
                + DATE_KEY + "text, "
                + LINEHASH_KEY + " text );";
	    
	    private static final String CREATE_ROUTE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + DB_TABLE_CALENDAR + " (" + LINEHASH_KEY
                + " integer primary key autoincrement, " 
                + STOPS_ID_KEY + " text not null, "
                + STOPS_NAMES_KEY + " text not null );";
	    
	    private static final String CREATE_VERSION_TABLE = "CREATE TABLE IF NOT EXISTS "
                + DB_TABLE_VERSION + " (" + AGENCY_ID_KEY
                + " integer primary key autoincrement, "
                + VERSION_KEY + " text not null );";
		

		public RoutesDatabase(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
		
	}

}
