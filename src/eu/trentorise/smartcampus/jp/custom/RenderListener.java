package eu.trentorise.smartcampus.jp.custom;

import android.widget.TableRow;

public interface RenderListener {
	public void addToTimetable(TableRow tr);
	public void onDayFinished(boolean result);
}
