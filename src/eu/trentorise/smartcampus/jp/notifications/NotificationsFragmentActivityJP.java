package eu.trentorise.smartcampus.jp.notifications;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.feedback.activity.FeedbackFragmentActivity;
import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.helper.JPHelper;

public class NotificationsFragmentActivityJP extends FeedbackFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notification_fragment_jp);
		setUpContent();
	}

	private void setUpContent() {
		// Action bar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true); // system title
		actionBar.setHomeButtonEnabled(true);
		// actionBar.setDisplayShowHomeEnabled(true); // home icon bar
		actionBar.setDisplayHomeAsUpEnabled(true); // home as up
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public String getAppToken() {
		return Config.APP_TOKEN;
	}

	@Override
	public String getAuthToken() {
		return JPHelper.getAuthToken();
	}

}
