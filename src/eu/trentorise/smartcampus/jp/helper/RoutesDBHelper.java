package eu.trentorise.smartcampus.jp.helper;

import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class RoutesDBHelper {
	
	public static RoutesDatabase routesDB;
	
	private static RoutesDBHelper instance = null;
	
	protected RoutesDBHelper(Context context) {
		routesDB = new RoutesDatabase(context);
	}
	
	public static void init(Context applicationContext) {
		instance = new RoutesDBHelper(applicationContext);
	}
	
	public static void updateAgencys(Agency... agencies){
		
		SQLiteDatabase db = RoutesDBHelper.routesDB.getWritableDatabase();
		
		//delete all
		for(Agency agency : agencies){
			removeHashesForAgency(agency,db);		
		}
		//populate again and update version
		for(Agency agency : agencies){
			addHashesAndDateForAgency(agency,db);
			db.insert(RoutesDatabase.DB_TABLE_VERSION, RoutesDatabase.VERSION_KEY,agency.toContentValues() );
		}
		
		

	}
	
	//TODO left the date in content values!!!!!!!
	private static void addHashesAndDateForAgency(Agency agency,
			SQLiteDatabase db) {
		
		for(String hash: agency.added){
			db.insert(RoutesDatabase.DB_TABLE_CALENDAR, RoutesDatabase.DATE_KEY, agency.toContentValues(hash));
		}
		
	}

	private static int removeHashesForAgency(Agency agency,SQLiteDatabase db){
			/*
			 * This part produces a String like this:
			 * hash=somehash1 OR hash=somehas2 OR hash=somehash3 ecc..
			 * to put after the WHERE clause in SQL
			 */
			String whereClause=RoutesDatabase.AGENCY_ID_KEY+"="+agency.agencyId+" AND ( ";
			for(String hash : agency.removed)
				whereClause+= RoutesDatabase.LINEHASH_KEY + " = " + hash + " OR ";
			whereClause=whereClause.substring(0,whereClause.length()-4)+")";
			
			//delete old stuff
			return db.delete(RoutesDatabase.DB_TABLE_CALENDAR,
					whereClause, null);
	}
	
	public class Agency{
		public String agencyId;
		public List<String> removed;
		public List<String> added;
		public String version;
		public String calendar;
		
		public ContentValues toContentValues(){
			ContentValues cv = new ContentValues();
			cv.put(RoutesDatabase.AGENCY_ID_KEY, agencyId);
			cv.put(RoutesDatabase.VERSION_KEY, version);
			return cv; 
		}
		
		public ContentValues toContentValues(String toAddHash){
			ContentValues cv = new ContentValues();
			cv.put(RoutesDatabase.AGENCY_ID_KEY, agencyId);
			cv.put(RoutesDatabase.LINEHASH_KEY, toAddHash);
			return cv; 
		}
	}
	
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
                + DB_TABLE_CALENDAR + " (" 
	    		+ AGENCY_ID_KEY + " integer primary key autoincrement, "
                + DATE_KEY + "text not null, "
                + LINEHASH_KEY + " text not null);";
	    
	    private static final String CREATE_ROUTE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + DB_TABLE_ROUTE + " (" 
	    		+ LINEHASH_KEY + " integer primary key autoincrement, " 
                + STOPS_ID_KEY + " text not null, "
                + STOPS_NAMES_KEY + " text not null );";
	    
	    private static final String CREATE_VERSION_TABLE = "CREATE TABLE IF NOT EXISTS "
                + DB_TABLE_VERSION + " (" 
	    		+ AGENCY_ID_KEY + " integer primary key autoincrement, "
                + VERSION_KEY + " text not null );";
		

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
			// implementation is necessary
			// empty because we are at the first version
		}
		
		
	}

	
}
