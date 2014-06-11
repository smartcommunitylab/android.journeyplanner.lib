package eu.trentorise.smartcampus.jp.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.android.gms.internal.o;

import android.content.Context;
import android.util.Log;
import eu.trentorise.smartcampus.android.common.params.ParamsHelper;
import eu.trentorise.smartcampus.jp.R;

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

	/*
	 * MAP
	 */
	public static int getZoomLevelMap() {
		if (getInstance() != null && getInstance().getParamsAsset() != null)
			return (Integer) getInstance().getParamsAsset().get(KEY_ZOOM_MAP);
		else return DEFAULT_ZOOM_LEVEL;
	}

	public static List<Double> getCenterMap() {
		return (List<Double>) getInstance().getParamsAsset().get(KEY_CENTER_MAP);
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
