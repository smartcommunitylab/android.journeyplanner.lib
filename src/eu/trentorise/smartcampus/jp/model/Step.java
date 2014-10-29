package eu.trentorise.smartcampus.jp.model;

import java.io.Serializable;
import java.util.Map;

import android.text.Spanned;
import android.widget.ImageView;

public class Step implements Serializable {
	private static final long serialVersionUID = 1L;

	private String time;
	private transient Spanned description;
	private transient ImageView image;
	private String alert;
	private Map<String, Object> extra;
	private int legIndex;

	public Step() {
	}

	public Step(String time, Spanned description, ImageView image, String alert) {
		setTime(time);
		setDescription(description);
		setImage(image);
		setAlert(alert);
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Spanned getDescription() {
		return description;
	}

	public void setDescription(Spanned description) {
		this.description = description;
	}

	public ImageView getImage() {
		return image;
	}

	public void setImage(ImageView image) {
		this.image = image;
	}

	public String getAlert() {
		return alert;
	}

	public void setAlert(String alert) {
		this.alert = alert;
	}

	public Map<String, Object> getExtra() {
		return extra;
	}

	public void setExtra(Map<String, Object> extra) {
		this.extra = extra;
	}

	public int getLegIndex() {
		return legIndex;
	}
	public void setLegIndex(int legIndex) {
		this.legIndex = legIndex;
	}
}
