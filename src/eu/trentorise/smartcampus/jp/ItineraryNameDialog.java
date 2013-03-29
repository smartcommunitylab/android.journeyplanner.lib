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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import eu.trentorise.smartcampus.jp.custom.DialogHandler;

public class ItineraryNameDialog extends Dialog {

	private DialogHandler<String> handler = null;
	private String suggestedName;

	private EditText itineraryNameEditText;

	public ItineraryNameDialog(Context context, DialogHandler<String> handler, String suggestedName) {
		super(context);
		this.handler = handler;
		this.suggestedName = suggestedName;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.itineraryname_dialog);

		setTitle(R.string.dialog_itineraryname_hint);

		itineraryNameEditText = (EditText) findViewById(R.id.itineraryname_text);
		itineraryNameEditText.setText(suggestedName);

		Button okBtn = (Button) findViewById(R.id.itineraryname_add);
		Button cancelBtn = (Button) findViewById(R.id.itineraryname_cancel);

		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
				Toast toast = Toast.makeText(getContext(), R.string.toast_itinerary_not_saved, Toast.LENGTH_SHORT);
				toast.show();
			}
		});

		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (itineraryNameEditText.getText().length() > 0) {
					handler.handleSuccess(itineraryNameEditText.getText().toString());
					dismiss();
				} else {
					Toast toast = Toast.makeText(getContext(), R.string.toast_itinerary_name_empty, Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});
	}

}
