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

import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.StopId;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.data.message.Transport;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertDelay;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertType;
import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Stop;
import it.sayservice.platform.smartplanner.data.message.otpbeans.StopTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.StoptimesArrayAdapter;
import eu.trentorise.smartcampus.jp.custom.data.BasicAlert;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.jp.helper.processor.GetBroadcastDataProcessor;
import eu.trentorise.smartcampus.jp.helper.processor.SubmitAlertProcessor;

public class BT_DelayFormFragment extends FeedbackFragment {

	public static final String ARG_AGENCYID = "agencyId";

	private List<Route> routes = new ArrayList<Route>();
	private List<Stop> stops = new ArrayList<Stop>();
	private List<StopTime> stoptimes = new ArrayList<StopTime>();

	private Spinner routesSpinner;
	private Spinner stopsSpinner;
	private Spinner stoptimesSpinner;
	private EditText delayEditText;

	private ArrayAdapter<Route> routesAdapter;
	private ArrayAdapter<Stop> stopsAdapter;
	private ArrayAdapter<StopTime> stopTimesAdapter;

	private Route selectedRoute;
	private Stop selectedStop;
	private StopTime selectedStoptime;

	private String[] agencyIds;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.bn_form_bt_delay, container, false);
	}

	@Override
	public void onPause() {
		super.onPause();
		// hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		delayEditText = (EditText) getView().findViewById(R.id.bn_bt_delay_delay);
		imm.hideSoftInputFromWindow(delayEditText.getWindowToken(), 0);
	}

	@Override
	public void onStart() {
		super.onStart();

		Bundle bundle = this.getArguments();

		agencyIds = bundle.getStringArray(ARG_AGENCYID);

		String itemtext = bundle.getString("itemtext");
		if (itemtext != null) {
			((TextView) getSherlockActivity().findViewById(R.id.title)).setText(itemtext);
		}

		setupSpinners();

		delayEditText = (EditText) getView().findViewById(R.id.bn_bt_delay_delay);

		/*
		 * calls
		 */
		// SCAsyncTask<String, Void, Map<String, List<?>>> task = new
		// SCAsyncTask<String, Void, Map<String, List<?>>>(
		// getSherlockActivity(), new
		// GetBroadcastDataProcessor(getSherlockActivity(), routesAdapter,
		// stopsAdapter, stopTimesAdapter));
		// task.execute("10");

		routesAdapter.clear();
		for (Route r : RoutesHelper.getRoutesList(getSherlockActivity(), agencyIds)) {
			routesAdapter.add(r);
		}

		routesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				stopsAdapter.clear();
				stopTimesAdapter.clear();

				selectedRoute = routesAdapter.getItem(position);

				SCAsyncTask<String, Void, Map<String, List<?>>> task = new SCAsyncTask<String, Void, Map<String, List<?>>>(
						getSherlockActivity(), new GetBroadcastDataProcessor(getSherlockActivity(), routesAdapter,
								stopsAdapter, stopTimesAdapter));
				task.execute(selectedRoute.getId().getAgency(), selectedRoute.getId().getId());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		stopsSpinner.setOnItemSelectedListener(new OnItemSelectedListenerWrapper(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				stopTimesAdapter.clear();

				selectedStop = stops.get(stopsSpinner.getSelectedItemPosition());

				SCAsyncTask<String, Void, Map<String, List<?>>> task = new SCAsyncTask<String, Void, Map<String, List<?>>>(
						getSherlockActivity(), new GetBroadcastDataProcessor(getSherlockActivity(), routesAdapter,
								stopsAdapter, stopTimesAdapter));
				task.execute(selectedRoute.getId().getAgency(), selectedRoute.getId().getId(), selectedStop.getId());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		}));

		stoptimesSpinner.setOnItemSelectedListener(new OnItemSelectedListenerWrapper(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selectedStoptime = stoptimes.get(stoptimesSpinner.getSelectedItemPosition());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		}));

		Button sendBtn = (Button) getView().findViewById(R.id.bn_bt_delay_send);
		sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				selectedRoute = routesSpinner.getSelectedItemPosition() >= 0 ? routes.get(routesSpinner
						.getSelectedItemPosition()) : null;
				selectedStop = stopsSpinner.getSelectedItemPosition() >= 0 ? stops.get(stopsSpinner.getSelectedItemPosition())
						: null;
				selectedStoptime = stoptimesSpinner.getSelectedItemPosition() >= 0 ? stoptimes.get(stoptimesSpinner
						.getSelectedItemPosition()) : null;

				if (selectedRoute == null || selectedStop == null || selectedStoptime == null) {
					Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.err_delay_fields_empty,
							Toast.LENGTH_SHORT).show();
					return;
				}

				String agencyId = selectedRoute.getId().getAgency();

				TType type = TType.BUS;
				if (agencyId.equals("12")) {
					type = TType.BUS;
				} else if (agencyId.equals("5") || agencyId.equals("6") || agencyId.equals("10")) {
					type = TType.TRAIN;
				}

				String routeId = selectedRoute.getId().getId();
				String tripId = selectedStoptime.getTrip().getId();
				Transport transport = new Transport(type, agencyId, routeId, tripId);

				Calendar now = Calendar.getInstance();

				AlertDelay alertDelay = new AlertDelay();
				// alertDelay.setPosition(position);
				alertDelay.setTransport(transport);
				String delayString = delayEditText.getText().toString().trim();
				if (delayString.length() > 0) {
					alertDelay.setDelay(Long.parseLong(delayString) * 60L * 1000L); // milliseconds
				} else {
					long diff = now.getTimeInMillis() - selectedStoptime.getTime();
					Calendar cdiff = Calendar.getInstance();
					cdiff.setTimeInMillis(diff);
					alertDelay.setDelay(diff);
				}

				// validity 30 minutes
				// long from = now.getTimeInMillis();
				// long to = from + (1000L * 60 * 30);
				/*
				 * this should be different -> selectedStoptime.getStartTime()
				 * and selectedStoptime.getStopTime()
				 */
				long from = selectedStoptime.getTime();
				long to = now.getTimeInMillis() + (1000L * 60 * 30);

				// tripId_from_to
				String id = tripId + "_" + from + "_" + to;
				alertDelay.setId(id);
				// alertDelay.setDescription(description);
				// alertDelay.setCreatorId(creatorId);
				alertDelay.setCreatorType(CreatorType.USER);

				alertDelay.setFrom(from);
				alertDelay.setTo(to);
				// alertDelay.setEffect(effect);
				alertDelay.setType(AlertType.DELAY);

				StopId stopId = new StopId();
				stopId.setId(selectedStop.getId());
				Position position = new Position(selectedStop.getName(), stopId, null, Double.toString(selectedStop
						.getLongitude()), Double.toString(selectedStop.getLatitude()));
				alertDelay.setPosition(position);

				// int delayInMin = Math.max((int) (alertDelay.getDelay() / 1000
				// / 60), 1);
				// String note = type.toString() + " " +
				// selectedRoute.getRouteShortName() + " run "
				// + Utils.millis2time(selectedStoptime.getTime()) + " is " +
				// delayInMin
				// + "min. late (signaled from stop " + selectedStop.getName() +
				// ")";
				String note = getDestinationName(selectedRoute.getRouteLongName());
				alertDelay.setNote(note);

				BasicAlert ba = new BasicAlert(AlertType.DELAY, alertDelay);
				SCAsyncTask<BasicAlert, Void, Void> task = new SCAsyncTask<BasicAlert, Void, Void>(getSherlockActivity(),
						new SubmitAlertProcessor(getSherlockActivity()));
				task.execute(ba);
			}
		});

	}

	private void setupSpinners() {
		routesSpinner = (Spinner) getView().findViewById(R.id.bn_bt_delay_routes);
		routesAdapter = new ArrayAdapter<Route>(getSherlockActivity(), R.layout.dd_list, R.id.dd_textview, routes);
		routesSpinner.setAdapter(routesAdapter);

		stopsSpinner = (Spinner) getView().findViewById(R.id.bn_bt_delay_stops);
		stopsAdapter = new ArrayAdapter<Stop>(getSherlockActivity(), R.layout.dd_list, R.id.dd_textview, stops);
		stopsSpinner.setAdapter(stopsAdapter);

		stoptimesSpinner = (Spinner) getView().findViewById(R.id.bn_bt_delay_timetable);
		stopTimesAdapter = new StoptimesArrayAdapter(getSherlockActivity(), R.layout.dd_list, R.id.dd_textview, stoptimes);
		stoptimesSpinner.setAdapter(stopTimesAdapter);
	}

	private String getDestinationName(String routeName) {
		String[] split = routeName.split("-");
		return split[split.length - 1].trim();
	}

	private class OnItemSelectedListenerWrapper implements OnItemSelectedListener {

		private int lastPosition;
		private OnItemSelectedListener listener;

		public OnItemSelectedListenerWrapper(OnItemSelectedListener aListener) {
			lastPosition = 0;
			listener = aListener;
		}

		@Override
		public void onItemSelected(AdapterView<?> aParentView, View aView, int aPosition, long anId) {
			if (lastPosition != aPosition) {
				listener.onItemSelected(aParentView, aView, aPosition, anId);
			}
			lastPosition = aPosition;
		}

		@Override
		public void onNothingSelected(AdapterView<?> aParentView) {
			listener.onNothingSelected(aParentView);
		}
	}
}
