package eu.trentorise.smartcampus.jp.custom.data;

public class BusStop {
	
private String stopDescriptio;
private String stopId;

public String getStopDescriptio() {
	return stopDescriptio;
}
public void setStopDescriptio(String stopDescriptio) {
	this.stopDescriptio = stopDescriptio;
}
public String getStopId() {
	return stopId;
}
public void setStopId(String stopId) {
	this.stopId = stopId;
}
public BusStop(String stopDescriptio, String stopId) {
	super();
	this.stopDescriptio = stopDescriptio;
	this.stopId = stopId;
}

}
