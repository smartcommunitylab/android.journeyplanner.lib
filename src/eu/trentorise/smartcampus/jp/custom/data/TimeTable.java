package eu.trentorise.smartcampus.jp.custom.data;

import java.util.List;
import java.util.Map;

import eu.trentorise.smartcampus.mobilityservice.model.Delay;

public class TimeTable {

	private List<List<String>> tripIds;

	public List<String> getStops() {
		return stops;
	}

	public void setStops(List<String> stops) {
		this.stops = stops;
	}

	public List<String> getStopsId() {
		return stopsId;
	}

	public void setStopsId(List<String> stopsId) {
		this.stopsId = stopsId;
	}

	public List<List<List<String>>> getTimes() {
		return times;
	}

	public void setTimes(List<List<List<String>>> times) {
		this.times = times;
	}

	public List<Delay> getDelays() {
		return delays;
	}

	public void setDelays(List<Delay> delays) {
		this.delays = delays;
	}

	public List<List<String>> getTripIds() {
		return tripIds;
	}

	public void setTripIds(List<List<String>> tripIds) {
		this.tripIds = tripIds;
	}

	private List<String> stops;
	private List<String> stopsId;
	private List<List<List<String>>> times;
	private List<Delay> delays;

	// {
	// "stops" : [ "piazza", "via"],
	// "stopsId" : [ "axcd", "fefrfrg", ...., "defr"],
	// "times" : [[12:00:00, 12:05:00,,,,,,13:00:00], [ ]],
	// "delays" : [0,5]
	// }
}
