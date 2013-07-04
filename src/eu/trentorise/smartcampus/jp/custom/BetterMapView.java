package eu.trentorise.smartcampus.jp.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

import eu.trentorise.smartcampus.jp.custom.map.MapCache;

public class BetterMapView extends MapView {
	public interface OnMapChanged {
		public void onCenterChanged(GeoPoint center);

		public void onZoomChanged(GeoPoint center, double diagonal);
	}

	private OnMapChanged mOnMapChanged;
	private GeoPoint mCenter;
	private double mDiagonal;
	private static MapCache cache;
	
	public BetterMapView(Context mContext, String arg1) {
		super(mContext, arg1);
	}

	public OnMapChanged getOnMapChanged() {
		return mOnMapChanged;
	}



	public void setOnMapChanged(OnMapChanged mOnMapChanged) {
		this.mOnMapChanged = mOnMapChanged;
	}



	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			this.mCenter = getMapCenter();
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (!mCenter.equals(getMapCenter())) {
				mCenter = getMapCenter();
				mOnMapChanged.onCenterChanged(mCenter);
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mCenter == null)
			mCenter = getMapCenter();

		Projection proj = getProjection();
		GeoPoint lu = proj.fromPixels(0, 0);
		GeoPoint rd = proj.fromPixels(getWidth(), getHeight());
		int h = rd.getLongitudeE6() - lu.getLongitudeE6();
		int w = rd.getLatitudeE6() - rd.getLatitudeE6();
		double diagonal = Math.sqrt(Math.pow(w, 2) + Math.pow(h, 2)) / 1e6;
		if (diagonal != mDiagonal) {
			mDiagonal = diagonal;
			mOnMapChanged.onZoomChanged(getMapCenter(), diagonal);
		}

	}

	public double getDiagonalLenght() {
		return mDiagonal;
	}

	public MapCache getCache() {
		if (cache == null)
			cache = new MapCache();
		return cache;
	}

}
