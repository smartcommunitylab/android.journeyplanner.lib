package eu.trentorise.smartcampus.jp.custom.data;

import java.util.ArrayList;

public class RouteTimeTable {

	private ArrayList<String> timetable;
	private Integer late;
	
	public RouteTimeTable(ArrayList<String> timetable, Integer late) {
		super();
		this.timetable = timetable;
		this.late = late;
	}
	
	public ArrayList<String> getTimetable() {
		return timetable;
	}
	public void setTimetable(ArrayList<String> timetable) {
		this.timetable = timetable;
	}
	public Integer getLate() {
		return late;
	}
	public void setLate(Integer late) {
		this.late = late;
	}
	
}
