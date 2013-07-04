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
package eu.trentorise.smartcampus.jp.custom;

import it.sayservice.platform.smartplanner.data.message.TType;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.MonitorJourneyFragment;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.MyItinerariesListAdapter.MonitorMyItineraryProcessor;
import eu.trentorise.smartcampus.jp.custom.data.BasicItinerary;
import eu.trentorise.smartcampus.jp.custom.data.BasicRecurrentJourney;
import eu.trentorise.smartcampus.jp.custom.data.BasicRecurrentJourneyParameters;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.Utils;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class MyRecurItinerariesListAdapter extends ArrayAdapter<BasicRecurrentJourney> {

	Context context;
	int layoutResourceId;
	List<BasicRecurrentJourney> myItineraries;
    static Map<Integer,String> mp=new HashMap<Integer,String>();  


	public MyRecurItinerariesListAdapter(Context context, int layoutResourceId, List<BasicRecurrentJourney> myItineraries) {
		super(context, layoutResourceId, myItineraries);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.myItineraries = myItineraries;
		init();
	}

	private void init() {
        mp.put(1,"Su");  
        mp.put(2,"Mo");  
        mp.put(3,"Tu");  
        mp.put(4,"We");  
        mp.put(5,"Th");  
        mp.put(6,"Fr");  
        mp.put(7,"Sa");  		
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RowHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RowHolder();
			holder.name = (TextView) row.findViewById(R.id.it_name);
//			holder.timeFrom = (TextView) row.findViewById(R.id.it_time_from);
//			holder.timeTo = (TextView) row.findViewById(R.id.it_time_to);
			holder.from = (TextView) row.findViewById(R.id.itlocation_from);
			holder.to = (TextView) row.findViewById(R.id.itlocation_to);
			holder.recurrence = (TextView) row.findViewById(R.id.recurrence);
			holder.monitor = (ToggleButton) row.findViewById(R.id.its_monitor);

			//holder.transportTypes = (LinearLayout) row.findViewById(R.id.it_transporttypes);
			row.setTag(holder);
		} else {
			holder = (RowHolder) row.getTag();
		}

		BasicRecurrentJourney myItinerary = getItem(position);
		if (myItinerary.getName() != null && myItinerary.getName().length() > 0) {
			holder.name.setText(myItinerary.getName());
		} else {
			holder.name.setText(null);
		}

		try {
			Date time = Config.FORMAT_TIME_SMARTPLANNER.parse(myItinerary.getData().getParameters().getTime());
			// time from
//			holder.timeFrom.setText(Config.FORMAT_TIME_UI.format(time));
			// time to
			time.setTime(time.getTime()+myItinerary.getData().getParameters().getInterval());
//			holder.timeTo.setText(Config.FORMAT_TIME_UI.format(time));
		} catch (ParseException e) {
		}
		// position from
		holder.from.setText(Html.fromHtml("<i>"+context.getString(R.string.label_from)+" </i>"+myItinerary.getData().getParameters().getFrom().getName()));
		// position to
		holder.to.setText(Html.fromHtml("<i>"+context.getString(R.string.label_to)+" </i>"+myItinerary.getData().getParameters().getTo().getName()));
		
		// recurrence 
//		holder.recurrence.setText(PlanRecurJourneyFragment.getRecurrenceString(myItinerary.getData().getRecurrence()));
		
//		holder.recurrence.setText(Html.fromHtml("<i>"+context.getString(R.string.label_days)+" </i>"+"Mo Tu We Th Fr Sa Su"));
		holder.recurrence.setText(Html.fromHtml("<i>"+context.getString(R.string.label_days)+" </i>"+setRecurrenceByNumber(myItinerary.getData().getParameters().getRecurrence())));

//		// transport types
//		holder.transportTypes.removeAllViews();
//		for (TType t : myItinerary.getData().getTransportTypes()) {
//			ImageView imgv = Utils.getImageByTType(getContext(), t);
//			if (imgv.getDrawable() != null) {
//				holder.transportTypes.addView(imgv);
//			}
//		}
		/*Set monitor on or off and clicklistener*/

		holder.monitor.setOnCheckedChangeListener(null);
		holder.monitor.setChecked(myItinerary.isMonitor());
		if (myItinerary.isMonitor())
			holder.monitor.setBackgroundResource(R.drawable.ic_monitor_on); 
		else holder.monitor.setBackgroundResource(R.drawable.ic_monitor_off);
		 holder.monitor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				if (buttonView.getId() == R.id.its_monitor) {
					SCAsyncTask<Object, Void, Boolean> task = new SCAsyncTask<Object,Void , Boolean>((SherlockFragmentActivity) context,
						new MonitorMyRecItineraryProcessor((SherlockFragmentActivity) context));
					task.execute(Boolean.toString(isChecked), myItineraries.get(position).getClientId(),position);
				} else {
					return;
				}
			}
		});
		return row;
	}

	private String setRecurrenceByNumber(List<Integer> list) {
		String daysOfWeek = new String();
		for (Integer day : list) {
			daysOfWeek=daysOfWeek+" "+checkDay(day);
		}
		return daysOfWeek;
	}
	public static String checkDay(Integer day)  
    {  
     
        return mp.get(day);  
    }  
	

	static class RowHolder {
		TextView name;
		TextView from;
		TextView to;
		TextView recurrence;
		ToggleButton monitor;

	}
	

	public class MonitorMyRecItineraryProcessor extends AbstractAsyncTaskProcessor<Object, Boolean> {

		Integer position;
		//List<BasicRecurrentJourneyParameters> myItineraries;
		String id;
		
		public MonitorMyRecItineraryProcessor(SherlockFragmentActivity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(Object... params) throws SecurityException, Exception {
			// 0: monitor
			// 1: id
			boolean monitor = Boolean.parseBoolean((String) params[0]);
			 position = (Integer) params[2];
			// myItineraries=(List<BasicRecurrentJourneyParameters>) params[1];
			 id =  (String) params[1];
			return JPHelper.monitorMyRecItinerary(monitor, id);
		}

		@Override
		public void handleResult(Boolean result) {
			//cambia background in funzione a quello che ho
			myItineraries.get(position).setMonitor(result);
			notifyDataSetChanged();

		}
	}
}
