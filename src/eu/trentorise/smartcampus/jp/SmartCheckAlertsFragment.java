package eu.trentorise.smartcampus.jp;

import java.util.List;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.custom.SmartCheckAlertsAdapter;
import eu.trentorise.smartcampus.jp.helper.AlertRoadsHelper;
import eu.trentorise.smartcampus.jp.helper.processor.SmartCheckAlertRoadsProcessor;
import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;

public class SmartCheckAlertsFragment extends SherlockListFragment {

	protected static final String PARAM_AID = "alertRoadsAgencyId";
	private String agencyId;
	private SmartCheckAlertsAdapter adapter;
	private SCAsyncTask<Void, Void, List<AlertRoadLoc>> loader;

	public SmartCheckAlertsFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_AID)) {
			this.agencyId = savedInstanceState.getString(PARAM_AID);
		} else if (getArguments() != null && getArguments().containsKey(PARAM_AID)) {
			this.agencyId = getArguments().getString(PARAM_AID);
		}

		setHasOptionsMenu(true);

		adapter = new SmartCheckAlertsAdapter(getSherlockActivity(), R.layout.smartcheck_alert_row);
		// adapter.setMyLocation(JPHelper.getLocationHelper().getLocation());
		adapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				if (getView() != null) {
					TextView smartcheckRoutesMsg = (TextView) getView().findViewById(R.id.smartcheck_none);
					smartcheckRoutesMsg.setText(getString(R.string.smart_check_alerts_empty));

					if (adapter.getCount() == 0) {
						smartcheckRoutesMsg.setVisibility(View.VISIBLE);
					} else {
						smartcheckRoutesMsg.setVisibility(View.GONE);
					}
					super.onChanged();
				}
			}
		});

		setListAdapter(adapter);

		// LOAD
		// getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
		loader = new SCAsyncTask<Void, Void, List<AlertRoadLoc>>(getSherlockActivity(), new SmartCheckAlertRoadsProcessor(
				getSherlockActivity(), adapter, agencyId));
		loader.execute();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		AlertRoadLoc alertRoad = adapter.getItem(position);

		FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
		Fragment fragment = new SmartCheckAlertDetailsFragment();
		Bundle args = new Bundle();
		args.putSerializable(SmartCheckAlertDetailsFragment.ARG_ALERT, alertRoad);
		fragment.setArguments(args);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.replace(Config.mainlayout, fragment, "lines");
		fragmentTransaction.addToBackStack(fragment.getTag());
		// fragmentTransaction.commitAllowingStateLoss();
		fragmentTransaction.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheck_list, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(), getView());
	}

	@Override
	public void onPause() {
		super.onPause();

		if (loader != null) {
			loader.cancel(true);
		}
		SherlockFragmentActivity sfa = getSherlockActivity();
		if (sfa != null)
			sfa.setSupportProgressBarIndeterminateVisibility(false);
	}

	// @Override
	// public void onPrepareOptionsMenu(Menu menu) {
	// menu.clear();
	// MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_map, 1,
	// R.string.menu_item_parking_map);
	// item.setIcon(R.drawable.map);
	// item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	// super.onPrepareOptionsMenu(menu);
	// }

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// if (item.getItemId() == R.id.menu_item_map) {
	// goToParkingsMap(null);
	// return true;
	// } else {
	// return super.onOptionsItemSelected(item);
	// }
	// }

}
