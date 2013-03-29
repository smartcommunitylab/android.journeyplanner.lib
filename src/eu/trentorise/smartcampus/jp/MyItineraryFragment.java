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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.custom.LegsListAdapter;
import eu.trentorise.smartcampus.jp.custom.data.BasicItinerary;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.processor.DeleteMyItineraryProcessor;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class MyItineraryFragment extends FeedbackFragment {

	private BasicItinerary myItinerary;
	private Itinerary itinerary;
	private List<Leg> legs;

	public static MyItineraryFragment newInstance(BasicItinerary myItinerary) {
		MyItineraryFragment f = new MyItineraryFragment();
		f.myItinerary = myItinerary;
		f.itinerary = myItinerary.getData();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.myitinerary, container, false);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);
		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();

		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_delete, Menu.NONE, R.string.menu_item_delete);
		if (myItinerary != null) {
			if (myItinerary.isMonitor())
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_monitor, Menu.NONE, R.string.menu_item_monitor_off);
			else
				submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_monitor, Menu.NONE, R.string.menu_item_monitor_on);
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_monitor) {
			//toggle the monitor
			SCAsyncTask<String, Void, Boolean> task = new SCAsyncTask<String, Void, Boolean>(getSherlockActivity(),
					new MonitorMyItineraryProcessor(getSherlockActivity()));
			task.execute(Boolean.toString(!myItinerary.isMonitor()), myItinerary.getClientId());
			return true;
		} else if (item.getItemId() == R.id.menu_item_delete) {
			//delete monitor
			AlertDialog.Builder deleteAlertDialog = new AlertDialog.Builder(getSherlockActivity());
			deleteAlertDialog.setTitle(getString(R.string.dialog_delete_itinerary, myItinerary.getName()));
			deleteAlertDialog.setMessage(getString(R.string.dialog_are_you_sure));
			deleteAlertDialog.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SCAsyncTask<String, Void, Void> task = new SCAsyncTask<String, Void, Void>(getSherlockActivity(),
							new DeleteMyItineraryProcessor(getSherlockActivity(),MyItineraryFragment.this.getTag()));
					task.execute(myItinerary.getName(), myItinerary.getClientId());
					dialog.dismiss();
					getSherlockActivity().getSupportFragmentManager().popBackStackImmediate();
				}
			});
			deleteAlertDialog.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			deleteAlertDialog.show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
			}

	@Override
	public void onStart() {
		super.onStart();

		legs = itinerary.getLeg();

		TextView nameTextView = (TextView) getView().findViewById(R.id.myitinerary_name);
		nameTextView.setText(myItinerary.getName());

		TextView dateTextView = (TextView) getView().findViewById(R.id.myitinerary_date);
		dateTextView.setText(Config.FORMAT_DATE_UI.format(new Date(itinerary.getStartime())));

		TextView timeTextView = (TextView) getView().findViewById(R.id.myitinerary_time);
		timeTextView.setText(Config.FORMAT_TIME_UI.format(new Date(itinerary.getStartime())));

		ListView legsListView = (ListView) getView().findViewById(R.id.myitinerary_legs);

		// HEADER (before setAdapter or it won't work!)
		if (legsListView.getHeaderViewsCount() == 0) {
			ViewGroup startLayout = (ViewGroup) getSherlockActivity().getLayoutInflater().inflate(R.layout.itinerary_leg, null);
			TextView startLegTimeTextView = (TextView) startLayout.findViewById(R.id.leg_time);
			startLegTimeTextView.setText(Config.FORMAT_TIME_UI.format(new Date(itinerary.getStartime())));
			TextView startLegDescTextView = (TextView) startLayout.findViewById(R.id.leg_description);
			startLegDescTextView.setText(myItinerary.getOriginalFrom().getName());
			legsListView.addHeaderView(startLayout);
		}

		// FOOTER (before setAdapter or it won't work!)
		if (legsListView.getFooterViewsCount() == 0) {
			ViewGroup endLayout = (ViewGroup) getSherlockActivity().getLayoutInflater().inflate(R.layout.itinerary_leg, null);
			TextView endLegTimeTextView = (TextView) endLayout.findViewById(R.id.leg_time);
			endLegTimeTextView.setText(Config.FORMAT_TIME_UI.format(new Date(itinerary.getEndtime())));
			TextView endLegDescTextView = (TextView) endLayout.findViewById(R.id.leg_description);
			endLegDescTextView.setText(myItinerary.getOriginalTo().getName());
			legsListView.addFooterView(endLayout);
		}

		legsListView.setAdapter(new LegsListAdapter(getSherlockActivity(), R.layout.itinerary_leg, myItinerary
				.getOriginalFrom(), myItinerary.getOriginalTo(), legs));

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

		// Button deleteMyItineraryBtn = (Button)
		// getView().findViewById(R.id.myitinerary_delete);
		// deleteMyItineraryBtn.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// AlertDialog.Builder deleteAlertDialog = new
		// AlertDialog.Builder(getSherlockActivity());
		// deleteAlertDialog.setTitle("Delete " + myItinerary.getName());
		// deleteAlertDialog.setMessage("Are you sure?");
		// deleteAlertDialog.setPositiveButton("OK", new
		// DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// SCAsyncTask<String, Void, Void> task = new SCAsyncTask<String, Void,
		// Void>(getSherlockActivity(),
		// new DeleteMyItineraryProcessor(getSherlockActivity()));
		// task.execute(myItinerary.getName(), myItinerary.getClientId());
		// dialog.dismiss();
		// getSherlockActivity().getSupportFragmentManager().popBackStackImmediate();
		// }
		// });
		// deleteAlertDialog.setNegativeButton("Cancel", new
		// DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// dialog.dismiss();
		// }
		// });
		// deleteAlertDialog.show();
		// }
		// });

		ToggleButton monitorToggleBtn = (ToggleButton) getView().findViewById(R.id.myitinerary_toggle);
		TextView monitorLabel = (TextView) getView().findViewById(R.id.myitinerary_monitor_label);

		// monitorToggleBtn.setChecked(myItinerary.isMonitor());

		monitorToggleBtn.setOnCheckedChangeListener(null);
		monitorToggleBtn.setChecked(myItinerary.isMonitor());

		if (myItinerary.isMonitor()) {
			monitorToggleBtn.setBackgroundResource(R.drawable.ic_monitor_on);
			monitorLabel.setText(getString(R.string.monitor_on));
			monitorLabel.setTextAppearance(getSherlockActivity(), R.style.label_jp);

		} else {
			monitorToggleBtn.setBackgroundResource(R.drawable.ic_monitor_off);
			monitorLabel.setText(getString(R.string.monitor_off));
			monitorLabel.setTextAppearance(getSherlockActivity(), R.style.label_black_jp);

		}

		monitorToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SCAsyncTask<String, Void, Boolean> task = new SCAsyncTask<String, Void, Boolean>(getSherlockActivity(),
						new MonitorMyItineraryProcessor(getSherlockActivity()));
				task.execute(Boolean.toString(isChecked), myItinerary.getClientId());
			}
		});
	}

	public class MonitorMyItineraryProcessor extends AbstractAsyncTaskProcessor<String, Boolean> {
		ToggleButton monitorToggleBtn;
		TextView monitorLabel;

		public MonitorMyItineraryProcessor(SherlockFragmentActivity activity) {
			super(activity);
			monitorToggleBtn = (ToggleButton) activity.findViewById(R.id.myitinerary_toggle);
			monitorLabel = (TextView) activity.findViewById(R.id.myitinerary_monitor_label);
		}

		@Override
		public Boolean performAction(String... strings) throws SecurityException, Exception {
			// 0: monitor
			// 1: id
			boolean monitor = Boolean.parseBoolean(strings[0]);
			String id = strings[1];
			return JPHelper.monitorMyItinerary(monitor, id);
		}

		@Override
		public void handleResult(Boolean result) {
			myItinerary.setMonitor(result);
			if (result) {
				monitorToggleBtn.setBackgroundResource(R.drawable.ic_monitor_on);
				monitorLabel.setText(getString(R.string.monitor_on));
				monitorLabel.setTextAppearance(getSherlockActivity(), R.style.label_jp);
			} else {
				monitorToggleBtn.setBackgroundResource(R.drawable.ic_monitor_off);
				monitorLabel.setText(getString(R.string.monitor_off));
				monitorLabel.setTextAppearance(getSherlockActivity(), R.style.label_black_jp);

			}
			getSherlockActivity().invalidateOptionsMenu();

		}

	}

}
