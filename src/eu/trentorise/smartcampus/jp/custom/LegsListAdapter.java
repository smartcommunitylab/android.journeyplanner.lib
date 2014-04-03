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
package eu.trentorise.smartcampus.jp.custom;

import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.Position;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.helper.LegContentRenderer;
import eu.trentorise.smartcampus.jp.helper.Utils;

public class LegsListAdapter extends ArrayAdapter<Leg> {

	Context context;
	int layoutResourceId;

	Position fromPosition;
	Position toPosition;
	List<Leg> legs;

	private LegContentRenderer renderer;

	public LegsListAdapter(Context context, int layoutResourceId, Position fromPosition, Position toPosition,
			List<Leg> legs) {
		super(context, layoutResourceId, legs);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.fromPosition = fromPosition;
		this.toPosition = toPosition;
		this.legs = legs;
		this.renderer = new LegContentRenderer(this.context, fromPosition, toPosition, this.legs);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RowHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RowHolder();
			holder.time = (TextView) row.findViewById(R.id.leg_time);
			holder.description = (TextView) row.findViewById(R.id.leg_description);
			holder.alerts = (TextView) row.findViewById(R.id.leg_alerts);
			// holder.transportType = (FrameLayout)
			// row.findViewById(R.id.leg_transporttype);

			row.setTag(holder);
		} else {
			holder = (RowHolder) row.getTag();
		}

		Leg leg = legs.get(position);

		// time
		Date time = new Date(leg.getStartime());
		String timeFromString = Config.FORMAT_TIME_UI.format(time);
		holder.time.setText(timeFromString);
		
		if(position==0)
			leg.setFrom(fromPosition);
		else if(position==getCount()-1)
			leg.setTo(toPosition);

		// description
		holder.description.setText(renderer.buildDescription(leg, position));

		ImageView imgv = Utils.getImageByTType(getContext(), leg.getTransport().getType());
		if (imgv.getDrawable() != null) {
			holder.description.setCompoundDrawablesWithIntrinsicBounds(null, null, imgv.getDrawable(), null);
		}

		// alert
		if (Utils.containsAlerts(leg)) {
			holder.alerts.setText(renderer.buildAlerts(leg, position));
			holder.alerts.setVisibility(View.VISIBLE);
		} else {
			holder.alerts.setVisibility(View.GONE);
		}

		// ***** TEST *****
		// if (leg.getTransport().getType().equals(TType.TRAIN)) {
		// holder.alerts.setText("Demo alert: " + renderer.millis2mins(5 * 60 *
		// 1000));
		// holder.alerts.setVisibility(View.VISIBLE);
		// }
		// ****************

		// holder.transportType.removeAllViews();
		// ImageView imgv = Helper.getImageByTType(getContext(),
		// leg.getTransport().getType());
		// if (imgv.getDrawable() != null) {
		// holder.transportType.addView(imgv);
		// }

		return row;
	}

	static class RowHolder {
		TextView time;
		TextView description;
		TextView alerts;
		// FrameLayout transportType;
	}

}
