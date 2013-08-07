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
package eu.trentorise.smartcampus.jp.custom.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.android.common.navigation.NavigationHelper;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.SmartCheckAlertsAdapter;
import eu.trentorise.smartcampus.jp.custom.SmartCheckParkingsAdapter;
import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;

public class AlertRoadsInfoDialog extends SherlockDialogFragment {

	public static final String ARG_ALERT = "alert";
	public static final String ARG_ALERTSLIST = "alertslist";
	private AlertRoadLoc alertRoad;
	private List<AlertRoadLoc> alertRoadsList;

	// private RadioGroup parkingsRadioGroup;

	public AlertRoadsInfoDialog() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.alertRoad = (AlertRoadLoc) this.getArguments().getSerializable(ARG_ALERT);
		alertRoadsList = (ArrayList<AlertRoadLoc>) this.getArguments().getSerializable(ARG_ALERTSLIST);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().setTitle(R.string.smart_check_alerts_dialog_title);
		View view = null;

		if (alertRoadsList != null) {
			view = inflater.inflate(R.layout.parkings_dialog_multi, container, false); // TODO
		} else {
			view = inflater.inflate(R.layout.parkings_dialog, container, false); // TODO
		}

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (alertRoadsList != null) {
			// multiple stops
			AlertRoadLoc firstParking = alertRoadsList.get(0);
			View rowiew = SmartCheckAlertsAdapter.buildAlertRoad(getSherlockActivity(), R.layout.smartcheck_alert_row,
					firstParking, null, null);

			LinearLayout entryLayout = (LinearLayout) getDialog().findViewById(R.id.parkings_dialog_entry); // TODO
			entryLayout.addView(rowiew, 0);
		} else if (alertRoad != null) {
			// single stop
			View rowView = SmartCheckAlertsAdapter.buildAlertRoad(getSherlockActivity(), R.layout.smartcheck_alert_row,
					alertRoad, null, null); // TODO

			LinearLayout entryLayout = (LinearLayout) getDialog().findViewById(R.id.parkings_dialog_entry); // TODO
			entryLayout.addView(rowView, 0);
		}

		Button btn_cancel = (Button) getDialog().findViewById(R.id.parkings_dialog_close); // TODO
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});

		Button btn_ok = (Button) getDialog().findViewById(R.id.parkings_dialog_directions); // TODO
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO : open details

				getDialog().dismiss();
			}
		});

	}
}
