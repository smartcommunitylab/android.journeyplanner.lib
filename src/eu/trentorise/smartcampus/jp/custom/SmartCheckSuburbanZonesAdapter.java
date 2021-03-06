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
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;

public class SmartCheckSuburbanZonesAdapter extends ArrayAdapter<SmartLine> {

	Context mContext;
	int layoutResourceId;
	List<SmartLine> routes;

	public SmartCheckSuburbanZonesAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.mContext = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Holder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new Holder();
			holder.zoneName = (TextView) row.findViewById(android.R.id.text1);

			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}

		SmartLine smartZone = getItem(position);

		holder.zoneName.setText(smartZone.getLine());

		// colored text, white background
		holder.zoneName.setTextColor(smartZone.getColor());
		// white text, colored background
		// holder.zoneName.setTextColor(mContext.getResources().getColor(android.R.color.white));
		// holder.zoneName.setBackgroundColor(smartZone.getColor());
		holder.zoneName.setTextAppearance(mContext, android.R.attr.textAppearanceMedium);

		return row;
	}

	private class Holder {
		TextView zoneName;
	}

}
