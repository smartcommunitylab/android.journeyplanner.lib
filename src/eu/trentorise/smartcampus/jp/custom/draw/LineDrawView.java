/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.jp.custom.draw;

import eu.trentorise.smartcampus.jp.helper.Utils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class LineDrawView extends View {

	Paint paint = new Paint();

	public LineDrawView(Context context) {
		super(context);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(Utils.convertDpToPixel(2, context));
	}

	@Override
	public void onDraw(Canvas canvas) {
		int w = canvas.getWidth();
		int h = canvas.getHeight();

		canvas.drawLine(0, h/2, w, h/2, paint);
		// canvas.drawCircle(50, 0, 5, paint);
		// canvas.drawCircle(50, 50, 5, paint);
	}

}
