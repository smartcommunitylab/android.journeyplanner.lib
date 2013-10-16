package eu.trentorise.smartcampus.jp.model;

import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;

public class AlertRoadLoc extends AlertRoad implements LocatedObject {
	private static final long serialVersionUID = -5793815978571171197L;
	
	public AlertRoadLoc(AlertRoad ar) {
		setAgencyId(ar.getAgencyId());
		setCreatorId(ar.getCreatorId());
		setCreatorType(ar.getCreatorType());
		setDescription(ar.getDescription());
		setEffect(ar.getEffect());
		setEntity(ar.getEntity());
		setFrom(ar.getFrom());
		setId(ar.getId());
		setNote(ar.getNote());
		setRoad(ar.getRoad());
		setTo(ar.getTo());
		setType(ar.getType());
	}
	
	@Override
	public double[] location() {
		return new double[] { Double.parseDouble(getRoad().getLat()), Double.parseDouble(getRoad().getLon()) };
	}

}
