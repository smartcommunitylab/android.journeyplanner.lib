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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.R.layout;
import eu.trentorise.smartcampus.jp.custom.SmartCheckDirectionAdapter;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;

public class SmartCheckBusDirectionFragment extends FeedbackFragment {
	protected static final String PARAM_LINE = "line";
	protected static final String PARAM_AGENCY = "agency";
	private SmartLine smartLine = null;
	private String agencyId = null;
	private ListView busDirectionsView;
	private TextView busLine;
	private LinearLayout busLineLayout;
	ArrayList<SmartLine> busLines;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_LINE)) {
			this.smartLine = (SmartLine) savedInstanceState.getParcelable(PARAM_LINE);
		} else if (getArguments() != null && getArguments().containsKey(PARAM_LINE)) {
			this.smartLine = (SmartLine) getArguments().getParcelable(PARAM_LINE);
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_AGENCY)) {
			this.agencyId = savedInstanceState.getString(PARAM_AGENCY);
		} else if (getArguments() != null && getArguments().containsKey(PARAM_AGENCY)) {
			this.agencyId = getArguments().getString(PARAM_AGENCY);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheckdirection, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		busDirectionsView = (ListView) getSherlockActivity().findViewById(R.id.bus_directions_list);
		busLineLayout = (LinearLayout) getSherlockActivity().findViewById(R.id.line_layout);
		busLineLayout.setBackgroundColor(smartLine.getColor());
		busLine = (TextView) getSherlockActivity().findViewById(R.id.line_label);

		String lineName = smartLine.getRoutesShorts().get(0);
		for (int i = 1; i < smartLine.getRoutesShorts().size(); i++) {
			String routeShort = smartLine.getRoutesShorts().get(i);
			if (!routeShort.equals(lineName)) {
				lineName = null;
				break;
			}
		}

		if (lineName != null) {
			busLine.setText(lineName);
		} else {
			busLine.setText(smartLine.getLine());
		}

		if (agencyId != null && (RoutesHelper.AGENCYID_BUS_SUBURBAN.equals(agencyId))) {
			busLine.setTextAppearance(getSherlockActivity(), android.R.style.TextAppearance_Large);
		}

		busLine.setTextColor(getSherlockActivity().getResources().getColor(R.color.transparent_white));

		SmartCheckDirectionAdapter adapter = new SmartCheckDirectionAdapter(getSherlockActivity(), layout.smart_direction_row);
		for (String routeLong : smartLine.getRoutesLong()) {
			adapter.add(routeLong);
		}
		busDirectionsView.setAdapter(adapter);

		busDirectionsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// RelativeLayout ll = (RelativeLayout) view;
				// TextView option = (TextView) ll.getChildAt(0);

				// fragment with the bus timetable
				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
				Fragment fragment = new SmartCheckTTFragment();
				Bundle b = new Bundle();
				SmartLine smartLineParam = new SmartLine(null, smartLine.getRoutesShorts().get(position), smartLine.getColor(),
						new ArrayList<String>(Arrays.asList(smartLine.getRoutesShorts().get(position))), new ArrayList<String>(
								Arrays.asList(smartLine.getRoutesLong().get(position))), new ArrayList<String>(Arrays
								.asList(smartLine.getRouteID().get(position))));
				b.putParcelable(SmartCheckTTFragment.PARAM_SMARTLINE, smartLineParam);
				fragment.setArguments(b);
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				fragmentTransaction.replace(Config.mainlayout, fragment, "lines");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		});
	}
}
