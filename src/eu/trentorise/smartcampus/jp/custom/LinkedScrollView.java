package eu.trentorise.smartcampus.jp.custom;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class LinkedScrollView extends ScrollView {
	public boolean cascadeScroll = true;
	public ArrayList<LinkedScrollView> others = new ArrayList<LinkedScrollView>();

	public LinkedScrollView(Context context) {
		super(context);
	}
	
	public LinkedScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LinkedScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		if (cascadeScroll) {
			for (int i = 0; i < others.size(); i++) {
				others.get(i).cascadeScroll = false;
				others.get(i).scrollTo(l, t);
				others.get(i).cascadeScroll = true;
			}
		}
		super.onScrollChanged(l, t, oldl, oldt);
	}
	
}
