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

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import eu.trentorise.smartcampus.ac.UserRegistration;
import eu.trentorise.smartcampus.ac.authenticator.AMSCAccessProvider;
import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;

public class BroadcastNotificationsFragment extends FeedbackFragment {

	// private BroadcastNotificationsFragment self;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// self = this;
		return inflater.inflate(R.layout.bn_home, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		ListView list = (ListView) getSherlockActivity().findViewById(R.id.bn_options_list);

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String itemtext = ((TextView) view).getText().toString();
				Bundle bundle = new Bundle();
				bundle.putString("itemtext", itemtext);

				SherlockFragment fragment = null;
				if (itemtext.contentEquals(getString(R.string.broadcast_notifications_bus_trento_delay))) {
					bundle.putStringArray(BT_DelayFormFragment.ARG_AGENCYID, new String[] { RoutesHelper.AGENCYID_BUS_TRENTO });
					fragment = new BT_DelayFormFragment();
				} else if (itemtext.contentEquals(getString(R.string.broadcast_notifications_bus_rovereto_delay))) {
					bundle.putStringArray(BT_DelayFormFragment.ARG_AGENCYID,
							new String[] { RoutesHelper.AGENCYID_BUS_ROVERETO });
					fragment = new BT_DelayFormFragment();
				} else if (itemtext.contentEquals(getString(R.string.broadcast_notifications_bus_suburban_delay))) {
					bundle.putStringArray(BT_DelayFormFragment.ARG_AGENCYID,
							new String[] { RoutesHelper.AGENCYID_BUS_SUBURBAN });
					fragment = new BT_DelayFormFragment();
				} else if (itemtext.contentEquals(getString(R.string.broadcast_notifications_train_delay))) {
					bundle.putStringArray(BT_DelayFormFragment.ARG_AGENCYID, new String[] { RoutesHelper.AGENCYID_TRAIN_BZVR,
							RoutesHelper.AGENCYID_TRAIN_TM, RoutesHelper.AGENCYID_TRAIN_TNBDG });
					fragment = new BT_DelayFormFragment();
				} else {
					// fragment = new FormFragment();
					Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.tmp, Toast.LENGTH_SHORT).show();
				}

				if (new AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
					// show dialog box
					UserRegistration.upgradeuser(getSherlockActivity());

				} else if (fragment != null) {

					fragment.setArguments(bundle);
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					// fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					// fragmentTransaction.detach(self);
					fragmentTransaction.replace(Config.mainlayout, fragment, Config.NOTIFICATIONS);
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();
				}
			}
		});

	}

}
