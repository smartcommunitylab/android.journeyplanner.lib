package eu.trentorise.smartcampus.jp.model;

import java.io.Serializable;

public class RouteDescriptor implements Serializable {
	private static final long serialVersionUID = 4650920854875541761L;

	private String agencyId;
	private String routeId;
	private Integer nameResource;
	private String shortNameResource;
	private String routeGTFSId;

	public RouteDescriptor(String agencyId, String routeId, int nameResource, String shortNameResource, String routeGTFSId) {
		this.agencyId = agencyId;
		this.routeId = routeId;
		this.nameResource = nameResource;
		this.shortNameResource = shortNameResource;
		this.routeGTFSId = routeGTFSId;
	}

	public String getAgencyId() {
		return agencyId;
	}

	public String getRouteId() {
		return routeId;
	}

	public Integer getNameResource() {
		return nameResource;
	}
	
	public String getShortNameResource() {
		return shortNameResource;
	}

	public String getRouteGTFSId() {
		return routeGTFSId;
	}

}
