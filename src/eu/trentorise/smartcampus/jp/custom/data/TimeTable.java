package eu.trentorise.smartcampus.jp.custom.data;

import java.util.List;

public class TimeTable {


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
	public List<List<Integer>> getDelays() {
		return delays;
	}
	public void setDelays(List<List<Integer>> delays) {
		this.delays = delays;
	}
	private List<String> stops; 
	private List<String> stopsId; 
	private List<List<List<String>>> times; 
	private List<List<Integer>> delays;
	
//	{
//		"stops" : [ "piazza", "via"],
//		"stopsId" : [ "axcd", "fefrfrg", ...., "defr"],
//		"times" : [[12:00:00, 12:05:00,,,,,,13:00:00], [ ]],
//		"delays" : [0,5]
//		}
}
