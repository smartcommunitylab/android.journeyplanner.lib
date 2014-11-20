package eu.trentorise.smartcampus.jp.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.location.Location;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.model.ParkingSerial;

public class ParkingsHelper {

	public static final String PARKING_AID_TRENTO = "COMUNE_DI_TRENTO";
	public static final String PARKING_AID_ROVERETO = "COMUNE_DI_ROVERETO";

	public static final int PARKING_NOT_MONITORED_OLD = -2;
	public static final int PARKING_UNAVAILABLE = -1;
	public static final int PARKING_FULL = 0;
	public static final int PARKING_LOW_AVAIL = 5;
	public static final int PARKING_HIGH_AVAIL = 20;

	private static final String BIKE_SHARING_TRENTO = "BIKE_SHARING_TRENTO";
	private static final String BIKE_SHARING_ROVERETO = "BIKE_SHARING_ROVERETO";
	private static final String BIKE_SHARING_TO_BIKE_TRENTO = "BIKE_SHARING_TOBIKE_TRENTO";
	private static final String BIKE_SHARING_TO_BIKE_ROVERETO = "BIKE_SHARING_TOBIKE_ROVERETO";

	private static final String CAR_PARKING_TRENTO = "COMUNE_DI_TRENTO";
	private static final String CAR_PARKING_ROVERETO = "COMUNE_DI_ROVERETO";
	private static final String CAR_SHARING_TRENTO = "CAR_SHARING_SERVICE";
	
	public static final String PARKING_EXTRA_SEARCHTIME = "searchTime";
	public static final String PARKING_EXTRA_SEARCHTIME_MIN = "min";
	public static final String PARKING_EXTRA_SEARCHTIME_MAX = "max";
	public static final String PARKING_EXTRA_COST = "costData";
	public static final Object PARKING_EXTRA_COST_FIXED = "fixedCost";
	
	private static Location myLocation;

	private static ParkingSerial focusedParking = null;

	private static List<ParkingSerial> parkingsCache = new ArrayList<ParkingSerial>();

	public static ParkingSerial getFocused() {
		return focusedParking;
	}

	public static void setFocused(ParkingSerial focusedParking) {
		ParkingsHelper.focusedParking = focusedParking;
	}

	public static List<ParkingSerial> getCache() {
		return parkingsCache;
	}

	public static void setCache(List<ParkingSerial> parkingsCache) {
		ParkingsHelper.parkingsCache = parkingsCache;
	}

	/*
	 * DATA AND CONFIG
	 */
	public static int getColor(ParkingSerial parking) {
		int color = R.color.parking_blue;

		if (parking.isMonitored()) {
			if (parking.getSlotsAvailable() == PARKING_UNAVAILABLE) {
				// data unavailable
				// color = R.color.parking_red;
			} else if (parking.getSlotsAvailable() <= PARKING_LOW_AVAIL) {
				color = R.color.parking_red;
			} else if (parking.getSlotsAvailable() > PARKING_LOW_AVAIL && parking.getSlotsAvailable() <= PARKING_HIGH_AVAIL) {
				color = R.color.parking_orange;
			} else if (parking.getSlotsAvailable() > PARKING_HIGH_AVAIL) {
				color = R.color.parking_green;
			}
		}

		return color;
	}

	public static int getMarker(ParkingSerial parking) {
		int marker = R.drawable.marker_parking;

		if (parking.isMonitored()) {
			if (parking.getSlotsAvailable() == PARKING_UNAVAILABLE) {
				// data unavailable
				// marker = R.drawable.marker_parking_red;
			} else if (parking.getSlotsAvailable() <= PARKING_LOW_AVAIL) {
				marker = R.drawable.marker_parking_red;
			} else if (parking.getSlotsAvailable() > PARKING_LOW_AVAIL && parking.getSlotsAvailable() <= PARKING_HIGH_AVAIL) {
				marker = R.drawable.marker_parking_orange;
			} else if (parking.getSlotsAvailable() > PARKING_HIGH_AVAIL) {
				marker = R.drawable.marker_parking_green;
			}
		}

		return marker;
	}

	public static String getName(String parking) {
		String parkingName = parkingsNames.get(parking);
		if (parkingName != null) {
			return parkingName;
		} else {
			return parking;
		}
	}
	
	
	public static String getName(ParkingSerial parking) {
		String parkingName = parkingsNames.get(parking.getName());
		if (parkingName != null) {
			return parkingName;
		} else {
			return parking.getName();
		}
	}

	/*
	 * Comparators
	 */
	private static Comparator<ParkingSerial> parkingNameComparator = new Comparator<ParkingSerial>() {
		public int compare(ParkingSerial p1, ParkingSerial p2) {
			return p1.getName().toString().compareTo(p2.getName().toString());
		}
	};

	private static Comparator<ParkingSerial> parkingDistanceComparator = new Comparator<ParkingSerial>() {
		public int compare(ParkingSerial p1, ParkingSerial p2) {
			Location p1Location = new Location("");
			p1Location.setLatitude(p1.getPosition()[0]);
			p1Location.setLongitude(p1.getPosition()[1]);
			float p1distance = myLocation.distanceTo(p1Location);

			Location p2Location = new Location("");
			p2Location.setLatitude(p2.getPosition()[0]);
			p2Location.setLongitude(p2.getPosition()[1]);
			float p2distance = myLocation.distanceTo(p2Location);

			return ((Float) p1distance).compareTo((Float) p2distance);
		}
	};

	public static Comparator<ParkingSerial> getParkingNameComparator() {
		return parkingNameComparator;
	}

	public static Comparator<ParkingSerial> getParkingDistanceComparator(Location myLocation) {
		ParkingsHelper.myLocation = myLocation;
		return parkingDistanceComparator;
	}

	private static final Map<String, String> parkingsNames;
	static {
		Map<String, String> map = new HashMap<String, String>();
		map.put("Circonvallazione Nuova, Ponte S. Lorenzo - Trento", "Parcheggio vecchia tangenziale di Piedicastello");
		map.put("P1", "Parcheggio area ex SIT via Canestrini");
		map.put("P2", "Garage Centro Europa");
		map.put("P3", "Garage Autosilo Buonconsiglio");
		map.put("P4", "Garage piazza Fiera");
		map.put("P5", "Garage Parcheggio Duomo");
		map.put("P6", "Parcheggio CTE via Bomporto");
		map.put("P7", "Parcheggio piazzale Sanseverino");
		map.put("ex-Zuffo - Trento", "Parcheggio Area ex Zuffo");
		map.put("via Asiago, Stazione FS Villazzano - Trento", "Parcheggio via Asiago - Villazzano STAZIONE FS");
		map.put("via Fersina - Trento", "Parcheggio Ghiaie via Fersina");
		map.put("via Maccani - Trento", "Parcheggio Campo Coni via E. Maccani");
		map.put("via Roggia Grande,16 - Trento", "Garage Autorimessa Europa");
		map.put("via Roggia Grande,16-Trento", "Garage Autorimessa Europa");
		map.put("via Torre Verde, 40 - Trento", "Garage Torre Verde");
		map.put("via valentina Zambra - Trento", "Garage Parcheggio Palazzo Onda");
		parkingsNames = Collections.unmodifiableMap(map);
	}

	public static String getParkingAgencyName(Context ctx, String agencyId) {
		if (BIKE_SHARING_TRENTO.equals(agencyId)) {
			return ctx.getString(R.string.BIKE_SHARING_TRENTO);
		}
		if (BIKE_SHARING_ROVERETO.equals(agencyId)) {
			return ctx.getString(R.string.BIKE_SHARING_ROVERETO);
		}
		if (BIKE_SHARING_TO_BIKE_TRENTO.equals(agencyId)) {
			return ctx.getString(R.string.BIKE_SHARING_TOBIKE_TRENTO);
		}
		if (BIKE_SHARING_TO_BIKE_ROVERETO.equals(agencyId)) {
			return ctx.getString(R.string.BIKE_SHARING_TOBIKE_ROVERETO);
		}
		if (BIKE_SHARING_TO_BIKE_ROVERETO.equals(agencyId)) {
			return ctx.getString(R.string.BIKE_SHARING_TOBIKE_ROVERETO);
		}
		if (CAR_PARKING_ROVERETO.equals(agencyId)) {
			return ctx.getString(R.string.CAR_PARKING_ROVERETO);
		}
		if (CAR_PARKING_TRENTO.equals(agencyId)) {
			return ctx.getString(R.string.CAR_PARKING_TRENTO);
		}
		if (CAR_SHARING_TRENTO.equals(agencyId)) {
			return ctx.getString(R.string.CAR_SHARING_TRENTO);
		}
		return agencyId;
	}
	
	public static String getParkingCost(Map<String, Object> extra, Context ctx) {
		if (extra != null && extra.containsKey(ParkingsHelper.PARKING_EXTRA_COST)) {
			Map<String,Object> costData = (Map<String, Object>) extra.get(ParkingsHelper.PARKING_EXTRA_COST);
			String price = (String)costData.get(ParkingsHelper.PARKING_EXTRA_COST_FIXED);
			if (price.equals("0")) return ctx.getResources().getString(R.string.step_parking_free);
			try {
				Double d = Double.parseDouble(price);
				if (d.equals(0)) return ctx.getResources().getString(R.string.step_parking_free);
			} catch (NumberFormatException e) {
			}
			return price;
		}
		return null;
	}
	
	public static String getParkingCostLong(Map<String, Object> extra, Context ctx) {
		String res = getParkingCost(extra, ctx);
		if (res == null || ctx.getResources().getString(R.string.step_parking_free).equals(res)) return res;
		return ctx.getString(R.string.step_parking_cost, res);
	}

}
