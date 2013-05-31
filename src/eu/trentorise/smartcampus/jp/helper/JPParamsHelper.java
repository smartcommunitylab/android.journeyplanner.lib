package eu.trentorise.smartcampus.jp.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	public static final String KEY_SMARTCHECK_OPTIONS = "smartcheck_options";
	public static final String KEY_BROADCAST_NOTIFICATIONS_OPTIONS = "broadcast_notifications_options";

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

	public static String[] getBroadcastNotificationsOptions() {
		return getFilteredResource(R.array.broadcast_notifications_array, KEY_BROADCAST_NOTIFICATIONS_OPTIONS);
	}

	public static String[] getSmartCheckOptions() {
		return getFilteredResource(R.array.smart_checks_list, KEY_SMARTCHECK_OPTIONS);
	}

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
