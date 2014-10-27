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
import it.sayservice.platform.smartplanner.data.message.journey.SingleJourney;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.jp.custom.ItinerariesListAdapter;
import eu.trentorise.smartcampus.jp.helper.Utils;
import eu.trentorise.smartcampus.jp.helper.processor.PlanNewJourneyProcessor;

public class ItineraryChoicesFragment extends SherlockFragment {

	private static final String ITINERARIES = "itineraries";
	private static final String JOURNEY = "journey";
	private static final String LOADED = "loaded";
	private SingleJourney singleJourney;
	private List<Itinerary> itineraries = new ArrayList<Itinerary>();
	private ItinerariesListAdapter itinerariesAdapter;
	private ItinerariesListAdapter itinerariesPromotedAdapter;
	private LinearLayout noItemsView;
	private boolean mLoaded = false;

	public static ItineraryChoicesFragment newInstance(SingleJourney singleJourney) {
		ItineraryChoicesFragment f = new ItineraryChoicesFragment();
		f.singleJourney = singleJourney;
		return f;
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		if (singleJourney != null) {
			arg0.putSerializable(JOURNEY, singleJourney);
		}

		if (itineraries != null) {
			arg0.putSerializable(ITINERARIES, new ArrayList<Itinerary>(itineraries));
		}

		arg0.putBoolean(LOADED, mLoaded);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(JOURNEY)) {
				singleJourney = (SingleJourney) savedInstanceState.get(JOURNEY);
			}
			if (savedInstanceState.containsKey(ITINERARIES)) {
				itineraries = (List<Itinerary>) savedInstanceState.get(ITINERARIES);
			}
			if (savedInstanceState.containsKey(LOADED)) {
				mLoaded = savedInstanceState.getBoolean(LOADED);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.itinerarychoices, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		TextView dateTextView = (TextView) getView().findViewById(R.id.choices_date);
		// Date dateDate = Utils.sjDateString2date(singleJourney.getDate());
		// dateTextView.setText(new SimpleDateFormat(Config.FORMAT_DATE_UI,
		// Locale.US).format(dateDate));
		dateTextView.setText(Utils.serverDate2UIDate(singleJourney.getDate()));

		TextView timeTextView = (TextView) getView().findViewById(R.id.choices_time);
		// Date timeDate =
		// Utils.sjTimeString2date(singleJourney.getDepartureTime());
		// timeTextView.setText(new SimpleDateFormat(Config.FORMAT_TIME_UI,
		// Locale.ITALY).format(timeDate));
		timeTextView.setText(Utils.serverTime2UITime(singleJourney.getDepartureTime()));

		TextView fromTextView = (TextView) getView().findViewById(R.id.choices_from);
		fromTextView.setText(singleJourney.getFrom().getName());

		TextView toTextView = (TextView) getView().findViewById(R.id.choices_to);
		toTextView.setText(singleJourney.getTo().getName());

		View itinerariesPromotedView = getView().findViewById(R.id.choices_promoted_layout);
		ListView itinerariesPromotedList = (ListView) getView().findViewById(R.id.choices_promoted_listView);
		ListView itinerariesList = (ListView) getView().findViewById(R.id.choices_listView);
		noItemsView = (LinearLayout) getView().findViewById(R.id.no_items_label);
		noItemsView.setVisibility(View.GONE);

		if (itinerariesPromotedAdapter == null) {
			itinerariesPromotedAdapter = new ItinerariesListAdapter(getSherlockActivity(), R.layout.itinerarychoices_row);
		}
		itinerariesPromotedList.setAdapter(itinerariesPromotedAdapter);

		if (itinerariesAdapter == null) {
			itinerariesAdapter = new ItinerariesListAdapter(getSherlockActivity(), R.layout.itinerarychoices_row);
		}
		itinerariesList.setAdapter(itinerariesAdapter);

		if (!mLoaded) {
			SCAsyncTask<SingleJourney, Void, List<Itinerary>> task = new SCAsyncTask<SingleJourney, Void, List<Itinerary>>(
					getSherlockActivity(), new PlanNewJourneyProcessor(getSherlockActivity(), itinerariesList,
							itinerariesAdapter, itinerariesPromotedView, itinerariesPromotedAdapter, noItemsView));
			task.execute(singleJourney);
			mLoaded = true;
		}

		AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ArrayAdapter<Itinerary> adapter = null;
				if (parent.getId() == R.id.choices_promoted_listView) {
					adapter = itinerariesPromotedAdapter;
				} else if ((parent.getId() == R.id.choices_listView)) {
					adapter = itinerariesAdapter;
				}

				if (adapter != null) {
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					Fragment fragment = ItineraryFragment.newInstance(singleJourney, adapter.getItem(position));
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					fragmentTransaction.replace(Config.mainlayout, fragment, ItineraryChoicesFragment.this.getTag());
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();
				}
			}
		};

		itinerariesPromotedList.setOnItemClickListener(onClickListener);
		itinerariesList.setOnItemClickListener(onClickListener);

		itinerariesPromotedAdapter.notifyDataSetChanged();
		itinerariesAdapter.notifyDataSetChanged();

		if (itinerariesPromotedAdapter.isEmpty()) {
			itinerariesPromotedView.setVisibility(View.GONE);
		} else {
			itinerariesPromotedView.setVisibility(View.VISIBLE);
		}

		if (itinerariesAdapter.isEmpty()) {
			itinerariesList.setVisibility(View.GONE);
		} else {
			itinerariesList.setVisibility(View.VISIBLE);
		}
	}

	public SingleJourney getSingleJourney() {
		return singleJourney;
	}

	public List<Itinerary> getItineraries() {
		return itineraries;
	}
}
