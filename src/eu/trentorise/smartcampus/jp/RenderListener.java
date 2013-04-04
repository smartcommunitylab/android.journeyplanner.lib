package eu.trentorise.smartcampus.jp;

import android.widget.TableRow;

public interface RenderListener {
	public void addToTimetable(TableRow tr);
	public void onDayFinished(boolean result);
}
