package eu.trentorise.smartcampus.jp;

import java.util.ArrayList;
import java.util.Arrays;

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
import eu.trentorise.smartcampus.jp.custom.SmartCheckAdapter;

public class SmartCheckListFragment extends FeedbackFragment {

	private ListView optionsListView;

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
		optionsListView = (ListView) getSherlockActivity().findViewById(R.id.smart_check_list);
		ArrayList<String> stringOfOptions = new ArrayList<String>(Arrays.asList(getSherlockActivity().getResources()
				.getStringArray(R.array.smart_checks_list)));
		SmartCheckAdapter adapter = new SmartCheckAdapter(getSherlockActivity(), layout.smart_option_row, stringOfOptions);
		optionsListView.setAdapter(adapter);
		optionsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				RelativeLayout ll = (RelativeLayout) view;
				TextView option = (TextView) ll.getChildAt(0);
				String optionName = option.getText().toString();
				if (optionName.compareTo(getSherlockActivity().getResources().getStringArray(R.array.smart_checks_list)[0]) == 0) {
					// bus Time Table
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					Fragment fragment = new SmartCheckBusFragment();
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					fragmentTransaction.replace(Config.mainlayout, fragment);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
				} else if (optionName
						.compareTo(getSherlockActivity().getResources().getStringArray(R.array.smart_checks_list)[1]) == 0) {
					// extraurban bus Time Table
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					Fragment fragment = new SmartCheckSuburbanFragment();
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					fragmentTransaction.replace(Config.mainlayout, fragment);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
				} else if (optionName
						.compareTo(getSherlockActivity().getResources().getStringArray(R.array.smart_checks_list)[2]) == 0) {
					// train Time Table
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					Fragment fragment = new SmartCheckTrainFragment();
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					fragmentTransaction.replace(Config.mainlayout, fragment);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
				} else if (optionName
						.compareTo(getSherlockActivity().getResources().getStringArray(R.array.smart_checks_list)[3]) == 0) {
					// Parking availability
					FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
							.beginTransaction();
					Fragment fragment = new SmartCheckParkingsFragment();
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					fragmentTransaction.replace(Config.mainlayout, fragment);
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
				} else {
					// Toast available soon
					Toast.makeText(getSherlockActivity(), R.string.tmp, Toast.LENGTH_SHORT).show();
				}
			}

		});

	}

}
