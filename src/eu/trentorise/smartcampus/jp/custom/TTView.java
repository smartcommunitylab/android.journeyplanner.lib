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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Toast;
import eu.trentorise.smartcampus.jp.R;

/**
 * @author raman
 *
 */
public class TTView extends CustomGridView<String> {

	protected Paint mVLinePaint;
	protected Paint mHLinePaint;

	protected Paint mTextPaint;

	public TTView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TTView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TTView(Context context) {
		super(context);
	}

	@Override
	protected void drawCell(Canvas canvas, String item, int row, int col, int x, int y) {
		String text = (String) item;
		int xPos = (int)(x + getColWidth()/2 - mTextPaint.measureText(text)/2);
		int yPos = (int) ((y+getRowHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2)) ; 
		canvas.drawText(text, xPos, yPos, mTextPaint);
	}

	@Override
	protected void handleClick(String object) {
//		Toast.makeText(getContext(), (String)object, Toast.LENGTH_SHORT).show();
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

		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextSize(18);
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
