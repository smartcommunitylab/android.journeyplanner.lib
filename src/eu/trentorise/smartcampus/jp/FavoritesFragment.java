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
package eu.trentorise.smartcampus.jp;

import it.sayservice.platform.smartplanner.data.message.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.android.common.GeocodingAutocompletionHelper;
import eu.trentorise.smartcampus.android.common.GeocodingAutocompletionHelper.OnAddressSelectedListener;
import eu.trentorise.smartcampus.android.common.SCGeocoder;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.android.map.InfoDialog;
import eu.trentorise.smartcampus.jp.custom.FavoritesAdapter;
import eu.trentorise.smartcampus.jp.custom.UserPrefsHolder;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.PrefsHelper;

public class FavoritesFragment extends FeedbackFragment {
	// to be replaced with real favorites
	private UserPrefsHolder userPrefsHolder = null;
	private FavoritesAdapter favoritesAdapter;
	private Position newFavPosition;
	
	private View addDialogView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO: REMEMBER THIS!!!
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.favorites, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		ListView list = (ListView) getSherlockActivity().findViewById(R.id.favorites_list);
		if (userPrefsHolder == null) {
			userPrefsHolder = PrefsHelper.sharedPreferences2Holder(getSherlockActivity().getSharedPreferences(
					Config.USER_PREFS, Context.MODE_PRIVATE));
		}
		favoritesAdapter = new FavoritesAdapter(getActivity(), R.layout.favorites_row, userPrefsHolder);
		list.setAdapter(favoritesAdapter);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.addmenu, menu);

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_add) {
			// Toast.makeText(getSherlockActivity(), "Coming soon",
			// Toast.LENGTH_SHORT).show();
			createAddDialog();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent result) {
		// super.onActivityResult(requestCode, resultCode, result);
		if (resultCode == InfoDialog.RESULT_SELECTED) {
			Address address = result.getParcelableExtra("address");
			savePosition(address);
		}
	}

	private void createAddDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
		builder.setTitle(getString(R.string.favorites_title));
		addDialogView = getSherlockActivity().getLayoutInflater().inflate(R.layout.addfavorite, null);

		List<Double> mapcenter = JPParamsHelper.getCenterMap();
		double[] refLoc = mapcenter == null ? null : new double[] { mapcenter.get(0), mapcenter.get(1) };

		/*
		 * edittext
		 */
		final AutoCompleteTextView editText = (AutoCompleteTextView) addDialogView.findViewById(R.id.addfav_actext);
		GeocodingAutocompletionHelper autocompletionHelper = new GeocodingAutocompletionHelper(getSherlockActivity(), editText,
				Config.TN_REGION, Config.TN_COUNTRY, Config.TN_ADM_AREA, refLoc);
		autocompletionHelper.setOnAddressSelectedListener(new OnAddressSelectedListener() {
			@Override
			public void onAddressSelected(Address address) {
				savePosition(address);
			}
		});
		if (newFavPosition != null) {
			editText.setText(newFavPosition.getName());
		}

		/*
		 * button
		 */
		ImageButton opt = (ImageButton) addDialogView.findViewById(R.id.addfav_pos);
		opt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createPositionDialog().show();
			}
		});

		builder.setView(addDialogView);

		/*
		 * dialog buttons
		 */
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (newFavPosition == null) {
					Toast.makeText(getActivity(), R.string.favorites_empty_address, Toast.LENGTH_SHORT).show();
				} else if (saveFavorite(newFavPosition, userPrefsHolder)) {
					Toast.makeText(getActivity(), R.string.favorites_saved, Toast.LENGTH_SHORT).show();
					favoritesAdapter.notifyDataSetChanged();
					editText.setText("");
					dialog.dismiss();
				}
			}
		});

		builder.create().show();
	}

	private void savePosition(Address address) {
		EditText text = null;

		String s = "";
		for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
			s += address.getAddressLine(i) + " ";
		}
		s = s.trim();

		newFavPosition = new Position(address.getAddressLine(0), null, null, Double.toString(address.getLongitude()),
				Double.toString(address.getLatitude()));
		if (addDialogView != null) {
			text = (EditText) addDialogView.findViewById(R.id.addfav_actext);
		}

		if (text != null) {
			text.setFocusable(false);
			text.setFocusableInTouchMode(false);
			text.setText(s);
			text.setFocusable(true);
			text.setFocusableInTouchMode(true);
		}
	}

	private AlertDialog createPositionDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
		// final CharSequence[] items = (userPrefsHolder.getFavorites() != null
		// && !userPrefsHolder.getFavorites().isEmpty()) ? new CharSequence[] {
		// getString(R.string.address_dlg_current),
		// getString(R.string.address_dlg_map),
		// getString(R.string.address_dlg_favorites) }
		// : new CharSequence[] { getString(R.string.address_dlg_current),
		// getString(R.string.address_dlg_map) };

		final CharSequence[] items = new CharSequence[] { getString(R.string.address_dlg_current),
				getString(R.string.address_dlg_map) };

		builder.setTitle(getString(R.string.address_dlg_title));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0:
					// GeoPoint hereGeoPoint =
					// MapManager.requestMyLocation(getSherlockActivity());
					GeoPoint hereGeoPoint = JPHelper.getLocationHelper().getLocation();
					if (hereGeoPoint != null) {
						findAddressForField(hereGeoPoint);
					}
					break;
				case 1:
					Intent intent = new Intent(getSherlockActivity(), AddressSelectActivity.class);
					intent.putExtra("field", "none");
					startActivityForResult(intent, InfoDialog.RESULT_SELECTED);
					break;
				// case 2:
				// createFavoritesDialog(field);
				// break;
				default:
					break;
				}
			}
		});
		return builder.create();
	}

	private void findAddressForField(GeoPoint point) {
		List<Address> hereAddressesList = new SCGeocoder(getSherlockActivity()).findAddressesAsync(point);
		if (hereAddressesList != null && !hereAddressesList.isEmpty()) {
			Address hereAddress = hereAddressesList.get(0);
			savePosition(hereAddress);
		} else {
			Address customAddress = new Address(Locale.getDefault());
			customAddress.setLatitude(point.getLatitudeE6() / 1E6);
			customAddress.setLongitude(point.getLongitudeE6() / 1E6);
			customAddress.setAddressLine(
					0,
					"LON " + Double.toString(customAddress.getLongitude()) + ", LAT "
							+ Double.toString(customAddress.getLatitude()));
			savePosition(customAddress);
		}
	}

	// protected Position addToFavorites(Position position) {
	// if (position == null) {
	// Toast.makeText(getActivity(), R.string.favorites_empty_address,
	// Toast.LENGTH_SHORT).show();
	// } else {
	// if (saveFavorite(position, userPrefsHolder)) {
	// Toast.makeText(getActivity(), R.string.favorites_saved,
	// Toast.LENGTH_SHORT).show();
	// favoritesAdapter.notifyDataSetChanged();
	// return position;
	// }
	// }
	//
	// return null;
	// }

	private boolean saveFavorite(Position position, UserPrefsHolder holder) {
		SharedPreferences userPrefs = getSherlockActivity().getSharedPreferences(Config.USER_PREFS, Context.MODE_PRIVATE);

		if (holder.getFavorites() == null) {
			holder.setFavorites(new ArrayList<Position>());
		}
		for (Position p : holder.getFavorites()) {
			if (p.getLat().equals(position.getLat()) && p.getLon().equals(position.getLon())
					&& p.getName().equals(position.getName())) {
				return true;
			}
		}
		holder.getFavorites().add(position);
		String json = Utils.convertToJSON(holder.getFavorites());
		SharedPreferences.Editor prefsEditor = userPrefs.edit();
		prefsEditor.putString(Config.USER_PREFS_FAVORITES, json);
		return prefsEditor.commit();
	}

}
