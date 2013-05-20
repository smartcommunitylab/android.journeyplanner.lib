package eu.trentorise.smartcampus.jp.custom;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.trentorise.smartcampus.jp.R;

public class SmartCheckDirectionAdapter extends ArrayAdapter<String> {

	private Context context;
	int layoutResourceId;

	// private ArrayList<String> directions;

	// public SmartCheckDirectionAdapter(Context context, int resource,
	// ArrayList<String> directions) {
	public SmartCheckDirectionAdapter(Context context, int layoutResourceId) {
		// super(context, layoutResourceId, directions);
		super(context, layoutResourceId);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		// this.directions = directions;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new ViewHolder();
			holder.direction = (TextView) row.findViewById(R.id.smart_option);
			holder.direction.setText(getItem(position));
			row.setTag(holder);

		} else {
			holder = (ViewHolder) row.getTag();
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new ViewHolder();
			holder.direction = (TextView) row.findViewById(R.id.smart_option);
			holder.direction.setText(getItem(position));
			row.setTag(holder);
		}

		return row;
	}

	public static class ViewHolder {
		TextView direction;
		// ImageView icon;
	}

}
