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
package eu.trentorise.smartcampus.jp;

import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.journey.SingleJourney;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.jp.custom.DialogHandler;
import eu.trentorise.smartcampus.jp.custom.StepsListAdapter;
import eu.trentorise.smartcampus.jp.helper.StepUtils;
import eu.trentorise.smartcampus.jp.helper.processor.SaveItineraryProcessor;
import eu.trentorise.smartcampus.jp.model.Step;
import eu.trentorise.smartcampus.mobilityservice.model.BasicItinerary;

public class ItineraryFragment extends SherlockFragment {

	public static final String ITINERARY = "itineraries";
	public static final String JOURNEY = "journey";
	public static final String LEGS = "legs";
	public static final String STEPS = "steps";

	private StepUtils stepUtils;

	private SingleJourney singleJourney;
	private Itinerary itinerary;
	private List<Step> steps;
	private StepsListAdapter stepsListAdapter;

	public static ItineraryFragment newInstance(SingleJourney singleJourney,
			Itinerary itinerary) {
		ItineraryFragment f = new ItineraryFragment();
		f.singleJourney = singleJourney;
		f.itinerary = itinerary;
		return f;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (singleJourney != null) {
			outState.putSerializable(JOURNEY, singleJourney);
		}
		if (itinerary != null) {
			outState.putSerializable(ITINERARY, itinerary);
		}
		if (steps != null) {
			outState.putSerializable(LEGS, new ArrayList<Step>(steps));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (getArguments() != null) {
			Bundle bundle = getArguments();
			if (bundle.containsKey(JOURNEY)) {
				singleJourney = (SingleJourney) bundle.getSerializable(JOURNEY);
			}
			if (bundle.containsKey(ITINERARY)) {
				itinerary = (Itinerary) bundle.getSerializable(ITINERARY);
			}
			if (bundle.containsKey(STEPS)) {
				steps = (List<Step>) bundle.getSerializable(STEPS);
			}
		}

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(JOURNEY)) {
				singleJourney = (SingleJourney) savedInstanceState
						.getSerializable(JOURNEY);
			}
			if (savedInstanceState.containsKey(ITINERARY)) {
				itinerary = (Itinerary) savedInstanceState
						.getSerializable(ITINERARY);
			}
			if (savedInstanceState.containsKey(STEPS)) {
				steps = (List<Step>) savedInstanceState.getSerializable(STEPS);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.itinerary, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		// legs = itinerary.getLeg();
		// Converting legs to steps.
		// You can have more steps than legs!
		stepUtils = new StepUtils(getSherlockActivity(),
				singleJourney.getFrom(), singleJourney.getTo());
		steps = stepUtils.legs2steps(itinerary.getLeg());

		final ListView stepsListView = (ListView) getView().findViewById(
				R.id.itinerary_steps);

		// date & time
		TextView dateTextView = (TextView) getView().findViewById(
				R.id.itinerary_date);
		dateTextView.setText(Config.FORMAT_DATE_UI.format(new Date(itinerary
				.getStartime())));
		TextView timeTextView = (TextView) getView().findViewById(
				R.id.itinerary_time);
		timeTextView.setText(Config.FORMAT_TIME_UI.format(new Date(itinerary
				.getStartime())));

		// promoted
		if (itinerary.isPromoted()) {
			TextView promotedTextView = (TextView) getView().findViewById(
					R.id.promoted_textview);
			promotedTextView.setVisibility(View.VISIBLE);
		}

		// add header (before setAdapter or it won't work!)
		if (stepsListView.getHeaderViewsCount() == 0) {
			View headerView = getSherlockActivity().getLayoutInflater()
					.inflate(R.layout.itinerary_step, stepsListView, false);
			TextView headerTimeTextView = (TextView) headerView
					.findViewById(R.id.step_time);
			TextView headerDescTextView = (TextView) headerView
					.findViewById(R.id.step_description);
			headerTimeTextView.setText(Config.FORMAT_TIME_UI.format(itinerary
					.getStartime()));
			headerDescTextView.setText(singleJourney.getFrom().getName());
			headerDescTextView.setTextAppearance(getSherlockActivity(),
					android.R.style.TextAppearance_Medium);
			stepsListView.addHeaderView(headerView);
		}
		// add footer (before setAdapter or it won't work!)
		if (stepsListView.getFooterViewsCount() == 0) {
			View footerView = getSherlockActivity().getLayoutInflater()
					.inflate(R.layout.itinerary_step, stepsListView, false);
			TextView footerTimeTextView = (TextView) footerView
					.findViewById(R.id.step_time);
			TextView footerDescTextView = (TextView) footerView
					.findViewById(R.id.step_description);
			footerTimeTextView.setText(Config.FORMAT_TIME_UI.format(itinerary
					.getEndtime()));
			footerDescTextView.setText(singleJourney.getTo().getName());
			footerDescTextView.setTextAppearance(getSherlockActivity(),
					android.R.style.TextAppearance_Medium);
			stepsListView.addFooterView(footerView);
		}

		if (stepsListAdapter == null) {
			stepsListAdapter = new StepsListAdapter(getSherlockActivity(),
					R.layout.itinerary_step, steps);
		}
		stepsListView.setAdapter(stepsListAdapter);

		stepsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int idx = position == 0 ? -1 : position > steps.size() ? steps.size() : steps.get(position-1).getLegIndex();
				showMap(idx);
			}
		});

		Button saveItineraryBtn = (Button) getView().findViewById(
				R.id.itinerary_save);
		saveItineraryBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String suggestedName = "";

				Dialog dialog = new ItineraryNameDialog(getActivity(),
						new DialogHandler<String>() {
							@Override
							public void handleSuccess(String name) {
								BasicItinerary basicItinerary = new BasicItinerary();
								basicItinerary.setData(itinerary);
								basicItinerary.setOriginalFrom(singleJourney
										.getFrom());
								basicItinerary.setOriginalTo(singleJourney
										.getTo());
								basicItinerary.setName(name);
								SCAsyncTask<BasicItinerary, Void, Void> task = new SCAsyncTask<BasicItinerary, Void, Void>(
										getSherlockActivity(),
										new SaveItineraryProcessor(
												getSherlockActivity()));
								task.execute(basicItinerary);
							}
						}, suggestedName);
				dialog.show();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_map) {
			showMap(null);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.itinerarymenu, menu);
	}

	private void showMap(Integer pos) {
		Intent i = new Intent(getActivity(), LegMapActivity.class);
		if (itinerary != null && itinerary.getLeg() != null) {
			i.putExtra(LegMapActivity.LEGS,
					new ArrayList<Leg>(itinerary.getLeg()));
			try {
				long date = Config.FORMAT_DATE_SMARTPLANNER.parse(
						singleJourney.getDate()).getTime();
				i.putExtra(LegMapActivity.DATE, date);
			} catch (ParseException e) {
				Log.e(ItineraryFragment.class.getSimpleName(), e.getMessage());
			}
			if (pos != null) {
				i.putExtra(LegMapActivity.ACTIVE_POS, pos);
			}
		}
		getActivity().startActivity(i);
	}
}
