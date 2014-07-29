package eu.trentorise.smartcampus.jp.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.pushservice.NotificationCenter;

public class NotificationsListFragmentJP extends SherlockListFragment {

	private NotificationsListAdapterJP adapter;
	private final static String CORE_MOBILITY = "core.mobility";
	private static final int MAX_MSG = 50;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
		return inflater.inflate(R.layout.notifications_list_jp, container);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		FeedbackFragmentInflater.inflateHandleButton(getSherlockActivity(),
				getView());

		adapter = new NotificationsListAdapterJP(getActivity(),
				R.layout.notifications_row_jp);
		setListAdapter(adapter);
		adapter.clear();
		TextView empty = new TextView(getActivity());
		empty.setText(R.string.notifications_list_empty_jp);
		getListView().setEmptyView(empty);
		

		// instantiate again JPHelper if needed
		if (!JPHelper.isInitialized()) {
			JPHelper.init(getSherlockActivity());
		}

		adapter.addAll(JPHelper.notificationCenter.getNotifications());
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//TODO fix this code
//		if (adapter.getItem(position).getEntities() != null
//				&& !adapter.getItem(position).getEntities().isEmpty()) {
//			String objectId = adapter.getItem(position).getEntities().get(0)
//					.getId();
//			SCAsyncTask<String, Void, Object> viewDetailsTask = new SCAsyncTask<String, Void, Object>(
//					getSherlockActivity(),
//					new NotificationsAsyncTaskProcessorJP(getSherlockActivity()));
//			viewDetailsTask.execute(objectId);
//		}
	}

	//TODO remove all this comments when push works
	@Override
	public void onDestroy() {
		JPHelper.notificationCenter.markAllNotificationAsRead();

		super.onDestroy();
	}

//	public static NotificationFilter getNotificationFilter() {
//		NotificationFilter filter = new NotificationFilter();
//		filter.setOrdering(ORDERING.ORDER_BY_ARRIVAL);
//		return filter;
//	}

	/*
	 * AsyncTask
	 */
//	private class NotificationsAsyncTaskProcessorJP extends
//			AbstractAsyncTaskProcessor<String, Object> {
//
//		private Context ctx;
//
//		public NotificationsAsyncTaskProcessorJP(Activity activity) {
//			super(activity);
//			ctx = activity.getApplicationContext();
//		}
//
//		@Override
//		public Object performAction(String... params) throws SecurityException,
//				ConnectionException, Exception {
//			String id = params[0];
//
//			return JPHelper.getItineraryObject(id, JPHelper.getAuthToken(ctx));
//		}
//
//		@Override
//		public void handleResult(Object result) {
//			if (result == null)
//				return;
//
//			Fragment fragment = null;
//			FragmentTransaction fragmentTransaction = getSherlockActivity()
//					.getSupportFragmentManager().beginTransaction();
//			fragmentTransaction
//					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//
//			if (result instanceof BasicItinerary) {
//				fragment = MyItineraryFragment
//						.newInstance((BasicItinerary) result);
//				fragmentTransaction.replace(Config.mainlayout, fragment,
//						Config.MY_JOURNEYS_FRAGMENT_TAG);
//			} else if (result instanceof BasicRecurrentJourney) {
//				fragment = new MyRecurItineraryFragment();
//				Bundle b = new Bundle();
//				b.putSerializable(MyRecurItineraryFragment.PARAMS,
//						(BasicRecurrentJourney) result);
//				b.putBoolean(MyRecurItineraryFragment.PARAM_EDITING, false);
//				fragment.setArguments(b);
//				fragmentTransaction.replace(Config.mainlayout, fragment,
//						Config.MY_RECUR_JOURNEYS_FRAGMENT_TAG);
//			}
//
//			if (fragment != null) {
//				fragmentTransaction.addToBackStack(fragment.getTag());
//				fragmentTransaction.commit();
//			}
//		}

	}
//TODO remove this once push works
//	private class NotificationsLoader extends
//			AbstractAsyncTaskProcessor<Void, List<Notification>> {
//
//		public NotificationsLoader(Activity activity) {
//			super(activity);
//		}
//
//		@Override
//		public List<Notification> performAction(Void... params)
//				throws SecurityException, ConnectionException, Exception {
//			return NotificationsHelper.getNotifications(
//					getNotificationFilter(), 0, -1, 0);
//		}
//
//		@Override
//		public void handleResult(List<Notification> notificationsList) {
//			TextView listEmptyTextView = (TextView) getView().findViewById(
//					R.id.jp_list_text_empty);
//
//			if (!notificationsList.isEmpty()) {
//				adapter.clear();
//				for (Notification n : notificationsList) {
//					adapter.add(n);
//				}
//				listEmptyTextView.setVisibility(View.GONE);
//				adapter.notifyDataSetChanged();
//			} else if (adapter.isEmpty()) {
//				listEmptyTextView.setVisibility(View.VISIBLE);
//			}
//		}
//	}
//
//}
