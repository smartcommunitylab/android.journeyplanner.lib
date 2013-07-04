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
import android.util.AttributeSet;

/**
 * @author raman
 *
 */
public class TypesView extends TTView {

	public TypesView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TypesView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TypesView(Context context) {
		super(context);
	}

	@Override
	protected void handleClick(String object) {
	}

	@Override
	public void setData(List<String> data) {
		super.setData(data);
		setNumRows(1);
		setNumCols(data == null ? 0 : data.size());
	}
	
	private String getText(String text) {
		if (text.toLowerCase().startsWith("r"))
			return "R";
		if (text.toLowerCase().startsWith("e"))
			 return  "E";
		if (text.toLowerCase().startsWith("i"))
			return "IC";
		return text;
	}

	@Override
	protected void drawCell(Canvas canvas, String item, int row, int col, int x, int y) {
		super.drawCell(canvas, getText(item), row, col, x, y);
	}

	
}
