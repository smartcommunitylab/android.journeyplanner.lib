package eu.trentorise.smartcampus.jp.custom;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;

public class SmartCheckBusAdapter extends ArrayAdapter<SmartLine> {

	private Context context;
	int layoutResourceId;

	public SmartCheckBusAdapter(Context context, int resource, List<SmartLine> busLines) {
		super(context, resource, busLines);
		this.context = context;
		this.layoutResourceId = resource;
	}

	public static class ViewHolder {
		// TextView lineName;
		ImageView icon;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new ViewHolder();
			holder.icon = (ImageView) row.findViewById(R.id.bus_icon);
			row.setTag(holder);
			holder.icon.setImageDrawable(getItem(position).getIcon());

		} else {
			holder = (ViewHolder) row.getTag();
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new ViewHolder();
			holder.icon = (ImageView) row.findViewById(R.id.bus_icon);

			row.setTag(holder);
			holder.icon.setImageDrawable(getItem(position).getIcon());
		}

		return row;
	}
}
