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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.SmartCheckAlertsAdapter;
import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;

@SuppressLint("ValidFragment")
public class AlertRoadsInfoDialog extends SherlockDialogFragment {

	public interface OnDetailsClick {
		public void OnDialogDetailsClick(AlertRoadLoc alert);
	}

	public static final String ARG_ALERT = "alert";
	public static final String ARG_ALERTSLIST = "alertslist";
	private AlertRoadLoc alertRoad;
	private List<AlertRoadLoc> alertRoadsList;
	private RadioGroup radioGroup;
	private OnDetailsClick listener;

	public AlertRoadsInfoDialog(OnDetailsClick listener) {
		this.listener = listener;
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
			view = inflater.inflate(R.layout.smartcheck_alerts_dialog_multi, container, false);
			getDialog().setTitle(alertRoadsList.get(0).getRoad().getStreet());
		} else {
			view = inflater.inflate(R.layout.smartcheck_alerts_dialog, container, false);
		}

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (alertRoadsList != null) {
			// multiple
			radioGroup = (RadioGroup) getDialog().findViewById(R.id.smartcheck_alerts_dialog_multi_rg);
			radioGroup.removeAllViews();
			for (int i = 0; i < alertRoadsList.size(); i++) {
				AlertRoadLoc alertRoad = alertRoadsList.get(i);
				RadioButton rb = new RadioButton(getSherlockActivity());
				rb.setTag(alertRoad);
				rb.setText(alertRoad.getDescription());
				rb.setMaxLines(3);
				rb.setEllipsize(TruncateAt.END);

				radioGroup.addView(rb);
				if (i == 0) {
					radioGroup.check(rb.getId());
				}
			}
		} else if (alertRoad != null) {
			// single
			View rowView = SmartCheckAlertsAdapter.buildAlertRoad(getSherlockActivity(), R.layout.smartcheck_alert_row,
					alertRoad, null, null);

			LinearLayout entryLayout = (LinearLayout) getDialog().findViewById(R.id.smartcheck_alerts_dialog_entry);
			entryLayout.addView(rowView, 0);
		}

		Button btn_cancel = (Button) getDialog().findViewById(R.id.smartcheck_alerts_dialog_close);
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});

		Button btn_ok = (Button) getDialog().findViewById(R.id.smartcheck_alerts_dialog_details);
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertRoadLoc alert = null;

				if (alertRoadsList != null) {
					RadioButton selectedRb = (RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
					alert = (AlertRoadLoc) selectedRb.getTag();
				} else if (alertRoad != null) {
					alert = alertRoad;
				}

				listener.OnDialogDetailsClick(alert);

				getDialog().dismiss();
			}
		});

	}
}
