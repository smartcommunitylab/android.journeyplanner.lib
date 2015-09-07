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

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.trentorise.smartcampus.jp.helper.Utils;
import eu.trentorise.smartcampus.jp.model.AlertStopTime;

public class StoptimesArrayAdapter extends ArrayAdapter<AlertStopTime> {

	Context context;
	int layoutResourceId;
	int textViewResourceId;
	List<AlertStopTime> stoptimesList;

	public StoptimesArrayAdapter(Context context, int layoutResourceId, int textViewResourceId, List<AlertStopTime> stoptimesList) {
		super(context, layoutResourceId, textViewResourceId, stoptimesList);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.textViewResourceId = textViewResourceId;
		this.stoptimesList = stoptimesList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	private View getCustomView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		DataHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DataHolder();
			holder.timeTextView = (TextView) row.findViewById(textViewResourceId);

			row.setTag(holder);
		} else {
			holder = (DataHolder) row.getTag();
		}

		AlertStopTime stoptime = this.getItem(position);
		holder.timeTextView.setText(Utils.millis2time(stoptime.getTime()));

		return row;
	}

	static class DataHolder {
		TextView timeTextView;
	}
}
