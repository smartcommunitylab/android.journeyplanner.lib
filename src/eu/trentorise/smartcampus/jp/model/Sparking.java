package eu.trentorise.smartcampus.jp.model;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;

import java.io.Serializable;

public class Sparking implements Serializable {
	private static final long serialVersionUID = -6427540022630812734L;

	private String name;
	private String description;
	private int slotsTotal;
	private int slotsAvailable;
	private double[] position;

	public Sparking (Parking parking) {
		setName(parking.getName());
		setDescription(parking.getDescription());
		setSlotsTotal(parking.getSlotsTotal());
		setSlotsAvailable(parking.getSlotsAvailable());
		setPosition(parking.getPosition());
	}
	
	public Parking getParking() {
		Parking parking = new Parking();
		parking.setName(getName());
		parking.setDescription(getDescription());
		parking.setSlotsTotal(getSlotsTotal());
		parking.setSlotsAvailable(getSlotsAvailable());
		parking.setPosition(getPosition());
		return parking;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSlotsTotal() {
		return slotsTotal;
	}

	public void setSlotsTotal(int slotsTotal) {
		this.slotsTotal = slotsTotal;
	}

	public int getSlotsAvailable() {
		return slotsAvailable;
	}

	public void setSlotsAvailable(int slotsAvailable) {
		this.slotsAvailable = slotsAvailable;
	}

	public double[] getPosition() {
		return position;
	}

	public void setPosition(double[] position) {
		this.position = position;
	}

}
