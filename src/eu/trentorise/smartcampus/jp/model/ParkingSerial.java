package eu.trentorise.smartcampus.jp.model;

import java.io.Serializable;

import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;

public class ParkingSerial implements Serializable, LocatedObject {
	private static final long serialVersionUID = -6427540022630812734L;

	private String name;
	private String description;
	private int slotsTotal;
	private int slotsAvailable;
	private double[] position;
	private Boolean monitored;

	// public Parking getParking() {
	// Parking parking = new Parking();
	// parking.setName(getName());
	// parking.setDescription(getDescription());
	// parking.setSlotsTotal(getSlotsTotal());
	// parking.setSlotsAvailable(getSlotsAvailable());
	// parking.setPosition(getPosition());
	// return parking;
	// }

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

	public Boolean isMonitored() {
		if (monitored == null) {
			return (getSlotsAvailable() > ParkingsHelper.PARKING_NOT_MONITORED_OLD);
		} else {
			return monitored;
		}
	}

	public void setMonitored(Boolean monitored) {
		this.monitored = monitored;
	}

	@Override
	public double[] location() {
		return getPosition();
	}

}
