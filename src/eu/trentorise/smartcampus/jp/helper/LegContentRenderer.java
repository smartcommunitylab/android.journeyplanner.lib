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
import it.sayservice.platform.smartplanner.data.message.alerts.AlertDelay;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import eu.trentorise.smartcampus.jp.R;

public class LegContentRenderer {

	private Context ctx;
	private Position fromPosition;
	private Position toPosition;
	private List<Leg> legs;

	public LegContentRenderer(Context context, Position fromPosition, Position toPosition, List<Leg> legs) {
		this.ctx = context;
		this.fromPosition = fromPosition;
		this.toPosition = toPosition;
		this.legs = legs;
	}

	public Spanned buildDescription(Leg leg, int index) {
		String desc = "";
		String from = this.ctx.getString(R.string.leg_from) + " " + bold(leg.getFrom().getName());
		String to = " " + this.ctx.getString(R.string.leg_to) + " " + bold(leg.getTo().getName());

		TType tType = leg.getTransport().getType();

		if (tType.equals(TType.WALK)) {
			if (isBadString(leg.getFrom().getName())) {
				from = buildDescriptionFrom(index);
			}

			if (isBadString(leg.getTo().getName())) {
				to = buildDescriptionTo(index);
			}
		} else if (tType.equals(TType.BICYCLE)) {
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
				from += this.ctx.getString(R.string.leg_bike_pick_up) + " "
						+ bold(leg.getFrom().getStopId().getAgencyId() + " " + leg.getFrom().getStopId().getId());
			}

			if (leg.getTo().getStopId() != null) {
				if (from.length() > 0 || to.length() > 0) {
					to += ", ";
				}
				to += this.ctx.getString(R.string.leg_bike_leave) + " "
						+ bold(leg.getTo().getStopId().getAgencyId() + " " + leg.getTo().getStopId().getId());
			}
		} else if (tType.equals(TType.CAR)) {
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
				from += this.ctx.getString(R.string.leg_car_pick_up) + " "
						+ bold(leg.getFrom().getStopId().getAgencyId() + " " + leg.getFrom().getStopId().getId());
			}

			if (leg.getTo().getStopId() != null) {
				if (from.length() > 0 || to.length() > 0) {
					to += ", ";
				}
				to += this.ctx.getString(R.string.leg_car_leave) + " "
						+ bold(leg.getTo().getStopId().getAgencyId() + " " + leg.getTo().getStopId().getId());
			}
		} else if (tType.equals(TType.BUS)) {
			// TODO
			to += " (" + this.ctx.getString(R.string.leg_bus_line) + " " + bold(RoutesHelper.getShortNameByRouteIdAndAgencyID(leg.getTransport().getRouteId(),leg.getTransport().getAgencyId())) + ")";
		} else if (tType.equals(TType.TRAIN)) {
			to += " (" + this.ctx.getString(R.string.leg_bus_line) + " " + bold(leg.getTransport().getTripId()) + ")";
		}

		desc = from + to;
		desc = desc.subSequence(0, 1).toString().toUpperCase() + desc.substring(1);
		return Html.fromHtml(desc);
	}

	private String buildDescriptionFrom(int index) {
		String from = "";

		if (index == 0) {
			from = this.ctx.getString(R.string.leg_from) + " " + bold(fromPosition.getName());
		} else if (legs.get(index - 1) == null || isBadString(legs.get(index - 1).getTo().getName())) {
			from = this.ctx.getString(R.string.leg_move);
		} else {
			from = this.ctx.getString(R.string.leg_from) + " " + bold(legs.get(index - 1).getTo().getName());
		}

		return from;
	}

	private String buildDescriptionTo(int index) {
		String to = "";

		if ((index + 1 == legs.size())) {
			to = this.ctx.getString(R.string.leg_to) + " " + bold(toPosition.getName());
		} else if (legs.get(index + 1) == null || isBadString(legs.get(index + 1).getFrom().getName())) {
			to = "";
		} else {
			to = this.ctx.getString(R.string.leg_to) + " " + bold(legs.get(index + 1).getFrom().getName());
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

	public String buildAlerts(Leg leg, int index) {
		// delay
		String delay = "";
		if (!leg.getAlertDelayList().isEmpty()) {
			for (AlertDelay ad : leg.getAlertDelayList()) {
				if (ad.getDelay() > 0) {
					delay = this.ctx.getString(R.string.leg_delay) + " " + millis2mins(ad.getDelay()) + " min";
				}
			}
		}

		return delay;
	}

	private String bold(String s) {
		return "<b>" + s + "</b>";
	}

	public int millis2mins(long millis) {
		return (int) ((millis / (1000 * 60)) % 60);
	}
}
