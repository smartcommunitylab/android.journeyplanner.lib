package eu.trentorise.smartcampus.jp.custom;

import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;

import java.util.Map;
import java.util.Map.Entry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.jp.R;

public class DelaysDialogFragment extends SherlockDialogFragment {

	public static final String ARG_DELAYS = "delays";
	private Map<CreatorType, String> delays;

	public DelaysDialogFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.delays = (Map<CreatorType, String>) this.getArguments().getSerializable(ARG_DELAYS);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.delaysdialog, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		getDialog().setTitle(getSherlockActivity().getString(R.string.dialog_delays));

		LinearLayout delaysLinearLayout = (LinearLayout) getView().findViewById(R.id.delaysdialog);

		for (Entry<CreatorType, String> delay : delays.entrySet()) {
			TextView tv = new TextView(getSherlockActivity());
			tv.setTextAppearance(getSherlockActivity(), android.R.style.TextAppearance_Small);

			int stringResource = 0;
			
			if (delay.getKey().equals(CreatorType.USER)) {
				tv.setTextColor(getSherlockActivity().getResources().getColor(R.color.blue));
				if (delay.getValue().equalsIgnoreCase("1")) {
					stringResource = R.string.dialog_delay_user_1;
				} else {
					stringResource = R.string.dialog_delay_user;
				}
			} else {
				tv.setTextColor(getSherlockActivity().getResources().getColor(R.color.red));
				if (delay.getValue().equalsIgnoreCase("1")) {
					stringResource = R.string.dialog_delay_system_1;
				} else {
					stringResource = R.string.dialog_delay_system;
				}
			}
			
			tv.setText(getSherlockActivity().getString(stringResource, delay.getValue()));

			delaysLinearLayout.addView(tv);
		}

		Button btn_ok = (Button) getDialog().findViewById(R.id.delaysdialog_ok);
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});
	}
}
