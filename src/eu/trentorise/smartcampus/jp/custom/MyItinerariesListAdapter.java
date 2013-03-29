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

import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.TType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.ItinerariesListAdapter.RowHolder;
import eu.trentorise.smartcampus.jp.custom.data.BasicItinerary;
import eu.trentorise.smartcampus.jp.custom.draw.LineDrawView;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.Utils;
import eu.trentorise.smartcampus.jp.helper.processor.MonitorMyItineraryProcessor;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class MyItinerariesListAdapter extends ArrayAdapter<BasicItinerary> {

	Context context;
	int layoutResourceId;
	List<BasicItinerary> myItineraries;

	public MyItinerariesListAdapter(Context context, int layoutResourceId, List<BasicItinerary> myItineraries) {
		super(context, layoutResourceId, myItineraries);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.myItineraries = myItineraries;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RowHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RowHolder();
			holder.name = (TextView) row.findViewById(R.id.its_name);
			holder.timeFrom = (TextView) row.findViewById(R.id.its_time_from);
			holder.locationFrom = (TextView) row.findViewById(R.id.its_location_from);

//			holder.line = (FrameLayout) row.findViewById(R.id.its_line);
			holder.timeTo = (TextView) row.findViewById(R.id.its_time_to);
			holder.locationTo = (TextView) row.findViewById(R.id.its_location_to);
			holder.transportTypes = (LinearLayout) row.findViewById(R.id.its_transporttypes);
			holder.monitor = (ToggleButton) row.findViewById(R.id.its_monitor);
			holder.day= (TextView) row.findViewById(R.id.its_day);


			row.setTag(holder);
		} else {
			holder = (RowHolder) row.getTag();
		}

		BasicItinerary myItinerary = myItineraries.get(position);
		if (myItinerary.getName().length() > 0) {
			holder.name.setText(myItinerary.getName());
			holder.name.setVisibility(View.VISIBLE);
		} else {
			holder.name.setVisibility(View.GONE);
		}

		Itinerary itinerary = myItinerary.getData();

		
		// day of the week
		Date timeFrom = new Date(itinerary.getStartime());
		String dateFromString = Config.FORMAT_DATE_UI.format(timeFrom);
		holder.day.setText(dateFromString);

		// time from
		String timeFromString = Config.FORMAT_TIME_UI.format(timeFrom);
		holder.timeFrom.setText(timeFromString);
		
		//from 
		holder.locationFrom.setText(Html.fromHtml("<i>"+context.getString(R.string.label_from)+" </i>"+myItinerary.getOriginalFrom().getName()));
		

		// time to
		Date timeTo = new Date(itinerary.getEndtime());
		String timeToString = Config.FORMAT_TIME_UI.format(timeTo);
		holder.timeTo.setText(timeToString);
		
		//to
		holder.locationTo.setText(Html.fromHtml("<i>"+context.getString(R.string.label_to)+" </i>"+myItinerary.getOriginalTo().getName()));


		// line between times
//		holder.line.addView(new LineDrawView(getContext()));

		// transport types
		List<TType> transportTypesList = new ArrayList<TType>();
		for (Leg l : itinerary.getLeg()) {
			if (!transportTypesList.contains(l.getTransport().getType())) {
				transportTypesList.add(l.getTransport().getType());
			}
		}

		

		 
		holder.transportTypes.removeAllViews();
		for (TType t : transportTypesList) {
			ImageView imgv =new ImageView(getContext());

		
			if (holder.transportTypes.getChildCount()>=2)
			{
				//rimuovi l'ultimo, aggiungi i 3 punti e esci dal for
				imgv.setImageResource(R.drawable.ic_three_dot);
				if (imgv.getDrawable() != null)
					{
						 LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,1);
						 imgv.setLayoutParams(lp);
						holder.transportTypes.removeViewAt(holder.transportTypes.getChildCount()-1);
						holder.transportTypes.addView(imgv);
						break;
					}
			}
			else {
				imgv = Utils.getImageByTType(getContext(), t);
			
			if (imgv.getDrawable() != null)
				{
					 LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,1);
					 imgv.setLayoutParams(lp);
					holder.transportTypes.addView(imgv);
				}
			}

		}
		if (holder.transportTypes.getChildCount()==1)
		{
			ImageView imgv =new ImageView(getContext());
			 LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,1);
			 imgv.setLayoutParams(lp);
			holder.transportTypes.addView(imgv);
		}
		

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
						new MonitorMyItineraryProcessor((SherlockFragmentActivity) context));
					task.execute(Boolean.toString(isChecked), myItineraries,position);
				} else {
					return;
				}
			}
		});
		return row;
	}

	static class RowHolder {
		TextView name;
		TextView day;
		TextView timeFrom;
		TextView locationFrom;
		ToggleButton monitor;
		FrameLayout line;
		TextView timeTo;
		TextView locationTo;
		LinearLayout transportTypes;
	}
	
	public class MonitorMyItineraryProcessor extends AbstractAsyncTaskProcessor<Object, Boolean> {

		Integer position;
		List<BasicItinerary> myItineraries;
		String id;
		
		public MonitorMyItineraryProcessor(SherlockFragmentActivity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(Object... params) throws SecurityException, Exception {
			// 0: monitor
			// 1: id
			boolean monitor = Boolean.parseBoolean((String) params[0]);
			 position = (Integer) params[2];
			 myItineraries=(List<BasicItinerary>) params[1];
			 id =  myItineraries.get(position).getClientId();
			return JPHelper.monitorMyItinerary(monitor, id);
		}

		@Override
		public void handleResult(Boolean result) {
			//cambia background in funzione a quello che ho
			myItineraries.get(position).setMonitor(result);
			notifyDataSetChanged();

		}
	}
}
