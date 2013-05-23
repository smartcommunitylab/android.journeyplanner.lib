package eu.trentorise.smartcampus.jp.custom;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.trentorise.smartcampus.jp.R;

public class SmartCheckListAdapter extends ArrayAdapter<String> {

	private Context context;
	private int layoutResourceId;

	public SmartCheckListAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new ViewHolder();
			holder.optionName = (TextView) row.findViewById(R.id.smart_option);
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		holder.optionName.setText(getItem(position));

		return row;
	}

	public static class ViewHolder {
		TextView optionName;
	}

}
