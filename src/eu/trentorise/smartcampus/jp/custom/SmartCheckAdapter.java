package eu.trentorise.smartcampus.jp.custom;

import java.util.ArrayList;

import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.R.id;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SmartCheckAdapter extends ArrayAdapter<String> {
	
	private ArrayList<String> listOfOptions;
	private Context context;
	int layoutResourceId;
	
	public SmartCheckAdapter(Context context, int resource,
			ArrayList<String> array) {
		super(context, resource, array);
		this.context=context;
		this.layoutResourceId = resource;
		this.listOfOptions =array;
		
	}

	public static class ViewHolder {
	    TextView optionName;
	   }
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;
	    ViewHolder holder;
	    if (convertView == null) {
	    	LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
	        holder=new ViewHolder();
	        holder.optionName = (TextView)row.findViewById(R.id.smart_option);	     
	        row.setTag(holder);
	    } else {
	        holder=(ViewHolder)row.getTag();
	    }
	    holder.optionName.setText(listOfOptions.get(position));
	    return row;
	}
}
