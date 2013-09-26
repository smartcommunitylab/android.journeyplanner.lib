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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;

import com.actionbarsherlock.view.MenuItem;
import com.github.espiandev.showcaseview.BaseTutorialActivity;

import eu.trentorise.smartcampus.jp.custom.TutorialActivity;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPHelper.Tutorial;

/**
 * @author raman
 * 
 */
public class TutorialManagerActivity extends BaseActivity {

	private final static int TUTORIAL_REQUEST_CODE = 10000;
	private Tutorial lastShowed;

	private void showTutorials() {
		JPHelper.Tutorial t = getFirstValidTutorial();

		String title = "", msg = "";
		boolean isLast = false;
		int id = R.id.btn_myprofile;
		if (t != null)
			switch (t) {
			case PLAN:
				id = R.id.btn_planjourney;
				title = getString(R.string.btn_planjourney);
				msg = getString(R.string.jp_plan_tut);
				break;
			case MONITOR:
				id = R.id.btn_monitorrecurrentjourney;
				title = getString(R.string.btn_monitorrecurrent);
				msg = getString(R.string.jp_monitor_tut);
				break;
			case WATCH:
				id = R.id.btn_monitorsavedjourney;
				title = getString(R.string.btn_monitorsaved);
				msg = getString(R.string.jp_watch_tut);
				break;
			case INFO:
				id = R.id.btn_smart;
				title = getString(R.string.btn_smartcheck);
				msg = getString(R.string.jp_info_tut);
				break;
			case SEND:
				id = R.id.btn_broadcast;
				title = getString(R.string.btn_broadcast);
				msg = getString(R.string.jp_send_tut);
				break;
			case NOTIF:
				id = R.id.btn_notifications;
				title = getString(R.string.btn_notifications);
				msg = getString(R.string.jp_notif_tut);
				break;
			case PREFST:
				id = R.id.btn_myprofile;
				title = getString(R.string.btn_myprofile);
				msg = getString(R.string.jp_prefs_tut);
				isLast = true;
				break;
			default:
				id = -1;
				break;
			}
		if (t != null) {
			lastShowed = t;
			displayShowcaseView(id, title, msg, isLast);
		} else
			JPHelper.setWantTour(this, false);
	}

	private void displayShowcaseView(int id, String title, String detail,
			boolean isLast) {
		int[] position = new int[2];
		View v = findViewById(id);

		if (v != null) {
			v.getLocationOnScreen(position);
			// scroll to the end of the screen
			// because the prefs button can be invisible
			// in landscape
			if (lastShowed == Tutorial.PREFST)
				((ScrollView) findViewById(R.id.jp_home_sv)).scrollTo(
						position[0], position[1]);

			v.getLocationOnScreen(position);
			BaseTutorialActivity.newIstance(this, position, v.getWidth(),
					Color.WHITE, null, title, detail, isLast,
					TUTORIAL_REQUEST_CODE, TutorialActivity.class);
		}
	}

	private Tutorial getFirstValidTutorial() {
		Tutorial t = JPHelper.getLastTutorialNotShowed(this);
		/* if smartcampus (no notif) salta notifiche (setta a true notif */
		ApplicationInfo ai;
		try {
			ai = getPackageManager().getApplicationInfo(getPackageName(),
					PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			if (aBundle.getBoolean("hidden-notification") && t != null
					&& t.equals(t.NOTIF)) {
				JPHelper.setTutorialAsShowed(this, t);
				t = JPHelper.getLastTutorialNotShowed(this);

			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t;
	}

	protected void showTourDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setMessage(getString(R.string.jp_first_launch))
				.setPositiveButton(getString(R.string.begin_tut),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								JPHelper.setWantTour(
										TutorialManagerActivity.this, true);
								showTutorials();
							}
						})
				.setNeutralButton(getString(android.R.string.cancel),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								JPHelper.setWantTour(
										TutorialManagerActivity.this, false);
								dialog.dismiss();
							}
						});
		builder.create().show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == TUTORIAL_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String resData = data.getExtras().getString(
						BaseTutorialActivity.RESULT_DATA);
				if (resData.equals(BaseTutorialActivity.OK))
					JPHelper.setTutorialVisibility(this, lastShowed, true);
			}

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_tutorial) {
			JPHelper.resetTutorialPreferences(this);
			JPHelper.setWantTour(getApplicationContext(), true);
			showTutorials();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		if (JPHelper.wantTour(this)) {
			showTutorials();
		}
	}


}
