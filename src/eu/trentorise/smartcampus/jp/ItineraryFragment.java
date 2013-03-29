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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.custom.DialogHandler;
import eu.trentorise.smartcampus.jp.custom.LegsListAdapter;
import eu.trentorise.smartcampus.jp.custom.data.BasicItinerary;
import eu.trentorise.smartcampus.jp.helper.processor.SaveItineraryProcessor;

public class ItineraryFragment extends FeedbackFragment {

	private static final String ITINERARY = "itineraries";
	private static final String JOURNEY = "journey";
	private static final String LEGS = "legs";

	private SingleJourney singleJourney;
	private Itinerary itinerary;
	private List<Leg> legs;

	public static ItineraryFragment newInstance(SingleJourney singleJourney, Itinerary itinerary) {
		ItineraryFragment f = new ItineraryFragment();
		f.singleJourney = singleJourney;
		f.itinerary = itinerary;
		return f;
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		if (singleJourney != null) arg0.putSerializable(JOURNEY, singleJourney);
		if (itinerary != null) {
			arg0.putSerializable(ITINERARY, itinerary);
		}
		if (legs != null) {
			arg0.putSerializable(LEGS, new ArrayList<Leg>(legs));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(JOURNEY)) singleJourney = (SingleJourney) savedInstanceState.get(JOURNEY);
			if (savedInstanceState.containsKey(ITINERARY)) itinerary = (Itinerary) savedInstanceState.get(ITINERARY);
			if (savedInstanceState.containsKey(LEGS)) legs = (List<Leg>) savedInstanceState.get(LEGS);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.itinerary, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		legs = itinerary.getLeg();

		TextView dateTextView = (TextView) getView().findViewById(R.id.itinerary_date);
		dateTextView.setText(Config.FORMAT_DATE_UI.format(new Date(itinerary.getStartime())));

		TextView timeTextView = (TextView) getView().findViewById(R.id.itinerary_time);
		timeTextView.setText(Config.FORMAT_TIME_UI.format(new Date(itinerary.getStartime())));

		ListView legsListView = (ListView) getView().findViewById(R.id.itinerary_legs);

		if (legsListView.getHeaderViewsCount() == 0) {
			// HEADER (before setAdapter or it won't work!)
			ViewGroup startLayout = (ViewGroup) getSherlockActivity().getLayoutInflater().inflate(R.layout.itinerary_leg,
					null);
			TextView startLegTimeTextView = (TextView) startLayout.findViewById(R.id.leg_time);
			startLegTimeTextView.setText(Config.FORMAT_TIME_UI.format(new Date(itinerary.getStartime())));
			TextView startLegDescTextView = (TextView) startLayout.findViewById(R.id.leg_description);
			startLegDescTextView.setText(singleJourney.getFrom().getName());
			legsListView.addHeaderView(startLayout);

			// FOOTER (before setAdapter or it won't work!)
			ViewGroup endLayout = (ViewGroup) getSherlockActivity().getLayoutInflater().inflate(R.layout.itinerary_leg,
					null);
			TextView endLegTimeTextView = (TextView) endLayout.findViewById(R.id.leg_time);
			endLegTimeTextView.setText(Config.FORMAT_TIME_UI.format(new Date(itinerary
					.getEndtime())));
			TextView endLegDescTextView = (TextView) endLayout.findViewById(R.id.leg_description);
			endLegDescTextView.setText(singleJourney.getTo().getName());
			legsListView.addFooterView(endLayout);
		}

		legsListView.setAdapter(new LegsListAdapter(getSherlockActivity(), R.layout.itinerary_leg, singleJourney
				.getFrom(), singleJourney.getTo(), legs));

		legsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent i = new Intent(getActivity(), LegMapActivity.class);
				if (legs != null) {
					i.putExtra(LegMapActivity.LEGS, new ArrayList<Leg>(legs));
				}
				i.putExtra(LegMapActivity.ACTIVE_POS, position - 1);
				getActivity().startActivity(i);
			}
		});

		Button saveItineraryBtn = (Button) getView().findViewById(R.id.itinerary_save);
		saveItineraryBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String suggestedName = "";

				Dialog dialog = new ItineraryNameDialog(getActivity(), new DialogHandler<String>() {
					@Override
					public void handleSuccess(String name) {
						BasicItinerary basicItinerary = new BasicItinerary();
						basicItinerary.setData(itinerary);
						basicItinerary.setOriginalFrom(singleJourney.getFrom());
						basicItinerary.setOriginalTo(singleJourney.getTo());
						basicItinerary.setName(name);
						SCAsyncTask<BasicItinerary, Void, Void> task = new SCAsyncTask<BasicItinerary, Void, Void>(
								getSherlockActivity(), new SaveItineraryProcessor(getSherlockActivity()));
						task.execute(basicItinerary);
					}
				}, suggestedName);
				dialog.show();
			}
		});
	}

}
