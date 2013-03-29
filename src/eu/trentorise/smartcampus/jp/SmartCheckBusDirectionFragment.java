package eu.trentorise.smartcampus.jp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.R.layout;
import eu.trentorise.smartcampus.jp.custom.SmartCheckDirectionAdapter;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;

public class SmartCheckBusDirectionFragment extends FeedbackFragment {
	protected static final String PARAMS = "line";
	private SmartLine params = null;
	private ListView busDirectionsView;
	private TextView busLine;
	private LinearLayout busLineLayout;
	ArrayList<SmartLine> busLines;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(PARAMS)) {
			this.params = (SmartLine) savedInstanceState.getParcelable(PARAMS);
		} else if (getArguments() != null && getArguments().containsKey(PARAMS)) {
			this.params = (SmartLine) getArguments().getParcelable(PARAMS);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return  inflater.inflate(R.layout.smartcheckdirection, container, false);	
	}

	@Override
	public void onStart() {
		super.onStart();
		
		 busDirectionsView = (ListView) getSherlockActivity().findViewById(R.id.bus_directions_list);
		 busLineLayout = (LinearLayout) getSherlockActivity().findViewById(R.id.line_layout);
		 busLineLayout.setBackgroundColor(params.getColor());
		 busLine = (TextView) getSherlockActivity().findViewById(R.id.line_label);
		 busLine.setText(params.getRoutesShorts().get(0));
		 busLine.setTextColor(getSherlockActivity().getResources().getColor(R.color.transparent_white));

		 SmartCheckDirectionAdapter adapter = new SmartCheckDirectionAdapter(getSherlockActivity(),layout.smart_direction_row,new ArrayList<String>(params.getRoutesLong()));
		 busDirectionsView.setAdapter(adapter);
		 busDirectionsView.setOnItemClickListener(new OnItemClickListener() {


				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					RelativeLayout ll = (RelativeLayout)arg1;
					 TextView option = (TextView) ll.getChildAt(0);
		        		//fragment with the bus timetable
		        		FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
						Fragment fragment = new SmartCheckTTFragment();
						Bundle b = new Bundle();
						SmartLine param = new SmartLine(null, params.getRoutesShorts().get(0), params.getColor(), new ArrayList<String>(Arrays.asList(params.getRoutesShorts().get(0))), new ArrayList<String>(Arrays.asList(params.getRoutesLong().get(arg2))), new ArrayList<String>(Arrays.asList(params.getRouteID().get(arg2))));
						b.putParcelable(SmartCheckTTFragment.PARAM_SMARTLINE, param);
						fragment.setArguments(b);
						fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
						fragmentTransaction.replace(Config.mainlayout, fragment);
						fragmentTransaction.addToBackStack(null);
						fragmentTransaction.commit();
		        	
					}	
		 });
	}
	

}
