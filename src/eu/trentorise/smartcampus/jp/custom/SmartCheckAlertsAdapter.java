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

import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoadType;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.helper.AlertRoadsHelper;
import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;

public class SmartCheckAlertsAdapter extends ArrayAdapter<AlertRoadLoc> {

	private Context mContext;
	private int layoutResourceId;

	// private Location myLocation;

	public SmartCheckAlertsAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.mContext = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AlertRoadLoc alertRoad = getItem(position);
		return buildAlertRoad(mContext, layoutResourceId, alertRoad, convertView, parent);
	}

	public static View buildAlertRoad(Context mContext, int layoutResourceId, AlertRoadLoc alertRoad, View convertView,
			ViewGroup parent) {

		View row = convertView;
		AlertRoadHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new AlertRoadHolder();
			// holder.alertRoadType = (TextView)
			// row.findViewById(R.id.smartcheck_alert_type);
			holder.alertRoadStreet = (TextView) row.findViewById(R.id.smartcheck_alert_street);
			holder.alertRoadDescription = (TextView) row.findViewById(R.id.smartcheck_alert_description);
			holder.alertRoadTypes = (LinearLayout) row.findViewById(R.id.smartcheck_alert_types);

			row.setTag(holder);
		} else {
			holder = (AlertRoadHolder) row.getTag();
		}

		// street
		holder.alertRoadStreet.setText(alertRoad.getRoad().getStreet());

		// description
		holder.alertRoadDescription.setText(extractShortDescription(alertRoad));

		// type
		holder.alertRoadTypes.removeAllViews();
		if (alertRoad.getChangeTypes().length > 0) {
			for (AlertRoadType type : alertRoad.getChangeTypes()) {
				ImageView typeImageView = new ImageView(mContext);
				typeImageView.setImageResource(AlertRoadsHelper.getDrawableResourceByType(type));
				float scale = mContext.getResources().getDisplayMetrics().density;
				int pixels = (int) (36 * scale + 0.5f);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(pixels, pixels);
				typeImageView.setLayoutParams(params);
				typeImageView.setPadding(0, 2, 0, 2);
				holder.alertRoadTypes.addView(typeImageView);
			}
			holder.alertRoadTypes.setVisibility(View.VISIBLE);
		} else {
			holder.alertRoadTypes.setVisibility(View.GONE);
		}

		return row;
	}

	/**
	 * @param alertRoad
	 * @return
	 */
	private static String extractShortDescription(AlertRoadLoc alertRoad) {
		if (alertRoad.getDescription().indexOf(':') > 0) {
			return alertRoad.getDescription().substring(alertRoad.getDescription().indexOf(':') + 1).trim();
		}
		return alertRoad.getDescription();
	}

	public static class AlertRoadHolder {
		// TextView alertRoadType;
		TextView alertRoadStreet;
		TextView alertRoadDescription;
		LinearLayout alertRoadTypes;
	}
}
