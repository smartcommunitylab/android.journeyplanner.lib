package eu.trentorise.smartcampus.jp.custom;

import java.util.ArrayList;

import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.R.id;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SmartCheckDirectionAdapter extends ArrayAdapter<String> {
	
	private ArrayList<String> directions;
	private Context context;
	int layoutResourceId;
	
	public SmartCheckDirectionAdapter(Context context, int resource,
			ArrayList<String> directions) {
		super(context, resource, directions);
		this.context=context;
		this.layoutResourceId = resource;
		this.directions =directions;
		
	}

	public static class ViewHolder {
		TextView direction;
//		ImageView icon;
	   }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;
	    ViewHolder holder;
	    if (convertView == null) {
	    	LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
	        holder=new ViewHolder();
	        holder.direction = (TextView)row.findViewById(R.id.smart_option);	  
	        holder.direction.setText(directions.get(position));
	        row.setTag(holder);

	    } else {
	        holder=(ViewHolder)row.getTag();
	    	LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
	        holder=new ViewHolder();
	        holder.direction = (TextView) row.findViewById(R.id.smart_option);	     
	        holder.direction.setText(directions.get(position));
	        row.setTag(holder);
	    }
	    
	    return row;
	}
}
