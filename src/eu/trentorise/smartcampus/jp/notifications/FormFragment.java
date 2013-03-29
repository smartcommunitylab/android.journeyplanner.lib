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
package eu.trentorise.smartcampus.jp.notifications;

import java.util.Arrays;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.R;

public class FormFragment extends FeedbackFragment {
	private String[] buslines = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
			"A", "B", "C", "D" };
	private String[] delays = { "5 min", "10 min", "15 min", "30 min", "45 min", "1 hour", "more" };
	private String[] strike_times = { "12:00", "15:00", "18:00" };
	private SherlockFragmentActivity activity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.activity = getSherlockActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.bn_form, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		Bundle bundle = this.getArguments();
		String itemtext = bundle.getString("itemtext");
		int pos = bundle.getInt("position");
		if (itemtext != null)
			((TextView) activity.findViewById(R.id.title)).setText(itemtext);
		setupSpinners();

		TextView pos_label = (TextView) activity.findViewById(R.id.pos_label);
		LinearLayout pos_layout = (LinearLayout) activity.findViewById(R.id.pos_layout);
		TableLayout transport_checkboxes = (TableLayout) activity.findViewById(R.id.transport_checkboxes);
		LinearLayout busline_layout = (LinearLayout) activity.findViewById(R.id.busline_layout);
		LinearLayout delay_layout = (LinearLayout) activity.findViewById(R.id.delay_layout);
		LinearLayout trainnumber_layout = (LinearLayout) activity.findViewById(R.id.trainnumber_layout);
		LinearLayout strike_layout = (LinearLayout) activity.findViewById(R.id.strike_layout);
		Button notification_confirm = (Button) activity.findViewById(R.id.notification_confirm);
		notification_confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.toast_notification_sent,
						Toast.LENGTH_SHORT).show();
				// back to notifications home
			}
		});

		switch (pos) {
		// I know this switch sucks, but I promise I'll fix it later >.<
		case 0: // accident
			pos_label.setVisibility(View.VISIBLE);
			pos_layout.setVisibility(View.VISIBLE);
			transport_checkboxes.setVisibility(View.VISIBLE);
			busline_layout.setVisibility(View.GONE);
			delay_layout.setVisibility(View.GONE);
			trainnumber_layout.setVisibility(View.GONE);
			strike_layout.setVisibility(View.GONE);
			notification_confirm.setVisibility(View.VISIBLE);
			break;
		case 1: // bus delay
			pos_label.setVisibility(View.VISIBLE);
			pos_layout.setVisibility(View.VISIBLE);
			transport_checkboxes.setVisibility(View.GONE);
			busline_layout.setVisibility(View.VISIBLE);
			strike_layout.setVisibility(View.GONE);
			trainnumber_layout.setVisibility(View.GONE);
			delay_layout.setVisibility(View.VISIBLE);
			notification_confirm.setVisibility(View.VISIBLE);
			break;
		case 2: // train delay
			pos_label.setVisibility(View.VISIBLE);
			pos_layout.setVisibility(View.VISIBLE);
			transport_checkboxes.setVisibility(View.GONE);
			busline_layout.setVisibility(View.GONE);
			trainnumber_layout.setVisibility(View.VISIBLE);
			delay_layout.setVisibility(View.VISIBLE);
			strike_layout.setVisibility(View.GONE);
			notification_confirm.setVisibility(View.VISIBLE);
			break;
		case 3: // road works
		case 5: // traffic jam
		case 6: // diversion
			pos_label.setVisibility(View.VISIBLE);
			pos_layout.setVisibility(View.VISIBLE);
			transport_checkboxes.setVisibility(View.GONE);
			busline_layout.setVisibility(View.GONE);
			trainnumber_layout.setVisibility(View.GONE);
			delay_layout.setVisibility(View.GONE);
			strike_layout.setVisibility(View.GONE);
			notification_confirm.setVisibility(View.VISIBLE);
			break;
		case 4: // strike
			pos_label.setVisibility(View.VISIBLE);
			pos_layout.setVisibility(View.VISIBLE);
			transport_checkboxes.setVisibility(View.GONE);
			busline_layout.setVisibility(View.GONE);
			trainnumber_layout.setVisibility(View.GONE);
			delay_layout.setVisibility(View.GONE);
			strike_layout.setVisibility(View.VISIBLE);
			notification_confirm.setVisibility(View.VISIBLE);
			break;
		default:
			return;
		}

	}

	private void setupSpinners() {
		Spinner buslinespinner = (Spinner) activity.findViewById(R.id.buslinespinner);
		buslinespinner.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, Arrays
				.asList(buslines)));
		Spinner delayspinner = (Spinner) activity.findViewById(R.id.delayspinner);
		delayspinner
				.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, Arrays.asList(delays)));
		Spinner strikespinner = (Spinner) activity.findViewById(R.id.strikespinner);
		strikespinner.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, Arrays
				.asList(strike_times)));

	}
}
