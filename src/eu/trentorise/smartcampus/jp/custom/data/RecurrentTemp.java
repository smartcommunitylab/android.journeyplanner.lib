package eu.trentorise.smartcampus.jp.custom.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.journey.RecurrentJourneyParameters;

public class RecurrentTemp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8877850596813727901L;
	
	private String clientId;
	private RecurrentJourneyParameters rjp;
	private List<Leg> legs;
	private Map<String,Boolean> routes;
	public RecurrentJourneyParameters getRjp() {
		return rjp;
	}
	public void setRjp(RecurrentJourneyParameters rjp) {
		this.rjp = rjp;
	}
	public List<Leg> getLegs() {
		return legs;
	}
	public void setLegs(List<Leg> legs) {
		this.legs = legs;
	}
	public Map<String, Boolean> getRoutes() {
		return routes;
	}
	public void setRoutes(Map<String, Boolean> routes) {
		this.routes = routes;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
