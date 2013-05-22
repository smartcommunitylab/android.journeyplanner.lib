package eu.trentorise.smartcampus.jp.helper;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import eu.trentorise.smartcampus.jp.R;

public class ParkingsHelper {

	public static final String PARKING_AID_TRENTO = "COMUNE_DI_TRENTO";
	public static final String PARKING_AID_ROVERETO = "COMUNE_DI_ROVERETO";
	
	public static final int PARKING_NOT_MONITORED = -2;
	public static final int PARKING_UNAVAILABLE = -1;
	public static final int PARKING_FULL = 0;
	public static final int PARKING_LOW = 5;
	public static final int PARKING_HIGH = 20;

	public static int getParkingColor(Parking parking) {
		int color = R.color.parking_blue;

		if (parking.getSlotsAvailable() == PARKING_NOT_MONITORED) {
			// data not monitored
			color = R.color.parking_blue;
		} else if (parking.getSlotsAvailable() == PARKING_UNAVAILABLE) {
			// data unavailable
			color = R.color.parking_red;
		} else if (parking.getSlotsAvailable() <= PARKING_LOW) {
			color = R.color.parking_red;
		} else if (parking.getSlotsAvailable() > PARKING_LOW && parking.getSlotsAvailable() <= PARKING_HIGH) {
			color = R.color.parking_orange;
		} else if (parking.getSlotsAvailable() > PARKING_HIGH) {
			color = R.color.parking_green;
		}

		return color;
	}

	public static int getParkingMarker(Parking parking) {
		int marker = R.drawable.marker_parking;

		if (parking.getSlotsAvailable() == PARKING_NOT_MONITORED) {
			// data not monitored
			marker = R.drawable.marker_parking;
		} else if (parking.getSlotsAvailable() == PARKING_UNAVAILABLE) {
			// data unavailable
			marker = R.drawable.marker_parking_red;
		} else if (parking.getSlotsAvailable() <= PARKING_LOW) {
			marker = R.drawable.marker_parking_red;
		} else if (parking.getSlotsAvailable() > PARKING_LOW && parking.getSlotsAvailable() <= PARKING_HIGH) {
			marker = R.drawable.marker_parking_orange;
		} else if (parking.getSlotsAvailable() > PARKING_HIGH) {
			marker = R.drawable.marker_parking_green;
		}

		return marker;
	}

	public static String getParkingName(Parking parking) {
		String parkingName = parkingsNames.get(parking.getName());
		if (parkingName != null) {
			return parkingName;
		} else {
			return parking.getName();
		}
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
		map.put("via Torre Verde, 40 - Trento", "Garage Torre Verde");
		map.put("via valentina Zambra - Trento", "Garage Parcheggio Palazzo Onda");
		parkingsNames = Collections.unmodifiableMap(map);
	}

}
