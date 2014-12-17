/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.jp.helper;

import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.data.message.Transport;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertAccident;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertDelay;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertParking;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertStrike;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.widget.ImageView;
import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.model.Step;

public class StepUtils {

	private Context mCtx;
	private Position fromPosition;
	private Position toPosition;
	private List<Leg> legs;

	public StepUtils(Context context, Position fromPosition, Position toPosition) {
		this.mCtx = context;
		this.fromPosition = fromPosition;
		this.toPosition = toPosition;
	}

	public List<Step> legs2steps(List<Leg> newLegs) {
		if (newLegs != null) {
			legs = newLegs;
		}

		List<Step> steps = new ArrayList<Step>();

		for (int index = 0; index < legs.size(); index++) {
			Leg leg = legs.get(index);

			Step step = new Step();
			// time
			step.setTime(Config.FORMAT_TIME_UI.format(new Date(leg.getStartime())));
			// description
			step.setDescription(buildDescription(leg, index));

			// image
			if (leg.getTransport() != null && leg.getTransport().getType() != null) {
				ImageView image = Utils.getImageByTType(mCtx, leg.getTransport().getType());
				step.setImage(image);
			}

			// alert
			if (Utils.containsAlerts(leg) && !leg.getTransport().getType().equals(TType.CAR)) {
				step.setAlert(buildAlerts(leg, index));
			}

			// extras
			step.setExtra(leg.getExtra());

			step.setLegIndex(index);
			steps.add(step);

			// extra step?
			if (leg.getTransport().getType().equals(TType.CAR) && leg.getTo().getStopId() != null) {
				step = new Step();
				step.setTime(Config.FORMAT_TIME_UI.format(new Date(legs.get(index + 1).getStartime())));
				step.setDescription(Html.fromHtml(mCtx.getString(R.string.step_car_leave) + " "
						+ bold(ParkingsHelper.getName(leg.getTo().getStopId().getId()))));
				step.setExtra(leg.getTo().getStopId().getExtra());
				step.setImage(Utils.getImageForParkingStation(mCtx, null));
				if (Utils.containsAlerts(leg)) {
					step.setAlert(buildAlerts(leg, index));
				}
				step.setLegIndex(index);
				steps.add(step);
			}
		}

		return steps;
	}

	private Spanned buildDescription(Leg leg, int index) {
		String desc = "";
		String from = leg.getFrom().getName() != null && leg.getFrom().getName().length() > 0 ? 
				  this.mCtx.getString(R.string.step_from) + " " + bold(placeName(leg.getFrom()))
				: "";
		String to = leg.getTo().getName() != null && leg.getTo().getName().length() > 0 ? 
				  this.mCtx.getString(R.string.step_to) + " " + bold(placeName(leg.getTo()))
				: "";

		TType tType = leg.getTransport().getType();

		if (tType.equals(TType.WALK)) {
			desc += mCtx.getString(R.string.step_walk);

			if (isBadString(leg.getFrom().getName())) {
				from = buildDescriptionFrom(index);
			}
			if (isBadString(leg.getTo().getName())) {
				to = buildDescriptionTo(index);
			}
		} else if (tType.equals(TType.BICYCLE)) {
			desc += mCtx.getString(R.string.step_bike_ride);

			if (leg.getFrom().getStopId() != null && 
					leg.getFrom().getStopId().getId() != null && leg.getFrom().getStopId().getId().length() > 0) {
				from = this.mCtx.getString(R.string.step_bike_pick_up)
						+ " "
						+ bold(ParkingsHelper.getParkingAgencyName(this.mCtx, leg.getFrom().getStopId().getAgencyId()) + " "
								+ leg.getFrom().getStopId().getId());
			} else if (isBadString(leg.getFrom().getName())) {
				from = buildDescriptionFrom(index);
			} else {
				from = "";
			}

			if (leg.getTo().getStopId() != null && 
					leg.getTo().getStopId().getId() != null && leg.getTo().getStopId().getId().length() > 0) {
				to = this.mCtx.getString(R.string.step_bike_leave)
						+ " "
						+ bold(ParkingsHelper.getParkingAgencyName(this.mCtx, leg.getFrom().getStopId().getAgencyId()) + " "
								+ leg.getTo().getStopId().getId());
			} else if (isBadString(leg.getTo().getName())) {
				to = buildDescriptionTo(index);
			} else {
				to = "";
			}
		} else if (tType.equals(TType.CAR)) {
			desc += mCtx.getString(R.string.step_car_drive);

			if (isBadString(leg.getFrom().getName())) {
				from = buildDescriptionFrom(index);
			}

			if (isBadString(leg.getTo().getName())) {
				to = buildDescriptionTo(index);
			}

			if (leg.getFrom().getStopId() != null) {
				if (from.length() > 0) {
					from += ", ";
				}
				from += this.mCtx.getString(R.string.step_car_pick_up) + " "
						+ bold(ParkingsHelper.getName(leg.getFrom().getStopId().getId()));
			}
		} else if (tType.equals(TType.BUS)) {
			desc += mCtx.getString(R.string.step_bus_take, RoutesHelper.getShortNameByRouteIdAndAgencyID(leg.getTransport()
					.getRouteId(), leg.getTransport().getAgencyId()));
		} else if (tType.equals(TType.TRAIN)) {
			desc += mCtx.getString(R.string.step_train_take, leg.getTransport().getTripId());
		}

		desc += (desc.length() > 0) ? ("<br/>" + from) : from;
		desc += (desc.length() > 0) ? ("<br/>" + to) : to;
		desc = desc.subSequence(0, 1).toString().toUpperCase(Locale.getDefault()) + desc.substring(1);
		return Html.fromHtml(desc);
	}

	private String buildDescriptionFrom(int index) {
		String from = "";

		if (index == 0) {
			from = this.mCtx.getString(R.string.step_from) + " " + bold(fromPosition.getName());
		} else if (legs.get(index - 1) == null || isBadString(legs.get(index - 1).getTo().getName())) {
			from = this.mCtx.getString(R.string.step_move);
		} else {
			from = this.mCtx.getString(R.string.step_from) + " " + bold(placeName(legs.get(index - 1).getTo()));
		}

		return from;
	}

	private String placeName(Position p) {
		if (p.getStopId() != null) {
			String name = ParkingsHelper.getName(p.getStopId().getId());
			return name != null && name.length() > 0 ? name : p.getName();
		}
		return p.getName();
	}

	private String buildDescriptionTo(int index) {
		String to = "";

		if ((index + 1 == legs.size())) {
			to = this.mCtx.getString(R.string.step_to) + " " + bold(toPosition.getName());
		} else if (legs.get(index + 1) == null || isBadString(legs.get(index + 1).getFrom().getName())) {
			to = "";
		} else {
			to = this.mCtx.getString(R.string.step_to) + " " + bold(placeName(legs.get(index + 1).getFrom()));
		}

		return to;
	}

	private boolean isBadString(String s) {
		if (s.contains("road") || s.contains("sidewalk") || s.contains("path") || s.contains("steps") || s.contains("track")
				|| s.contains("node ") || s.contains("way ")) {
			return true;
		}
		return false;
	}

	private String buildAlerts(Leg leg, int index) {
		// delay
		String delay = "";
		if (leg.getAlertDelayList() != null && !leg.getAlertDelayList().isEmpty()) {
			for (AlertDelay ad : leg.getAlertDelayList()) {
				if (ad.getDelay() > 0) {
					delay += this.mCtx.getString(R.string.step_delay) + " " + millis2mins(ad.getDelay()) + " min";
				}
			}
		}
		if (leg.getAlertAccidentList() != null && !leg.getAlertAccidentList().isEmpty()) {
			if (delay.length() > 0) {
				delay += "\n";
			}
			for (AlertAccident aa : leg.getAlertAccidentList()) {
				if (aa.getDescription() != null) {
					delay += aa.getDescription();
				}
			}
		}

		if (leg.getAlertAccidentList() != null && !leg.getAlertRoadList().isEmpty()) {
			if (delay.length() > 0) {
				delay += "\n";
			}
			for (AlertRoad aa : leg.getAlertRoadList()) {
				if (aa.getDescription() != null) {
					delay += aa.getDescription();
				}
			}
		}

		if (leg.getAlertStrikeList() != null && !leg.getAlertStrikeList().isEmpty()) {
			if (delay.length() > 0) {
				delay += "\n";
			}
			for (AlertStrike aa : leg.getAlertStrikeList()) {
				if (aa.getDescription() != null) {
					delay += aa.getDescription();
				}
			}
		}
		if (leg.getAlertParkingList() != null && !leg.getAlertParkingList().isEmpty()) {
			if (delay.length() > 0) {
				delay += "\n";
			}
			for (AlertParking aa : leg.getAlertParkingList()) {
				if (aa.getDescription() != null) {
					// check if it is 'from' station
					if (aa.getPlace() != null && aa.getPlace().getId() != null &&
						leg.getFrom().getStopId() != null &&	
						aa.getPlace().getId().equals(leg.getFrom().getStopId().getId())) 
					{
						if (leg.getTransport().getType().equals(TType.CAR)) {
							delay += mCtx.getString(R.string.parking_pickup_alert_car, //ParkingsHelper.getName(aa.getPlace().getId()),
									aa.getNoOfvehicles());
						}
						else if (leg.getTransport().getType().equals(TType.BICYCLE)) {
							delay += mCtx.getString(R.string.parking_pickup_alert_bike, //ParkingsHelper.getName(aa.getPlace().getId()),
									aa.getNoOfvehicles());
						}
						
					// otherwise if this is alert for the target place	
					} else if (leg.getTo().getStopId() != null && leg.getTo().getStopId().getId() != null) {
						delay += mCtx.getString(R.string.parking_alert, //ParkingsHelper.getName(aa.getPlace().getId()),
								aa.getPlacesAvailable());
					}
					
				}
			}
		}
		return delay;
	}

	private String bold(String s) {
		return "<b>" + s + "</b>";
	}

	private int millis2mins(long millis) {
		return (int) ((millis / (1000 * 60)) % 60);
	}
}
