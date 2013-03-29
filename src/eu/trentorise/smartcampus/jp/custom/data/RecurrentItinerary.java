package eu.trentorise.smartcampus.jp.custom.data;

import it.sayservice.platform.smartplanner.data.message.Transport;

public class RecurrentItinerary {
private String name;
private Transport transport;
private String from;
private String to;
private boolean monitor;


public RecurrentItinerary(String name, Transport transport, String from,
		String to, boolean monitor) {
	super();
	this.name = name;
	this.transport = transport;
	this.from = from;
	this.to = to;
	this.monitor = monitor;
}
public String getFrom() {
	return from;
}
public void setFrom(String from) {
	this.from = from;
}
public String getTo() {
	return to;
}
public void setTo(String to) {
	this.to = to;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public Transport getTransport() {
	return transport;
}
public void setTransportType(Transport transportType) {
	this.transport = transport;
}
public boolean isMonitor() {
	return monitor;
}
public void setMonitor(boolean monitor) {
	this.monitor = monitor;
}
}
