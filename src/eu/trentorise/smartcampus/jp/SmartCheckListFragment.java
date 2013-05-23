package eu.trentorise.smartcampus.jp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.R.layout;
import eu.trentorise.smartcampus.jp.custom.SmartCheckListAdapter;
import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;

public class SmartCheckListFragment extends FeedbackFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheck, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		ListView optionsListView = (ListView) getSherlockActivity().findViewById(R.id.smart_check_list);
		SmartCheckListAdapter adapter = new SmartCheckListAdapter(getSherlockActivity(), layout.smart_option_row);

		String[] optionsList = getSherlockActivity().getResources().getStringArray(R.array.smart_checks_list);
		for (int opt = 0; opt < optionsList.length; opt++) {
			adapter.add(optionsList[opt]);
		}

		optionsListView.setAdapter(adapter);

		optionsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				RelativeLayout ll = (RelativeLayout) view;
				TextView option = (TextView) ll.findViewById(R.id.smart_option);
				String optionName = option.getText().toString();

				Fragment fragment = null;

				if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_bus_trento_timetable))) {
					// Trento bus timetable
					fragment = new SmartCheckBusFragment();
					Bundle bundle = new Bundle();
					bundle.putString(SmartCheckBusFragment.PARAM_AID, RoutesHelper.AGENCYID_BUS_TRENTO);
					fragment.setArguments(bundle);
				} else if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_bus_rovereto_timetable))) {
					// Rovereto bus timetable
					fragment = new SmartCheckBusFragment();
					Bundle bundle = new Bundle();
					bundle.putString(SmartCheckBusFragment.PARAM_AID, RoutesHelper.AGENCYID_BUS_ROVERETO);
					fragment.setArguments(bundle);
				} else if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_suburban_timetable))) {
					// Suburban bus timetable
					fragment = new SmartCheckSuburbanFragment();
				} else if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_train_timetable))) {
					// Trains timetable
					fragment = new SmartCheckTrainFragment();
				} else if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_parking_trento))) {
					// Trento parking availability
					fragment = new SmartCheckParkingsFragment();
					Bundle bundle = new Bundle();
					bundle.putString(SmartCheckParkingsFragment.PARAM_AID, ParkingsHelper.PARKING_AID_TRENTO);
					fragment.setArguments(bundle);
				} else if (optionName.contentEquals(getResources().getString(R.string.smart_check_list_parking_rovereto))) {
					// Rovereto parking availability
					fragment = new SmartCheckParkingsFragment();
					Bundle bundle = new Bundle();
					bundle.putString(SmartCheckParkingsFragment.PARAM_AID, ParkingsHelper.PARKING_AID_ROVERETO);
					fragment.setArguments(bundle);
				} else {
					// Toast available soon
					Toast.makeText(getSherlockActivity(), R.string.tmp, Toast.LENGTH_SHORT).show();
				}

				if (fragment != null) {
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					fragmentTransaction.replace(Config.mainlayout, fragment);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
				}
			}
		});
	}
}
