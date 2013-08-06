package eu.trentorise.smartcampus.jp.model;

import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;

public class AlertRoadLoc extends AlertRoad implements LocatedObject {
	private static final long serialVersionUID = -5793815978571171197L;

	@Override
	public double[] location() {
		return new double[] { Double.parseDouble(getRoad().getLat()), Double.parseDouble(getRoad().getLon()) };
	}

}
