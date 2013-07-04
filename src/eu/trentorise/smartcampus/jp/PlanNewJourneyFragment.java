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

import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.data.message.journey.SingleJourney;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.android.common.GeocodingAutocompletionHelper;
import eu.trentorise.smartcampus.android.common.GeocodingAutocompletionHelper.OnAddressSelectedListener;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.SCGeocoder;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.android.map.InfoDialog;
import eu.trentorise.smartcampus.jp.custom.UserPrefsHolder;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.PrefsHelper;
import eu.trentorise.smartcampus.jp.helper.processor.PlanNewJourneyProcessor;

public class PlanNewJourneyFragment extends FeedbackFragment {

	protected static final String FROM = "from";
	protected static final String TO = "to";
	protected static final String FROM_STAR = "from_star";
	protected static final String TO_STAR = "to_star";

	protected SharedPreferences userPrefs;

	protected Position fromPosition;
	protected Position toPosition;
	protected UserPrefsHolder userPrefsHolder;

	protected boolean fromFav = false;
	protected boolean toFav = false;

	private ImageButton fromFavBtn;
	private ImageButton toFavBtn;

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		if (fromPosition != null) {
			savedInstanceState.putSerializable(FROM, fromPosition);
		}

		if (toPosition != null) {
			savedInstanceState.putSerializable(TO, toPosition);
		}

		fromFav = (Boolean) fromFavBtn.getTag();
		savedInstanceState.putBoolean(FROM_STAR, (Boolean) fromFav);

		toFav = (Boolean) toFavBtn.getTag();
		savedInstanceState.putBoolean(TO_STAR, (Boolean) toFav);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(FROM)) {
				fromPosition = (Position) savedInstanceState.getSerializable(FROM);
			}

			if (savedInstanceState.containsKey(TO)) {
				toPosition = (Position) savedInstanceState.getSerializable(TO);
			}

			if (savedInstanceState.containsKey(FROM_STAR)) {
				fromFav = savedInstanceState.getBoolean(FROM_STAR);
			}

			if (savedInstanceState.containsKey(TO_STAR)) {
				toFav = savedInstanceState.getBoolean(TO_STAR);
			}
		}

		Address from = null, to = null;
		if (getArguments() != null) {
			from = (Address) getArguments().getSerializable(getString(R.string.navigate_arg_from));
			to = (Address) getArguments().getSerializable(getString(R.string.navigate_arg_to));
		} else if (getActivity().getIntent() != null) {
			from = (Address) getActivity().getIntent().getParcelableExtra(getString(R.string.navigate_arg_from));
			to = (Address) getActivity().getIntent().getParcelableExtra(getString(R.string.navigate_arg_to));
		}

		if (from != null) {
			findAddressForField(FROM, new GeoPoint((int) (from.getLatitude() * 1E6), (int) (from.getLongitude() * 1E6)));
		}

		if (to != null) {
			findAddressForField(TO, new GeoPoint((int) (to.getLatitude() * 1E6), (int) (to.getLongitude() * 1E6)));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.plannewjourney, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		// // turn on location helper
		// JPHelper.getLocationHelper().start();

		setUpLocationControls();
		setUpPreferenceControls();
		setUpTimingControls();
		setUpMainOperation();

		super.onResume();
	}

	protected void setUpMainOperation() {
		Button searchBtn = (Button) getView().findViewById(R.id.plannew_search);
		searchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// user preferences
				ToggleButton useCustomPrefsToggleBtn = (ToggleButton) getView().findViewById(R.id.plannew_options_toggle);
				View userPrefsLayout = (View) getView().findViewById(R.id.plannew_userprefs);

				if (useCustomPrefsToggleBtn.isChecked()) {
					TableLayout tTypesTableLayout = (TableLayout) userPrefsLayout.findViewById(R.id.transporttypes_table);
					RadioGroup rTypesRadioGroup = (RadioGroup) userPrefsLayout.findViewById(R.id.routetypes_radioGroup);
					userPrefsHolder = PrefsHelper.userPrefsViews2Holder(tTypesTableLayout, rTypesRadioGroup, userPrefs);
				} else {
					userPrefsHolder = PrefsHelper.sharedPreferences2Holder(userPrefs);
				}

				Date fromDate = (Date) getView().findViewById(R.id.plannew_date).getTag();
				Date fromTime = (Date) getView().findViewById(R.id.plannew_time).getTag();

				if (fromPosition == null) {
					Toast.makeText(getActivity(), R.string.from_field_empty, Toast.LENGTH_SHORT).show();
					return;
				}
				if (toPosition == null) {
					Toast.makeText(getActivity(), R.string.to_field_empty, Toast.LENGTH_SHORT).show();
					return;
				}

				if (!eu.trentorise.smartcampus.jp.helper.Utils.validFromDateTime(fromDate, fromTime)) {
					Toast.makeText(getActivity(), R.string.datetime_before_now, Toast.LENGTH_SHORT).show();
					return;
				}

				// build SingleJourney
				SingleJourney sj = new SingleJourney();

				sj.setFrom(fromPosition);
				sj.setTo(toPosition);

				// NEW! Date in getTag() support
				sj.setDate(Config.FORMAT_DATE_SMARTPLANNER.format(fromDate));
				sj.setDepartureTime(Config.FORMAT_TIME_SMARTPLANNER.format(fromTime));

				sj.setTransportTypes((TType[]) userPrefsHolder.getTransportTypes());
				sj.setRouteType(userPrefsHolder.getRouteType());

				SCAsyncTask<SingleJourney, Void, List<Itinerary>> task = new SCAsyncTask<SingleJourney, Void, List<Itinerary>>(
						getSherlockActivity(), new PlanNewJourneyProcessor(getSherlockActivity(), sj,
								PlanNewJourneyFragment.this.getTag()));
				task.execute(sj);
			}
		});
		// hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchBtn.getWindowToken(), 0);
	}

	protected void setUpTimingControls() {
		Date now = new Date();

		final EditText dateEditText = (EditText) getView().findViewById(R.id.plannew_date);
		dateEditText.setTag(now);
		dateEditText.setText(Config.FORMAT_DATE_UI.format(now));
		dateEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment f = DatePickerDialogFragment.newInstance((EditText) v);
				// f.setArguments(DatePickerDialogFragment.prepareData(f.toString()));
				f.show(getSherlockActivity().getSupportFragmentManager(), "datePicker");
			}
		});

		final EditText timeEditText = (EditText) getView().findViewById(R.id.plannew_time);
		timeEditText.setTag(now);
		timeEditText.setText(Config.FORMAT_TIME_UI.format(now));
		timeEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment f = TimePickerDialogFragment.newInstance((EditText) v);
				 f.setArguments(TimePickerDialogFragment.prepareData(timeEditText.toString()));
				f.show(getSherlockActivity().getSupportFragmentManager(), "timePicker");
			}
		});
	}

	protected void setUpLocationControls() {
		List<Double> mapcenter = JPParamsHelper.getCenterMap();
		double[] refLoc = mapcenter == null? null : new double[]{mapcenter.get(0),mapcenter.get(1)};

		AutoCompleteTextView fromEditText = (AutoCompleteTextView) getView().findViewById(R.id.plannew_from_text);
		GeocodingAutocompletionHelper fromAutocompletionHelper = new GeocodingAutocompletionHelper(getSherlockActivity(),
				fromEditText, Config.TN_REGION, Config.TN_COUNTRY, Config.TN_ADM_AREA, refLoc);
		fromAutocompletionHelper.setOnAddressSelectedListener(new OnAddressSelectedListener() {
			@Override
			public void onAddressSelected(Address address) {
				savePosition(address, FROM);
			}
		});
		if (fromPosition != null) {
			fromEditText.setText(fromPosition.getName());
		}

		AutoCompleteTextView toEditText = (AutoCompleteTextView) getView().findViewById(R.id.plannew_to_text);
		GeocodingAutocompletionHelper toAutocompletionHelper = new GeocodingAutocompletionHelper(getSherlockActivity(),
				toEditText, Config.TN_REGION, Config.TN_COUNTRY, Config.TN_ADM_AREA, refLoc);
		toAutocompletionHelper.setOnAddressSelectedListener(new OnAddressSelectedListener() {
			@Override
			public void onAddressSelected(Address address) {
				savePosition(address, TO);
			}
		});
		if (toPosition != null) {
			toEditText.setText(toPosition.getName());
		}

		// };
		ImageButton opt = (ImageButton) getView().findViewById(R.id.plannew_from_opt);
		opt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createPositionDialog(FROM).show();
			}
		});

		opt = (ImageButton) getView().findViewById(R.id.plannew_to_opt);
		opt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createPositionDialog(TO).show();
			}
		});

		fromFavBtn = (ImageButton) getView().findViewById(R.id.plannew_from_star);
		toggleStar(fromFavBtn, fromFav);
		fromFavBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String fromString = ((AutoCompleteTextView) getView().findViewById(R.id.plannew_from_text)).getText()
						.toString().trim();
				if (fromString.length() == 0 || isFavorite(fromString)) {
					createFavoritesDialog(FROM);
					// Toast.makeText(getActivity(), R.string.from_field_empty,
					// Toast.LENGTH_SHORT).show();
				} else {
					// TODO: dialog to confirm
					createFavoritesConfirmDialog(FROM);
				}
			}
		});

		toFavBtn = (ImageButton) getView().findViewById(R.id.plannew_to_star);
		toggleStar(toFavBtn, toFav);
		toFavBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String toString = ((AutoCompleteTextView) getView().findViewById(R.id.plannew_to_text)).getText().toString()
						.trim();
				if (toString.length() == 0 || isFavorite(toString)) {
					createFavoritesDialog(TO);
					// Toast.makeText(getActivity(), R.string.to_field_empty,
					// Toast.LENGTH_SHORT).show();
				} else {
					// TODO: dialog to confirm
					createFavoritesConfirmDialog(TO);
				}
			}
		});

		// favorite imagebuttons management
		fromEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				boolean fav = isFavorite(((AutoCompleteTextView) getView().findViewById(R.id.plannew_from_text)).getText()
						.toString());
				toggleStar(fromFavBtn, fav);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		toEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				boolean fav = isFavorite(((AutoCompleteTextView) getView().findViewById(R.id.plannew_to_text)).getText()
						.toString());
				toggleStar(toFavBtn, fav);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();

		// // turn off LocationHelper
		// JPHelper.getLocationHelper().stop();

		// hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		EditText timeEditText = (EditText) getView().findViewById(R.id.plannew_time);
		if (timeEditText != null) {
			imm.hideSoftInputFromWindow(timeEditText.getWindowToken(), 0);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	protected void setUpPreferenceControls() {
		userPrefs = getSherlockActivity().getSharedPreferences(Config.USER_PREFS, Context.MODE_PRIVATE);

		if (userPrefsHolder == null) {
			userPrefsHolder = PrefsHelper.sharedPreferences2Holder(userPrefs);
		}

		final View userPrefsLayout = (View) getView().findViewById(R.id.plannew_userprefs);
		PrefsHelper.buildUserPrefsView(getSherlockActivity(), userPrefsHolder, userPrefsLayout);

		ToggleButton useCustomPrefsToggleBtn = (ToggleButton) getView().findViewById(R.id.plannew_options_toggle);
		if (useCustomPrefsToggleBtn.isChecked()) {
			userPrefsLayout.setVisibility(View.VISIBLE);
		} else {
			userPrefsLayout.setVisibility(View.GONE);
		}

		useCustomPrefsToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					userPrefsLayout.setVisibility(View.VISIBLE);
				} else {
					userPrefsLayout.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent result) {
		// super.onActivityResult(requestCode, resultCode, result);
		if (resultCode == InfoDialog.RESULT_SELECTED) {
			Address address = result.getParcelableExtra("address");
			String field = result.getExtras().getString("field");
			savePosition(address, field);
		}
	}

	private void savePosition(Address address, String field) {
		EditText text = null;
		ImageButton imgBtn = null;

		String s = "";
		for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
			s += address.getAddressLine(i) + " ";
		}
		s = s.trim();

		if (field.equals(FROM)) {
			fromPosition = new Position(address.getAddressLine(0), null, null, Double.toString(address.getLongitude()),
					Double.toString(address.getLatitude()));
			if (getView() != null) {
				text = (EditText) getView().findViewById(R.id.plannew_from_text);
				imgBtn = (ImageButton) getView().findViewById(R.id.plannew_from_star);
			}
		} else if (field.equals(TO)) {
			toPosition = new Position(address.getAddressLine(0), null, null, Double.toString(address.getLongitude()),
					Double.toString(address.getLatitude()));

			if (getView() != null) {
				text = (EditText) getView().findViewById(R.id.plannew_to_text);
				imgBtn = (ImageButton) getView().findViewById(R.id.plannew_to_star);
			}
		}

		if (text != null) {
			text.setFocusable(false);
			text.setFocusableInTouchMode(false);
			text.setText(s);
			text.setFocusable(true);
			text.setFocusableInTouchMode(true);
		}

		if (imgBtn != null) {
			for (Position p : userPrefsHolder.getFavorites()) {
				if (address.getAddressLine(0).equalsIgnoreCase(p.getName())
						|| (address.getLatitude() == Double.parseDouble(p.getLat()) && address.getLongitude() == Double
								.parseDouble(p.getLon()))) {
					imgBtn.setImageResource(R.drawable.ic_fav_star_active);
					imgBtn.setTag(true);
				}
			}
		}
	}

	private AlertDialog createPositionDialog(final String field) {
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
						findAddressForField(field, hereGeoPoint);
					}
					break;
				case 1:
					Intent intent = new Intent(getSherlockActivity(), AddressSelectActivity.class);
					intent.putExtra("field", field);
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

	private void findAddressForField(final String field, GeoPoint point) {
		List<Address> hereAddressesList = new SCGeocoder(getSherlockActivity()).findAddressesAsync(point);
		if (hereAddressesList != null && !hereAddressesList.isEmpty()) {
			Address hereAddress = hereAddressesList.get(0);
			savePosition(hereAddress, field);
		} else {
			Address customAddress = new Address(Locale.getDefault());
			customAddress.setLatitude(point.getLatitudeE6() / 1E6);
			customAddress.setLongitude(point.getLongitudeE6() / 1E6);
			customAddress.setAddressLine(
					0,
					"LON " + Double.toString(customAddress.getLongitude()) + ", LAT "
							+ Double.toString(customAddress.getLatitude()));
			savePosition(customAddress, field);
		}
	}

	protected void createFavoritesDialog(final String field) {
		if (userPrefsHolder != null && userPrefsHolder.getFavorites() != null && !userPrefsHolder.getFavorites().isEmpty()) {
			final List<Position> list = userPrefsHolder.getFavorites();
			String[] items = new String[list.size()];
			for (int i = 0; i < items.length; i++) {
				items[i] = list.get(i).getName();
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
			builder.setTitle(getString(R.string.favorites_title));

			builder.setItems(items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// to be replaced with real favorites list
					Address address = new Address(Locale.getDefault());
					Position p = list.get(which);
					address.setLatitude(Double.parseDouble(p.getLat()));
					address.setLongitude(Double.parseDouble(p.getLon()));
					address.setAddressLine(0, p.getName());
					savePosition(address, field);

					if (FROM.equalsIgnoreCase(field)) {
						ImageButton ibtn = (ImageButton) getView().findViewById(R.id.plannew_from_star);
						ibtn.setImageResource(R.drawable.ic_fav_star_active);
						ibtn.setTag(true);
					} else if (TO.equalsIgnoreCase(field)) {
						ImageButton ibtn = (ImageButton) getView().findViewById(R.id.plannew_to_star);
						ibtn.setImageResource(R.drawable.ic_fav_star_active);
						ibtn.setTag(true);
					}
				}
			});

			builder.create().show();
		} else {
			Toast.makeText(getActivity(), R.string.favorites_empty_list, Toast.LENGTH_SHORT).show();
		}
	}

	protected void createFavoritesConfirmDialog(final String field) {
		final Position position;
		if (FROM.equals(field)) {
			position = fromPosition;
		} else if (TO.equals(field)) {
			position = toPosition;
		} else {
			position = null;
		}

		if (position != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
			builder.setTitle(getString(R.string.favorites_title));
			builder.setMessage(getString(R.string.favorites_add_confirmation, position.getName()));

			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Position savedPosition = addToFavorites(position);
					if (savedPosition != null) {
						toggleStar(field, true);
						dialog.dismiss();
					}
				}
			});

			builder.create().show();
		} else {
			Toast.makeText(getActivity(), R.string.favorites_empty_address, Toast.LENGTH_SHORT).show();
		}
	}

	protected Position addToFavorites(Position position) {
		if (position == null) {
			Toast.makeText(getActivity(), R.string.favorites_empty_address, Toast.LENGTH_SHORT).show();
		} else {
			if (saveFavorite(position, userPrefsHolder)) {
				Toast.makeText(getActivity(), R.string.favorites_saved, Toast.LENGTH_SHORT).show();
				return position;
			}
		}

		return null;
	}

	private boolean saveFavorite(Position position, UserPrefsHolder holder) {
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

	private boolean isFavorite(String text) {
		text = text.trim();
		if (text.length() > 0) {
			for (Position p : userPrefsHolder.getFavorites()) {
				if (p.getName().equalsIgnoreCase(text)) {
					return true;
				}
			}
		}
		return false;
	}

	private void toggleStar(ImageButton btn, Boolean setActive) {
		if (setActive != null) {
			btn.setTag(setActive);
			if (setActive) {
				btn.setImageResource(R.drawable.ic_fav_star_active);
			} else {
				btn.setImageResource(R.drawable.ic_fav_star);
			}
		} else {
			if ((Boolean) btn.getTag()) {
				btn.setTag(false);
				btn.setImageResource(R.drawable.ic_fav_star);
			} else {
				btn.setTag(true);
				btn.setImageResource(R.drawable.ic_fav_star_active);
			}
		}
	}

	private void toggleStar(String field, Boolean setActive) {
		ImageButton ibtn = null;

		if (FROM.equalsIgnoreCase(field)) {
			ibtn = (ImageButton) getView().findViewById(R.id.plannew_from_star);
		} else if (TO.equalsIgnoreCase(field)) {
			ibtn = (ImageButton) getView().findViewById(R.id.plannew_to_star);
		}

		if (ibtn != null) {
			toggleStar(ibtn, null);
		}
	}

}
