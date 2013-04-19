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

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;

import java.util.ArrayList;
import java.util.List;

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
import eu.trentorise.smartcampus.jp.model.Sparking;

public class ParkingsInfoDialog extends SherlockDialogFragment {

	public static final String ARG_PARKING = "parking";
	public static final String ARG_PARKINGS = "parkings";
	private Parking parking;
	private List<Parking> parkingsList;
	private RadioGroup parkingsRadioGroup;

	public ParkingsInfoDialog() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Sparking sparking = (Sparking) this.getArguments().getSerializable(ARG_PARKING);
		this.parking = sparking.getParking();

		ArrayList<Sparking> sparkingsList = (ArrayList<Sparking>) this.getArguments().getSerializable(ARG_PARKINGS);
		if (sparkingsList != null) {
			this.parkingsList = new ArrayList<Parking>();
			for (Sparking sp : sparkingsList) {
				parkingsList.add(sp.getParking());
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().setTitle(R.string.smart_check_stops_type_mobility);
		View view = null;

		if (parkingsList != null) {
			view = inflater.inflate(R.layout.mapdialogmulti, container, false);
		} else {
			view = inflater.inflate(R.layout.mapdialog, container, false);
		}

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (parkingsList != null) {
			// multiple stops
			parkingsRadioGroup = (RadioGroup) getDialog().findViewById(R.id.mapdialogmulti_rg);
			parkingsRadioGroup.removeAllViews();
			for (Parking p : parkingsList) {
				RadioButton rb = new RadioButton(getSherlockActivity());
				rb.setTag(p);
				if (!p.getName().equalsIgnoreCase(p.getDescription())) {
					rb.setText(p.getName() + " " + p.getDescription());
				} else {
					rb.setText(p.getName());
				}
				parkingsRadioGroup.addView(rb);
			}
			parkingsRadioGroup.getChildAt(0).setSelected(true);
		} else if (parking != null) {
			// single stop
			TextView msg = (TextView) getDialog().findViewById(R.id.mapdialog_msg);
			if (!parking.getName().equalsIgnoreCase(parking.getDescription())) {
				msg.setText(parking.getName() + " " + parking.getDescription());
			} else {
				msg.setText(parking.getName());
			}
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
				// Parking stop = null;
				//
				// if (parkingsList != null) {
				// RadioButton selectedRb = (RadioButton)
				// parkingsRadioGroup.findViewById(parkingsRadioGroup
				// .getCheckedRadioButtonId());
				// stop = (Parking) selectedRb.getTag();
				// } else if (parking != null) {
				// stop = parking;
				// }
				//
				// StopSelectActivity stopSelectActivity = (StopSelectActivity)
				// getSherlockActivity();
				// Intent stopSelectActivityIntent =
				// stopSelectActivity.getIntent();
				// stopSelectActivityIntent.putExtra(StopSelectActivity.ARG_STOP,
				// stop);
				// stopSelectActivity.setResult(Activity.RESULT_OK,
				// stopSelectActivityIntent);
				// stopSelectActivity.finish();
				//
				getDialog().dismiss();
			}
		});

	}
}
