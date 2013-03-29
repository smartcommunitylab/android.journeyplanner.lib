package eu.trentorise.smartcampus.jp.custom.data;

import it.sayservice.platform.smartplanner.data.message.journey.RecurrentJourney;
import eu.trentorise.smartcampus.storage.BasicObject;


public class BasicRecurrentJourney extends BasicObject {

	private static final long serialVersionUID = -4030283559378955235L;
	
	
	private String clientId;
	private RecurrentJourney data;
	private String name;
	private boolean monitor;
	
	public boolean isMonitor() {
		return monitor;
	}
	public void setMonitor(boolean monitor) {
		this.monitor = monitor;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public RecurrentJourney getData() {
		return data;
	}
	public void setData(RecurrentJourney data) {
		this.data = data;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	

}