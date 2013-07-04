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

import it.sayservice.platform.smartplanner.data.message.Position;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.R;

public class FavoritesAdapter extends ArrayAdapter<Position> {
	private Context context;
	private int layoutResourceId;

	public FavoritesAdapter(Context context, int layoutResourceId, UserPrefsHolder holder) {
		super(context, layoutResourceId, holder.getFavorites() == null ? new ArrayList<Position>() : holder.getFavorites());
		this.context = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		DataHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DataHolder();
			holder.favoriteTextView = (TextView) row.findViewById(R.id.favorites_tv);
			holder.deleteButton = (ImageButton) row.findViewById(R.id.favorites_del);

			row.setTag(holder);
		} else {
			holder = (DataHolder) row.getTag();
		}

		holder.favoriteTextView.setText(getItem(position).getName());
		holder.deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Position p = getItem(position);
				remove(p);

				List<Position> updatedPositions = new ArrayList<Position>();
				for (int i = 0; i < getCount(); i++) {
					updatedPositions.add(getItem(i));
				}

				SharedPreferences userPrefs = context.getSharedPreferences(Config.USER_PREFS, Context.MODE_PRIVATE);
				String json = Utils.convertToJSON(updatedPositions);
				SharedPreferences.Editor prefsEditor = userPrefs.edit();
				prefsEditor.putString(Config.USER_PREFS_FAVORITES, json);
				prefsEditor.commit();

				remove(p);
				notifyDataSetChanged();
				Toast.makeText(getContext(), context.getString(R.string.toast_position_deleted, p.getName()),
						Toast.LENGTH_SHORT).show();
			}
		});
		return row;
	}

	public static class DataHolder {
		public TextView favoriteTextView;
		public ImageButton deleteButton;
	}
}
