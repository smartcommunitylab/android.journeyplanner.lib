package eu.trentorise.smartcampus.jp.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import eu.trentorise.smartcampus.android.common.params.ParamsHelper;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.model.RouteDescriptor;

public class JPParamsHelper {
	private static final String TAG = "JPParamsHelper";
	private static Context mContext;
	private static JPParamsHelper instance = null;

	public static final String DEFAULT_APP_TOKEN = "journeyplanner";

	private static String FILENAME = "params_jp.js";

	/* json parameters in assets/params_js.js */
	public static final String KEY_APP_TOKEN = "app_token";
	public static final String KEY_ALERTROADS_PLANNING = "alertroads_in_planning";
	public static final String KEY_ALERTROADS_PLANNING_AGENCYID = "alertroads_in_planning_agencyid";
	public static final String KEY_SMARTCHECK_OPTIONS = "smartcheck_options";
	public static final String KEY_SUBURBAN_ZONES = "suburban_zones";
	public static final String KEY_AGENCY = "agency";
	public static final String KEY_AGENCY_ID = "agency_id";
	public static final String KEY_ROUTES_ID = "routes_id";
	public static final String KEY_BROADCAST_NOTIFICATIONS_OPTIONS = "broadcast_notifications_options";
	public static final String KEY_CENTER_MAP = "center_map";
	public static final String KEY_ZOOM_MAP = "zoom_map";

	private static final Integer DEFAULT_ZOOM_LEVEL = 15;
	private Map<Object, Object> paramsAsset;

	protected JPParamsHelper(Context mContext) {
		JPParamsHelper.mContext = mContext;
		this.paramsAsset = ParamsHelper.load(mContext, FILENAME);
	}

	private Map<Object, Object> getParamsAsset() {
		return this.paramsAsset;
	}

	public static void init(Context mContext) {
		if (instance == null) {
			instance = new JPParamsHelper(mContext);
		}
	}

	public static JPParamsHelper getInstance() {
		if (instance == null && mContext != null) {
			JPParamsHelper.init(mContext);
		}
		return instance;
	}

	/*
	 * CUSTOM METHODS
	 */
	public static String getAppToken() {
		String returnToken = new String();
		returnToken = (String) getInstance().getParamsAsset().get(KEY_APP_TOKEN);
		if (returnToken == null)
			return DEFAULT_APP_TOKEN;
		return returnToken;
	}

	public static boolean isAlertroadsVisibleOnPlanning() {
		Boolean alertsVisible = (Boolean) getInstance().getParamsAsset().get(KEY_ALERTROADS_PLANNING);
		if (alertsVisible != null && alertsVisible == true) {
			return true;
		}
		return false;
	}

	public static String getAlertroadsAgencyId() {
		String aid = null;
		String paramAgencyId = (String) getInstance().getParamsAsset().get(KEY_ALERTROADS_PLANNING_AGENCYID);
		if (paramAgencyId != null
				&& (AlertRoadsHelper.ALERTS_AID_ROVERETO.equals(paramAgencyId) || AlertRoadsHelper.ALERTS_AID_TRENTO
						.equals(paramAgencyId))) {
			aid = paramAgencyId;
		}
		return aid;
	}

	public static String[] getBroadcastNotificationsOptions() {
		return getFilteredResource(R.array.broadcast_notifications_array, KEY_BROADCAST_NOTIFICATIONS_OPTIONS);
	}

	public static String[] getSmartCheckOptions() {
		return getFilteredResource(R.array.smart_checks_list, KEY_SMARTCHECK_OPTIONS);
	}

	public static List<String> getSuburbanZones() {
		List<String> suburbanZones = new ArrayList<String>();
		List<Integer> result = (List<Integer>) getInstance().getParamsAsset().get(KEY_SUBURBAN_ZONES);
		if (result != null) {
			for (Integer n : result) {
				suburbanZones.add(Integer.toString(n));
			}
		}
		return suburbanZones;
	}

	public static List<String> getAgencyID() {
		List<String> agencyIDs = new ArrayList<String>();
		List<HashMap<String, Object>> result = (List<HashMap<String, Object>>) getInstance().getParamsAsset().get(KEY_AGENCY);
		if (result != null) {
			for (HashMap<String, Object> n : result) {
				if (n != null) {
					agencyIDs.add((String) n.get(KEY_AGENCY_ID));
				}
			}
		} else {
			agencyIDs = RoutesHelper.AGENCYIDS;
		}
		return agencyIDs;
	}

	public static List<RouteDescriptor> getRoutesIDByAgencyID(String agencyID) {
		List<RouteDescriptor> routes = new ArrayList<RouteDescriptor>();
		List<HashMap<String, Object>> result = (List<HashMap<String, Object>>) getInstance().getParamsAsset().get(KEY_AGENCY);
		if (result != null) {
			for (HashMap<String, Object> n : result) {
				if (((String) n.get(KEY_AGENCY_ID)).equals(agencyID)) {
					routes = (RoutesHelper.ROUTES.get((String) n.get(KEY_AGENCY_ID)));
					break;
				}
			}
		} else {
			routes = RoutesHelper.ROUTES.get(agencyID);
		}
		return routes;
	}

	public static List<String> getRoutesParamsIDByAgencyID(String agencyID) {
		List<String> routes = new ArrayList<String>();
		List<HashMap<String, Object>> result = (List<HashMap<String, Object>>) getInstance().getParamsAsset().get(KEY_AGENCY);
		if (result != null) {
			for (HashMap<String, Object> n : result) {
				if (((String) n.get(KEY_AGENCY_ID)).equals(agencyID)) {
					ArrayList<String> routesParams = (ArrayList<String>) n.get(KEY_ROUTES_ID);
					if (routesParams == null)
						return new ArrayList<String>();
					routes = routesParams;
					break;
				}
			}
		}
		return routes;
	}

	/*
	 * MAP
	 */
	public static int getZoomLevelMap() {
		if (getInstance() != null && getInstance().getParamsAsset() != null) {
			return (Integer) getInstance().getParamsAsset().get(KEY_ZOOM_MAP);
		} else {
			return DEFAULT_ZOOM_LEVEL;
		}
	}

	public static List<Double> getCenterMap() {
		if (getInstance() != null && getInstance().getParamsAsset() != null) {
			return (List<Double>) getInstance().getParamsAsset().get(KEY_CENTER_MAP);
		} else {
			return null;
		}
	}

	/*
	 * Private methods
	 */
	private static String[] getFilteredResource(int resource, String key) {
		List<String> filteredOptions = new ArrayList<String>();
		String[] options = mContext.getResources().getStringArray(resource);

		List<Integer> optionsFromAssets = (List<Integer>) getInstance().getParamsAsset().get(key);
		if (optionsFromAssets != null && !optionsFromAssets.isEmpty()) {
			for (Integer index : optionsFromAssets) {
				if (index != null) {
					try {
						filteredOptions.add(options[index - 1]);
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					}
				}
			}
		}

		return filteredOptions.toArray(new String[] {});
	}

}
