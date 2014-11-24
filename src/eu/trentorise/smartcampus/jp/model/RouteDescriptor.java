package eu.trentorise.smartcampus.jp.model;

import java.io.Serializable;

public class RouteDescriptor implements Serializable {
	private static final long serialVersionUID = 4650920854875541761L;

	private String agencyId;
	private String routeId;
	private Integer nameResource;
	private Object shortName;

	public RouteDescriptor(String agencyId, String routeId, int nameResource, Object shortName) {
		this.agencyId = agencyId;
		this.routeId = routeId;
		this.nameResource = nameResource;
		this.shortName = shortName;
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

	public Object getShortName() {
		return shortName;
	}

}
