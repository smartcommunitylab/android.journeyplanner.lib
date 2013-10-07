package eu.trentorise.smartcampus.jp.custom.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.util.Pair;

import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

public class MapCache {

	// private Map<String, Set<SmartCheckStop>> cache = new HashMap<String,
	// Set<SmartCheckStop>>();
	private Collection<Pair<String, SmartCheckStop>> cache = new HashSet<Pair<String, SmartCheckStop>>();
	private static final String key = "agencyId";

	public Collection<SmartCheckStop> getStopsByAgencyIds(String[] ids) {
		List<SmartCheckStop> list = new ArrayList<SmartCheckStop>();
		for (String id : ids) {
			// if (cache.containsKey(id))
			// list.addAll(cache.get(id));
			for (Pair<String, SmartCheckStop> p : cache) {
				if (p.first.equals(id))
					list.add(p.second);
			}
		}
		return list;
	}

	public boolean addStop(SmartCheckStop stop) {
		String agencyId = (String) stop.getCustomData().get(key);
		// Set<SmartCheckStop> cacheByAgencyId = cache.get(agencyId);
		// if (cacheByAgencyId == null) {
		// cacheByAgencyId = new HashSet<SmartCheckStop>();
		// cache.put(agencyId, cacheByAgencyId);
		// }
		// return cacheByAgencyId.add(stop);
		return cache.add(new Pair<String, SmartCheckStop>(agencyId, stop));
	}

	public SmartCheckStop getStopById(String id) {
		// for (Set<SmartCheckStop> stopSet : cache.values()) {
		// for (SmartCheckStop stop : stopSet) {
		// if (id.equals(stop.getId())) {
		// return stop;
		// }
		// }
		// }
		for (Pair<String, SmartCheckStop> p : cache) {
			if (id.equals(p.second.getId()))
				return p.second;
		}
		return null;
	}

}
