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

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Toast;

/**
 * @author raman
 * 
 */
public class StopsView extends TTView {

	public StopsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public StopsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public StopsView(Context context) {
		super(context);
	}

	@Override
	protected void drawCell(Canvas canvas, String item, int row, int col, int x, int y) {
		if (canvas != null) {
			String text = (String) item;
			int xPos = (int) (x + 10);
			int yPos = (int) ((y + getRowHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
			canvas.drawText(text, xPos, yPos, mTextPaint);
		}
	}

	@Override
	protected void handleClick(String object) {
		Toast.makeText(getContext(), object, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void init() {
		super.init();
		mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
	}

	@Override
	public void setData(List<String> data) {
		super.setData(data);
		setNumCols(1);
		setNumRows(data == null ? 0 : data.size());
	}

	@Override
	public int getColWidth() {
		return TTHelper.getPixels(getContext(), TTHelper.COL_PLACE_WIDTH);
	}

}
