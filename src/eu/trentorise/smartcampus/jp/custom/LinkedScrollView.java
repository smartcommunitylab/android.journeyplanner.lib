package eu.trentorise.smartcampus.jp.custom;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

public class LinkedScrollView extends ScrollView {
	public boolean cascadeScroll = true;
	public ArrayList<LinkedScrollView> others = new ArrayList<LinkedScrollView>();

	public LinkedScrollView(Context context) {
		super(context);
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
