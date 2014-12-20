package eu.trentorise.smartcampus.jp.timetable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesDBHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesDBHelper.AgencyDescriptor;

public class CTTTCacheUpdaterAsyncTask extends AsyncTask<Void, Integer, Void> {

	Map<String, Long> dbVersions;
	Map<String, Long> assetsVersions;


	public CTTTCacheUpdaterAsyncTask(Context mContext) {
		super();
	}

	@Override
	protected void onPreExecute() {
		Log.e(getClass().getCanonicalName(),
				"Agencies update from assets started");
	}

	@Override
	protected Void doInBackground(Void... params) {
		dbVersions = RoutesDBHelper.getVersions();
		assetsVersions = CompressedTTHelper.getVersionsFromAssets();
		List<AgencyDescriptor> adList = new ArrayList<AgencyDescriptor>();
		//update onl
		for (String agencyId : JPParamsHelper.getAgencyID()) {
			Long dbVersion = this.dbVersions.get(agencyId);
			Long assetsVersion = this.assetsVersions.get(agencyId);

			if (assetsVersion != null
					&& (dbVersion == null || dbVersion < assetsVersion)) {
				Log.e(RoutesDBHelper.class.getCanonicalName(),
						"Agency update from asset: " + agencyId);
				AgencyDescriptor ad = CompressedTTHelper
						.buildAgencyDescriptorFromAssets(agencyId,
								assetsVersion);
				adList.add(ad);
			} else {
				Log.e(RoutesDBHelper.class.getCanonicalName(),
						"Agency update from asset NOT NEEDED: " + agencyId);
			}
		}

		if (!adList.isEmpty()) {
			RoutesDBHelper.updateAgencies(adList
					.toArray(new AgencyDescriptor[] {}));
			Log.e(RoutesDBHelper.class.getCanonicalName(), "Agencies updated.");
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// assetsVersions = CompressedTTHelper.getVersionsFromAssets();
//		dbVersions = RoutesDBHelper.getVersions();
//		CTTTCacheNetworkUpdaterAsyncTask csat = new CTTTCacheNetworkUpdaterAsyncTask(
//				mContext);
//		csat.execute(dbVersions);
	}

}
