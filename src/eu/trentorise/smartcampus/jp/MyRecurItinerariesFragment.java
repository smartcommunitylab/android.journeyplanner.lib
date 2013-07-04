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

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.custom.MyRecurItinerariesListAdapter;
import eu.trentorise.smartcampus.jp.custom.data.BasicRecurrentJourney;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class MyRecurItinerariesFragment extends FeedbackFragment {

	private List<BasicRecurrentJourney> myItineraries = new ArrayList<BasicRecurrentJourney>();
	private MyRecurItinerariesListAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getSherlockActivity().getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
			getSherlockActivity().getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_TABS);
		}

		return inflater.inflate(R.layout.myitineraries, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		ListView myJourneysList = (ListView) getView().findViewById(
				R.id.myitineraries_list);
		 myItineraries = new ArrayList<BasicRecurrentJourney>();
		adapter = new MyRecurItinerariesListAdapter(getSherlockActivity(),
				R.layout.recur_itinerarychoices_row, myItineraries);
		myJourneysList.setAdapter(adapter);

		SCAsyncTask<Void, Void, List<BasicRecurrentJourney>> task = new SCAsyncTask<Void, Void, List<BasicRecurrentJourney>>(
				getSherlockActivity(), new GetMyRecurItinerariesProcessor(
						getSherlockActivity(), adapter));
		task.execute();

		myJourneysList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						FragmentTransaction fragmentTransaction = getSherlockActivity()
								.getSupportFragmentManager().beginTransaction();
						Fragment fragment = new MyRecurItineraryFragment();
						Bundle b = new Bundle();
						b.putSerializable(MyRecurItineraryFragment.PARAMS,
								adapter.getItem(position));
						b.putBoolean(MyRecurItineraryFragment.PARAM_EDITING, false);

						fragment.setArguments(b);
						fragmentTransaction
								.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
						fragmentTransaction
								.replace(Config.mainlayout, fragment,Config.MY_RECUR_JOURNEYS_FRAGMENT_TAG);
						fragmentTransaction.addToBackStack(fragment.getTag());
						fragmentTransaction.commit();
					}
				});
	}

	public class GetMyRecurItinerariesProcessor extends
			AbstractAsyncTaskProcessor<Void, List<BasicRecurrentJourney>> {

		private MyRecurItinerariesListAdapter adapter;
		private TextView noitems;

		public GetMyRecurItinerariesProcessor(
				SherlockFragmentActivity activity,
				MyRecurItinerariesListAdapter adapter) {
			super(activity);
			this.adapter = adapter;
			noitems = (TextView) activity
					.findViewById(R.id.myitinearies_noitems_label);

		}

		@Override
		public List<BasicRecurrentJourney> performAction(Void... params)
				throws SecurityException, Exception {
			return JPHelper.getMyRecurItineraries();
		}

		@Override
		public void handleResult(List<BasicRecurrentJourney> result) {
			if (!result.isEmpty()) {
				adapter.clear();
				for (BasicRecurrentJourney myt : result) {
					adapter.add(myt);
				}
				adapter.notifyDataSetChanged();

			}
			if ((result == null) || (result.size() == 0)) {
				// put "empty string" with no result
				noitems.setVisibility(View.VISIBLE);
			} else {
				noitems.setVisibility(View.GONE);
			}
		}
	}

}
