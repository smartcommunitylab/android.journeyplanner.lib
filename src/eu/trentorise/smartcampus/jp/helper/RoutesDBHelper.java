package eu.trentorise.smartcampus.jp.helper;

import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

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
	
	public static void updateAgencys(Agency[] agencies){
		
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
		db.close();
	}
	
	private static void addHashesAndDateForAgency(Agency agency,
			SQLiteDatabase db) {
		
		for(String hash: agency.added){
			db.insert(RoutesDatabase.DB_TABLE_CALENDAR, RoutesDatabase.DATE_KEY, agency.toContentValues(hash));
		}
		addRoutes(agency, db);
		
	}

	private static void removeHashesForAgency(Agency agency,SQLiteDatabase db){
			/*
			 * This part produces a String like this:
			 * hash=somehash1 OR hash=somehas2 OR hash=somehash3 ecc..
			 * to put after the WHERE clause in SQL
			 */
			String whereClause=RoutesDatabase.AGENCY_ID_KEY+"="+agency.agencyId+" AND ( ";
			for(String hash : agency.removed){
				whereClause+= RoutesDatabase.LINEHASH_KEY + " = " + hash + " OR ";
				
				
				// delete old staff from the ROUTES table.
				String whereClause2 = RoutesDatabase.LINEHASH_KEY + "="+ hash;
				db.delete(RoutesDatabase.DB_TABLE_ROUTE, whereClause2, null);
			}
			whereClause=whereClause.substring(0,whereClause.length()-4)+")";
			
			//delete old stuff from CALENDAR TABLE
			db.delete(RoutesDatabase.DB_TABLE_CALENDAR,
					whereClause, null);
	}

	private static void addRoutes(Agency agency,SQLiteDatabase db){
		int i= 0;
		for(CompressedTransitTimeTable ctt : agency.ctts){
			ContentValues routes = new ContentValues();
			routes.put(RoutesDatabase.LINEHASH_KEY, agency.added.get(i));
			String stopsIds = ctt.getStopsId().toString();
			routes.put(RoutesDatabase.STOPS_ID_KEY, stopsIds.substring(1,stopsIds.length()-1));
			String stopsNames = ctt.getStops().toString();
			routes.put(RoutesDatabase.STOPS_ID_KEY, stopsNames.substring(1,stopsNames.length()-1));
			db.insert(RoutesDatabase.DB_TABLE_ROUTE,RoutesDatabase.STOPS_NAMES_KEY, routes);
			i++;
		}
	}
	
	public class Agency{
		public String agencyId;
		public List<String> removed;
		public List<String> added;
		public List<CompressedTransitTimeTable> ctts;
		public String version;
		
		private String calendar;
		
		private HashMap<String,String> lines = new HashMap<String, String>();

		public Agency(String agencyId, List<String> removed,
				List<String> added, String version) {
			super();
			this.agencyId = agencyId;
			this.removed = removed;
			this.added = added;
			this.version = version;
		}

		public Agency(String agencyId, List<String> removed,
				List<String> added, List<CompressedTransitTimeTable> ctts,
				String version, String calendar) {
			super();
			this.agencyId = agencyId;
			this.removed = removed;
			this.added = added;
			this.ctts = ctts;
			this.version = version;
			this.calendar = calendar;
			createMap();
		}



		public String getCalendar() {
			return calendar;
		}

		public void setCalendar(String calendar) {
			this.calendar = calendar;
			createMap();
		}

		/**
		 * This method take the json of calendar.js
		 * and create a map where hashes are used as keys
		 * and date are the value
		 */
		private void createMap() {
			calendar = calendar.substring(1,calendar.length()-2);
			calendar=calendar.replace("\"", "");
			String[] rows = calendar.split(",");
			for(String elements : rows){
				String[] datas = elements.split(":");
				lines.put(datas[1], datas[0]);
			}
		}

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
			int index = toAddHash.lastIndexOf('_');
			cv.put(RoutesDatabase.DATE_KEY,lines.get(toAddHash.substring(index+1)));
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
	    		+ AGENCY_ID_KEY + " integer primary key, "
                + DATE_KEY + "text not null, "
                + LINEHASH_KEY + " text not null);";
	    
	    private static final String CREATE_ROUTE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + DB_TABLE_ROUTE + " (" 
	    		+ LINEHASH_KEY + " text primary key, " 
                + STOPS_ID_KEY + " text not null, "
                + STOPS_NAMES_KEY + " text not null );";
	    
	    private static final String CREATE_VERSION_TABLE = "CREATE TABLE IF NOT EXISTS "
                + DB_TABLE_VERSION + " (" 
	    		+ AGENCY_ID_KEY + " integer primary key, "
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
