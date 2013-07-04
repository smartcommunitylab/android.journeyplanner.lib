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

import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

@SuppressLint("ValidFragment")
public class StopsInfoDialog extends SherlockDialogFragment {

	public interface OnDetailsClick {
		public void OnDialogDetailsClick(SmartCheckStop stop);
	}

	public static final String ARG_STOP = "stop";
	public static final String ARG_STOPS = "stops";
	private SmartCheckStop stopObject;
	private List<SmartCheckStop> stopObjectsList;
	private RadioGroup stopsRadioGroup;
	private OnDetailsClick listener;

	public StopsInfoDialog(OnDetailsClick listener) {
		this.listener = listener;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.stopObject = (SmartCheckStop) this.getArguments().getSerializable(ARG_STOP);
		this.stopObjectsList = (List<SmartCheckStop>) this.getArguments().getSerializable(ARG_STOPS);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().setTitle(R.string.smart_check_stops_type_mobility);
		View view = null;

		if (stopObjectsList != null) {
			view = inflater.inflate(R.layout.mapdialogmulti, container, false);
		} else {
			view = inflater.inflate(R.layout.mapdialog, container, false);
		}

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (stopObjectsList != null) {
			// multiple stops
			stopsRadioGroup = (RadioGroup) getDialog().findViewById(R.id.mapdialogmulti_rg);
			//stopsRadioGroup.removeAllViews();
			for (SmartCheckStop stop : stopObjectsList) {
				RadioButton rb = new RadioButton(getSherlockActivity());
				rb.setTag(stop);
				rb.setText(stop.getTitle());
//				rb.setTextColor(getResources().getColor(R.color.radio_text_dialog));
				//rb.setTextAppearance(getSherlockActivity(), R.style.RadioTextDialog);
				stopsRadioGroup.addView(rb);
			}
			stopsRadioGroup.getChildAt(0).setSelected(true);
		} else if (stopObject != null) {
			// single stop
			TextView msg = (TextView) getDialog().findViewById(R.id.mapdialog_msg);
			msg.setText(stopObject.getTitle());
			msg.setMovementMethod(new ScrollingMovementMethod());
		}

		Button btn_cancel = (Button) getDialog().findViewById(R.id.mapdialog_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});

		Button btn_ok = (Button) getDialog().findViewById(R.id.mapdialog_ok);
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SmartCheckStop stop = null;

				if (stopObjectsList != null) {
					RadioButton selectedRb = (RadioButton) stopsRadioGroup.findViewById(stopsRadioGroup
							.getCheckedRadioButtonId());
					stop = (SmartCheckStop) selectedRb.getTag();
				} else if (stopObject != null) {
					stop = stopObject;
				}

				listener.OnDialogDetailsClick(stop);

				getDialog().dismiss();
			}
		});

	}
}
