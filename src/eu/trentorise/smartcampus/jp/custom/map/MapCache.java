package eu.trentorise.smartcampus.jp.custom.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

public class MapCache {

	private Map<String, Set<SmartCheckStop>> cache = new HashMap<String, Set<SmartCheckStop>>();
	private static final String key = "agencyId";

	public Collection<SmartCheckStop> getStopsByAgencyIds(String[] ids) {
		List<SmartCheckStop> list = new ArrayList<SmartCheckStop>();
		for (String id : ids) {
			if (cache.containsKey(id))
				list.addAll(cache.get(id));
		}
		return list;
	}

	public boolean addStop(SmartCheckStop stop) {
		String agencyId = (String) stop.getCustomData().get(key);
		Set<SmartCheckStop> cacheByAgencyId = cache.get(agencyId);
		if (cacheByAgencyId == null) {
			cacheByAgencyId = new HashSet<SmartCheckStop>();
			cache.put(agencyId, cacheByAgencyId);
		}
		return cacheByAgencyId.add(stop);
	}

	public SmartCheckStop getStopById(String id) {
		for (Set<SmartCheckStop> stopSet : cache.values()) {
			for (SmartCheckStop stop : stopSet) {
				if (id.equals(stop.getId())) {
					return stop;
				}
			}
		}
		return null;
	}

}
