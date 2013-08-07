package eu.trentorise.smartcampus.jp.helper;

import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoadType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;

public class AlertRoadsHelper {

	public static final String ALERTS_AID_TRENTO = "COMUNE_DI_TRENTO";
	public static final String ALERTS_AID_ROVERETO = "COMUNE_DI_ROVERETO";

	private static AlertRoadLoc focusedAlert = null;
	private static List<AlertRoadLoc> alertRoadsCache = new ArrayList<AlertRoadLoc>();

	public static AlertRoadLoc getFocused() {
		return focusedAlert;
	}

	public static void setFocused(AlertRoadLoc focusedAlert) {
		AlertRoadsHelper.focusedAlert = focusedAlert;
	}

	public static List<AlertRoadLoc> getCache() {
		return alertRoadsCache;
	}

	public static void setCache(List<AlertRoadLoc> alertRoadsCache) {
		AlertRoadsHelper.alertRoadsCache = alertRoadsCache;
	}

	public static int getDrawableResourceByType(AlertRoadType type) {
		int drawable = R.drawable.ic_menu_alert_other;

		if (AlertRoadType.ROAD_BLOCK.equals(type)) {
			drawable = R.drawable.ic_menu_alert_road_block;
		} else if (AlertRoadType.PARKING_BLOCK.equals(type)) {
			drawable = R.drawable.ic_menu_alert_parking_block;
		} else if (AlertRoadType.DRIVE_CHANGE.equals(type)) {
			drawable = R.drawable.ic_menu_alert_other;
		}

		return drawable;
	}

	public static int getMarker(AlertRoadLoc alert) {
		int marker = R.drawable.marker_alert_other;

		if (alert.getChangeTypes().length == 1) {
			if (AlertRoadType.ROAD_BLOCK.equals(alert.getChangeTypes()[0])) {
				marker = R.drawable.marker_alert_road_block;
			} else if (AlertRoadType.PARKING_BLOCK.equals(alert.getChangeTypes()[0])) {
				marker = R.drawable.marker_alert_parking_block;
			}
		}

		return marker;
	}

	/*
	 * Comparators
	 */
	private static Comparator<AlertRoadLoc> streetNameComparator = new Comparator<AlertRoadLoc>() {
		public int compare(AlertRoadLoc ar1, AlertRoadLoc ar2) {
			return ar1.getRoad().getStreet().compareTo(ar2.getRoad().getStreet());
		}
	};

	public static Comparator<AlertRoadLoc> getStreetNameComparator() {
		return streetNameComparator;
	}

}
