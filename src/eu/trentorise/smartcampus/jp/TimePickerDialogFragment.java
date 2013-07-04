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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class TimePickerDialogFragment extends SherlockDialogFragment implements TimePickerDialog.OnTimeSetListener {

	private EditText timeEditText;
	private static final String DATA = "data";

	public static Bundle prepareData(String time) {
		Bundle b = new Bundle();
		b.putString(DATA, time);
		return b;
	}

	static TimePickerDialogFragment newInstance(EditText timeEditText) {
		TimePickerDialogFragment f = new TimePickerDialogFragment();
		f.setTimeEditText(timeEditText);
		return f;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// return super.onCreateDialog(savedInstanceState);

		final Calendar c = Calendar.getInstance();
		// if (getArguments() != null && getArguments().containsKey(DATA)) {
		 try {
		 Date d = Config.FORMAT_TIME_UI.parse((String)getArguments().getString(DATA));
		 c.setTime(d);
		 } catch (ParseException e) {
			 e.printStackTrace();
		 }
		// }

		if (getTimeEditText().getTag() != null) {
			c.setTime((Date) getTimeEditText().getTag());
		}

		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getSherlockActivity(), this, hour, minute, DateFormat.is24HourFormat(getSherlockActivity()));
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		Date date = calendar.getTime();
		String formattedDate = Config.FORMAT_TIME_UI.format(date);
		getTimeEditText().setTag(date);
		getTimeEditText().setText(formattedDate);
		getDialog().dismiss();
	}

	public EditText getTimeEditText() {
		return timeEditText;
	}

	public void setTimeEditText(EditText timeEditText) {
		this.timeEditText = timeEditText;
	}

}
