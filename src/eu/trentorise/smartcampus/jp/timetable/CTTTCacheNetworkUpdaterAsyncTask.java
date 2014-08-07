package eu.trentorise.smartcampus.jp.timetable;

import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesDBHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesDBHelper.AgencyDescriptor;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.network.RemoteException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class CTTTCacheNetworkUpdaterAsyncTask extends
		AsyncTask<Map<String, Long>, Integer, Map<String, AgencyDescriptor>> {

	private long time;
	private int updatedAgency;
	private Context mContext;

	public CTTTCacheNetworkUpdaterAsyncTask(Context mContext) {
		super();
		this.mContext = mContext;
	}

	@Override
	protected void onPreExecute() {
		// TODO: test
		updatedAgency = 0;
		time = System.currentTimeMillis();
		Log.e(getClass().getCanonicalName(),
				"Agencies update from server started");

		super.onPreExecute();
	}

	@Override
	protected Map<String, AgencyDescriptor> doInBackground(
			Map<String, Long>... params) {

		Map<String, String> versionsMap = new HashMap<String, String>();

		Map<String, Map<String, Object>> subUrban = new HashMap<String, Map<String, Object>>();

		for (Entry<String, Long> entry : params[0].entrySet()) {

			versionsMap.put(entry.getKey(), entry.getValue().toString());
			if (entry.getKey().equals(RoutesHelper.AGENCYID_BUS_SUBURBAN)) {
				Map<String, Object> inner = new HashMap<String, Object>();
				inner.put("version", entry.getKey());
				List<String> routes = RoutesHelper.getRoutesIdsList(mContext,
						new String[] { RoutesHelper.AGENCYID_BUS_SUBURBAN });
				inner.put("routes", routes);
				subUrban.put(entry.getKey(), inner);
			}

		}

		Map<String, CacheUpdateResponse> cacheUpdateResponsesMap = null;
		Map<String, AgencyDescriptor> agencyDescriptorsMap = new HashMap<String, AgencyDescriptor>();

		try {
			cacheUpdateResponsesMap = JPHelper.getCacheStatus(versionsMap,
					JPHelper.getAuthToken(mContext));

			doUpdate(versionsMap, cacheUpdateResponsesMap, agencyDescriptorsMap);

			//se ci sono extraurbani
			if (!subUrban.isEmpty()) {
				Map<String, CacheUpdateResponse> res = JPHelper
						.getPartialCacheStatus(subUrban,
								JPHelper.getAuthToken(mContext));
				doUpdate(versionsMap, res, agencyDescriptorsMap);
			}

		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AACException e) {
			e.printStackTrace();
		}

		return agencyDescriptorsMap;
	}

	private void doUpdate(Map<String, String> versionsMap,
			Map<String, CacheUpdateResponse> cacheUpdateResponsesMap,
			Map<String, AgencyDescriptor> agencyDescriptorsMap)
			throws ProtocolException, SecurityException, RemoteException,
			AACException {
		for (Entry<String, CacheUpdateResponse> curEntry : cacheUpdateResponsesMap
				.entrySet()) {
			String agencyId = curEntry.getKey();
			List<String> addedList = curEntry.getValue().getAdded();
			List<String> removedList = curEntry.getValue().getRemoved();
			Long onlineVersion = curEntry.getValue().getVersion();
			Long dbVersion = Long.parseLong(versionsMap.get(agencyId));
			List<CompressedTransitTimeTable> ctttList = new ArrayList<CompressedTransitTimeTable>();

			if (onlineVersion > dbVersion) {
				Log.e(getClass().getCanonicalName(), "Updating Agency "
						+ agencyId);
				updatedAgency++;

				for (String removedFileName : removedList) {
					Log.e(getClass().getCanonicalName(),
							"Update of removedFileName: " + removedFileName);
				}

				for (String addedFileName : addedList) {
					Log.e(getClass().getCanonicalName(),
							"Update of addedFileName: " + addedFileName);
					CompressedTransitTimeTable cttt = JPHelper.getCacheUpdate(
							agencyId, addedFileName,
							JPHelper.getAuthToken(mContext));
					ctttList.add(cttt);
				}

				AgencyDescriptor agencyDescriptor = RoutesDBHelper
						.buildAgencyDescriptor(agencyId, curEntry.getValue(),
								ctttList);
				agencyDescriptorsMap.put(agencyId, agencyDescriptor);

				RoutesDBHelper.updateAgencies(agencyDescriptor); // Here
																	// start
																	// the
																	// updating
																	// of DB
				Log.e(RoutesDBHelper.class.getCanonicalName(),
						"Agencies updated.");
			} else {
				Log.e(getClass().getCanonicalName(),
						"No update found for Agency " + agencyId);
			}

		}
	}

	@Override
	protected void onPostExecute(Map<String, AgencyDescriptor> result) {
		// TODO: test
		time = (System.currentTimeMillis() - time) / 1000;
		Log.e(getClass().getCanonicalName(),
				"Agencies updated: " + Integer.toString(updatedAgency) + " in "
						+ Long.toString(time) + " seconds.");

		super.onPostExecute(result);
	}

}
