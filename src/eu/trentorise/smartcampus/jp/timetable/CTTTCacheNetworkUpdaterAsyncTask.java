package eu.trentorise.smartcampus.jp.timetable;

import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;

import java.util.ArrayList;
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
		
		Map<String, Map<String,String>> subUrban = new HashMap<String, Map<String,String>>();
		
		for (Entry<String, Long> entry : params[0].entrySet()) {
			//esclude gli extraurbani di cui fare il management dopo.
			if(!entry.getKey().equals(RoutesHelper.AGENCYID_BUS_SUBURBAN))
				versionsMap.put(entry.getKey(), entry.getValue().toString());
			else{
				//TODO inserire le routes
				Map<String,String> routes = new HashMap<String, String>();
				routes.put("version", entry.getValue().toString());
				String sroutes =  RoutesHelper.getRoutesIdsList(mContext, new String[]{RoutesHelper.AGENCYID_BUS_SUBURBAN}).toString();
				sroutes=sroutes.substring(1,sroutes.length()-1);
				String sfinal = "";
//				for(String s : sroutes.split(","))
//					sfinal+="\""+s.replace(" ", "")+"\",";
				sfinal = sroutes.split(",")[0];
				routes.put("routes",sfinal.substring(0, sfinal.length()-1));
				subUrban.put(entry.getKey(), routes);
			}
			// Test
			// versionsMap.put(entry.getKey(), "0");
		}
		Map<String, CacheUpdateResponse> cacheUpdateResponsesMap = null;
		Map<String, AgencyDescriptor> agencyDescriptorsMap = new HashMap<String, AgencyDescriptor>();

		try {
			cacheUpdateResponsesMap = JPHelper.getCacheStatus(versionsMap,
					JPHelper.getAuthToken(mContext));

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
						CompressedTransitTimeTable cttt = JPHelper
								.getCacheUpdate(agencyId, addedFileName,
										JPHelper.getAuthToken(mContext));
						ctttList.add(cttt);
					}

					AgencyDescriptor agencyDescriptor = RoutesDBHelper
							.buildAgencyDescriptor(agencyId,
									curEntry.getValue(), ctttList);
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
			if(!subUrban.isEmpty()){
				//TODO avviare un service/asynctask che ti fa queste cose.
				Map<String, CacheUpdateResponse> res = JPHelper.getPartialCacheStatus(subUrban,
					JPHelper.getAuthToken(mContext));
				res.size();
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
