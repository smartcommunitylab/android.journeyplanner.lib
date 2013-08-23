package eu.trentorise.smartcampus.jp.helper;

import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoadType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.map.AlertRoadsInfoDialog;
import eu.trentorise.smartcampus.jp.custom.map.AlertRoadsInfoDialog.OnDetailsClick;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;
import eu.trentorise.smartcampus.jp.model.LocatedObject;

public class AlertRoadsHelper {

	public static final String ALERTS_AID_TRENTO = "COMUNE_DI_TRENTO";
	public static final String ALERTS_AID_ROVERETO = "COMUNE_DI_ROVERETO";

	public static final String ALERTS_CACHE_SMARTCHECK = "alerts_cache_smartcheck";
	public static final String ALERTS_CACHE_PLAN = "alerts_cache_plan";

	private static AlertRoadLoc focusedAlert = null;

	private static Map<String, List<AlertRoadLoc>> alertRoadsCachesMap = new HashMap<String, List<AlertRoadLoc>>();

	public static AlertRoadLoc getFocused() {
		return focusedAlert;
	}

	public static void setFocused(AlertRoadLoc focusedAlert) {
		AlertRoadsHelper.focusedAlert = focusedAlert;
	}

	public static List<AlertRoadLoc> getCache(String cacheName) {
		return alertRoadsCachesMap.get(cacheName);
	}

	public static void setCache(String cacheName, List<AlertRoadLoc> alertRoadsCache) {
		AlertRoadsHelper.alertRoadsCachesMap.put(cacheName, alertRoadsCache);
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
	 * Map methods
	 */
	public static void staticOnMarkerClick(SherlockFragmentActivity mActivity, GoogleMap map, Marker marker,
			OnDetailsClick onDetailsClickImplementation) {
		String id = marker.getTitle();

		List<LocatedObject> list = MapManager.ClusteringHelper.getFromGridId(id);

		if (list == null || list.isEmpty()) {
			return;
		}

		if (list.size() > 1 && map.getCameraPosition().zoom == map.getMaxZoomLevel()) {
			AlertRoadsInfoDialog infoDialog = new AlertRoadsInfoDialog(onDetailsClickImplementation);
			Bundle args = new Bundle();
			args.putSerializable(AlertRoadsInfoDialog.ARG_ALERTSLIST, (ArrayList) list);
			infoDialog.setArguments(args);
			infoDialog.show(mActivity.getSupportFragmentManager(), "dialog");
		} else if (list.size() > 1) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), map.getCameraPosition().zoom + 1));
			MapManager.fitMapWithOverlays(list, map);
		} else {
			AlertRoadLoc alert = (AlertRoadLoc) list.get(0);
			AlertRoadsInfoDialog infoDialog = new AlertRoadsInfoDialog(onDetailsClickImplementation);
			Bundle args = new Bundle();
			args.putSerializable(AlertRoadsInfoDialog.ARG_ALERT, alert);
			infoDialog.setArguments(args);
			infoDialog.show(mActivity.getSupportFragmentManager(), "dialog");
		}
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
