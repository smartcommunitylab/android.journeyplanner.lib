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

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;
import eu.trentorise.smartcampus.jp.model.Step;

public class StepsListAdapter extends ArrayAdapter<Step> {

	Context mCtx;
	int resource;

	public StepsListAdapter(Context context, int resource, List<Step> objects) {
		super(context, resource, objects);
		this.mCtx = context;
		this.resource = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RowHolder holder = null;

		if (row == null) {
			row = ((Activity) mCtx).getLayoutInflater().inflate(resource, parent, false);
			holder = new RowHolder();
			holder.time = (TextView) row.findViewById(R.id.step_time);
			holder.description = (TextView) row.findViewById(R.id.step_description);
			holder.alerts = (TextView) row.findViewById(R.id.step_alerts);
			holder.parkingdataTime = (TextView) row.findViewById(R.id.step_parkingdata_time);
			holder.parkingdataPrice = (TextView) row.findViewById(R.id.step_parkingdata_price);
			holder.ttImage = (TextView) row.findViewById(R.id.step_img);
			row.setTag(holder);
		} else {
			holder = (RowHolder) row.getTag();
		}

		Step step = getItem(position);

		/*
		 * time
		 */
		holder.time.setText(step.getTime());

		/*
		 * description
		 */
		holder.description.setText(step.getDescription());

		/*
		 * image
		 */
		holder.ttImage.setCompoundDrawablesWithIntrinsicBounds(null, null, step.getImage().getDrawable(), null);

		/*
		 * alert
		 */
		if (step.getAlert() != null) {
			holder.alerts.setText(step.getAlert());
			holder.alerts.setVisibility(View.VISIBLE);
		} else {
			holder.alerts.setVisibility(View.GONE);
		}

		/*
		 * parking time
		 */
		Integer parkingSearchTimeMin = null;
		Integer parkingSearchTimeMax = null;
		if (step.getExtra() != null && step.getExtra().containsKey(ParkingsHelper.PARKING_EXTRA_SEARCHTIME)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> searchTime = (Map<String, Object>) step.getExtra().get(ParkingsHelper.PARKING_EXTRA_SEARCHTIME);
			if (searchTime.containsKey(ParkingsHelper.PARKING_EXTRA_SEARCHTIME_MIN)) {
				parkingSearchTimeMin = (Integer) searchTime.get(ParkingsHelper.PARKING_EXTRA_SEARCHTIME_MIN);
			}
			if (searchTime.containsKey(ParkingsHelper.PARKING_EXTRA_SEARCHTIME_MAX)) {
				parkingSearchTimeMax = (Integer) searchTime.get(ParkingsHelper.PARKING_EXTRA_SEARCHTIME_MAX);
			}
		}
		String parkingSearchTimeString = "";
		if (parkingSearchTimeMin != null && parkingSearchTimeMin > 0) {
			parkingSearchTimeString += parkingSearchTimeMin + "'";
		}
		if (parkingSearchTimeMax != null && parkingSearchTimeMax > 0) {
			parkingSearchTimeString += (parkingSearchTimeString.length() > 0 ? "-" + parkingSearchTimeMax
					: parkingSearchTimeMax) + "'";
		}

		
		if (parkingSearchTimeString.length() > 0) {
			holder.parkingdataTime.setText(mCtx.getString(R.string.step_parking_search, parkingSearchTimeString));
			holder.parkingdataTime.setVisibility(View.VISIBLE);
		} else {
			holder.parkingdataTime.setVisibility(View.GONE);
		}

		/*
		 * parking cost
		 */
		String parkingCost = "";
		if (step.getExtra() != null && step.getExtra().containsKey(ParkingsHelper.PARKING_EXTRA_COST)) {
			Map<String,Object> costData = (Map<String, Object>) step.getExtra().get(ParkingsHelper.PARKING_EXTRA_COST);
			parkingCost = (String)costData.get(ParkingsHelper.PARKING_EXTRA_COST_FIXED);
		}
		if (parkingCost.length() > 0) {
			holder.parkingdataPrice.setText(parkingCost);
			holder.parkingdataPrice.setText(mCtx.getString(R.string.step_parking_cost, parkingCost));
			holder.parkingdataPrice.setVisibility(View.VISIBLE);
		} else {
			holder.parkingdataPrice.setVisibility(View.GONE);
		}

		return row;
	}

	static class RowHolder {
		TextView time;
		TextView description;
		TextView alerts;
		TextView parkingdataTime;
		TextView parkingdataPrice;
		TextView ttImage;
	}

}
