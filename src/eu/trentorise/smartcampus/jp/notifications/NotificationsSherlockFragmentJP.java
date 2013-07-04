package eu.trentorise.smartcampus.jp.notifications;

import android.content.Intent;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.jp.R;

public class NotificationsSherlockFragmentJP extends SherlockFragment {

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_notifications, 0, R.string.notifications_unread);
		item.setIcon(R.drawable.ic_menu_notifications);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_item_notifications) {
			Intent intent = new Intent(getSherlockActivity().getApplicationContext(), NotificationsFragmentActivityJP.class);
			// intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			// getSherlockActivity().overridePendingTransition(android.R.anim.slide_in_left,
			// android.R.anim.slide_out_right);
		}

		return super.onOptionsItemSelected(item);
	}

}
