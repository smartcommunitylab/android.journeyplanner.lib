package eu.trentorise.smartcampus.jp.custom.data;

import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.Position;

import java.io.Serializable;

public class BasicRoute implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7232534251831904726L;

	/*
	 * transport type, from , to*/
	private String type;
	private String agencyId;
	private String tripId;
	private String routeId;
	private Boolean monitor;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
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
	public Boolean getMonitor() {
		return monitor;
	}
	public void setMonitor(Boolean monitor) {
		this.monitor = monitor;
	}
}

