/**
 *    Copyright 2012-2013 Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.trentorise.smartcampus.jp.custom;

import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.jp.R;

/**
 * @author raman
 *
 */
public class DelaysView extends CustomGridView<Map<String,String>> {

	protected Paint mVLinePaint;
	protected Paint mHLinePaint;

	protected Paint mUTextPaint;
	protected Paint mSTextPaint;

	public DelaysView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public DelaysView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DelaysView(Context context) {
		super(context);
	}

	@Override
	protected Paint getVLinePaint() {
		return mVLinePaint;
	}

	@Override
	protected Paint getHLinePaint() {
		return mHLinePaint;
	}

	@Override
	protected void init() {
		mVLinePaint = new Paint(0);
		mVLinePaint.setColor(getResources().getColor(android.R.color.black));
		mVLinePaint.setStrokeWidth(2);

		mHLinePaint = new Paint(0);
		mHLinePaint.setColor(getResources().getColor(R.color.sc_light_gray));
		mHLinePaint.setStrokeWidth(2);

		mUTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mUTextPaint.setTextSize(18);
		mUTextPaint.setColor(getContext().getResources().getColor(R.color.blue));

		mSTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mSTextPaint.setTextSize(18);
		mSTextPaint.setColor(getContext().getResources().getColor(R.color.red));
	}

	@Override
	protected void handleClick(Map<String,String> item) {
		final Map<CreatorType, String> delaysCreatorTypesMap = new HashMap<CreatorType, String>();
		for (Entry<String, String> delay : item.entrySet()) {
			if (!delay.getValue().equalsIgnoreCase("0")) {
				CreatorType ct = CreatorType.getAlertType(delay.getKey());
				delaysCreatorTypesMap.put(ct, delay.getValue());
			}
		}	
		if (delaysCreatorTypesMap.size() > 0) {
			DelaysDialogFragment delaysDialog = new DelaysDialogFragment();
			Bundle args = new Bundle();
			args.putSerializable(DelaysDialogFragment.ARG_DELAYS, (Serializable) delaysCreatorTypesMap);
			delaysDialog.setArguments(args);
			delaysDialog.show(((SherlockFragmentActivity)getContext()).getSupportFragmentManager(), "delaysdialog");
		}
	}

	@Override
	protected void drawCell(Canvas canvas, Map<String,String> item, int row, int col, int x, int y) {
		Map<CreatorType,String> map = new HashMap<CreatorType, String>(2);
		for (Iterator<Entry<String,String>> iterator = item.entrySet().iterator(); iterator.hasNext();) {
			Entry<String,String> delay = iterator.next();
			if (!delay.getValue().equalsIgnoreCase("0")) {
				CreatorType ct = CreatorType.getAlertType(delay.getKey());
				map.put(ct, delay.getValue());
			}
		}
		int yPos = (int) ((y+getRowHeight() / 2) - ((mSTextPaint.descent() + mSTextPaint.ascent()) / 2)) ; 
		if (map.size() == 2) {
			int xPos = (int)(x + getColWidth()/4 - mSTextPaint.measureText(map.get(CreatorType.SERVICE))/2);
			canvas.drawText(map.get(CreatorType.SERVICE) + "'", xPos, yPos, mSTextPaint);
			xPos = (int)(x + 3*getColWidth()/4 - mUTextPaint.measureText(map.get(CreatorType.USER))/2);
			canvas.drawText(map.get(CreatorType.USER) + "'", xPos, yPos, mUTextPaint);
		} else if (map.size() == 1) {
			Paint p = map.containsKey(CreatorType.SERVICE) ? mSTextPaint : mUTextPaint;
			String text = map.values().iterator().next();
			int xPos = (int)(x + getColWidth()/2 - mUTextPaint.measureText(text)/2);
			canvas.drawText(text + "'", xPos, yPos, p);
		} 
	}


	@Override
	public void setData(List<Map<String, String>> data) {
		super.setData(data);
		setNumRows(1);
		setNumCols(data == null ? 0 : data.size());
	}
	
	@Override
	public int getRowHeight() {
		return TTHelper.rowHeight(getContext());
	}

	@Override
	public int getColWidth() {
		return TTHelper.colWidth(getContext());
	}	
}
