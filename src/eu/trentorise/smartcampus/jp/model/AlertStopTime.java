package eu.trentorise.smartcampus.jp.model;

import java.io.Serializable;

public class AlertStopTime implements Serializable{
	private static final long serialVersionUID = -4567150508477092785L;

	private String stopId, stopName, tripId, routeId, agencyId;
	private long time;

	public AlertStopTime() {
		super();
	}

	public AlertStopTime(String stopId, String stopName, String tripId, String routeId, String agencyId, long time) {
		super();
		this.stopId = stopId;
		this.stopName = stopName;
		this.tripId = tripId;
		this.routeId = routeId;
		this.agencyId = agencyId;
		this.time = time;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getStopId() {
		return stopId;
	}

	public void setStopId(String stopId) {
		this.stopId = stopId;
	}

	public String getStopName() {
		return stopName;
	}

	public void setStopName(String stopName) {
		this.stopName = stopName;
	}

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}
	
}
