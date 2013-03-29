package eu.trentorise.smartcampus.jp.custom.data;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class SmartLine implements Parcelable {
	/**
	 *  a single line with more routes associated
	 */
	private Drawable icon;
	private String line;
	private int color;
	private List<String> routesID;
	private List<String> routesShorts;
	private List<String> routesLong;

	public SmartLine(Drawable icon, String line, int color,
			List<String> routesShorts, List<String> routesLong,
			List<String> routeID) {
		super();
		this.icon = icon;
		this.line = line;
		this.color = color;
		this.routesID = routeID;
		this.routesShorts = routesShorts;
		this.routesLong = routesLong;
	}

	public List<String> getRouteID() {
		return routesID;
	}

	public void setRouteID(List<String> routeID) {
		this.routesID = routeID;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public List<String> getRoutesShorts() {
		return routesShorts;
	}

	public void setRoutesShorts(List<String> routesShorts) {
		this.routesShorts = routesShorts;
	}

	public List<String> getRoutesLong() {
		return routesLong;
	}

	public void setRoutesLong(List<String> routesLong) {
		this.routesLong = routesLong;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

	}

}
