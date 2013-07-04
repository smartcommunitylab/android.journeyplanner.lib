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
package eu.trentorise.smartcampus.jp.custom.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

public class StopsItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private final static int densityX = 5;
	private final static int densityY = 5;

	private List<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private ArrayList<SmartCheckStop> mObjects = new ArrayList<SmartCheckStop>();
	private Set<OverlayItem> mGeneric = new HashSet<OverlayItem>();

	private SparseArray<int[]> item2group = new SparseArray<int[]>();

	private StopObjectMapItemTapListener listener = null;

	private Drawable groupMarker = null;

	private Context mContext = null;

	private MapView mMapView = null;

	List<List<List<OverlayItem>>> grid = new ArrayList<List<List<OverlayItem>>>();
	private Map<String, OverlayItem> layerMap = new HashMap<String, OverlayItem>();

	private boolean isPinch = false;

	// flag specifying that the map is being moved so there is no need to
	// recompute items
	private boolean animating = false;
	// hack parameters: need to compute consecutively the value to see whether
	// the animation is finished
	int lastStep = 0;
	int lastStepCount = 0;

	public StopsItemizedOverlay(Context mContext, MapView mapView) {
		super(boundCenterBottom(mContext.getResources().getDrawable(R.drawable.poi)));
		this.mContext = mContext;
		this.mMapView = mapView;
		populate();
	}

	public void populateAll() {
		populate();
	}

	public void setMapItemTapListener(StopObjectMapItemTapListener listener) {
		this.listener = listener;
	}

	public void addOverlay(SmartCheckStop o) {
		if (o.getLocation() != null) {
			GeoPoint point = new GeoPoint((int) (o.getLocation()[0] * 1E6), (int) (o.getLocation()[1] * 1E6));
			OverlayItem overlayitem = new OverlayItem(point, o.getTitle(), "");

			Drawable drawable = mContext.getResources().getDrawable(R.drawable.marker_poi_mobility);
			drawable.setBounds(-drawable.getIntrinsicWidth() / 2, -drawable.getIntrinsicHeight(),
					drawable.getIntrinsicWidth() / 2, 0);
			overlayitem.setMarker(drawable);
			boolean check = false;
			for (OverlayItem item : mOverlays) {
				if (item.getPoint().getLatitudeE6() == point.getLatitudeE6()
						&& point.getLongitudeE6() == item.getPoint().getLongitudeE6()) {
					check = true;
					break;
				}
			}
			if (!check) {
				mOverlays.add(overlayitem);
				mObjects.add(o);
			}
			// populate();
		}
	}

	public void addAllOverlays(Collection<SmartCheckStop> list) {
		for (SmartCheckStop o : list) {
			addOverlay(o);
		}
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public void addGenericOverlay(OverlayItem overlay, String key) {
		// actually is used only for my position. So clear and add the new one
		// mGeneric.clear();
		// mObjects.add(null);
		// mGeneric.add(overlay);
		// populate();
		// mMapView.invalidate();

		if (layerMap.containsKey(key)) {
			// elimina layer esistente da mOverlay e poi dalla hashmap
			OverlayItem precLayer = layerMap.get(key);
			mOverlays.remove(precLayer);
			mGeneric.remove(precLayer);
			layerMap.remove(key);

		}
		layerMap.put(key, overlay);
		mOverlays.add(overlay);
		mObjects.add(null);
		mGeneric.add(overlay);
		mMapView.invalidate();
		populate();

	}

	public void clearMarkers() {
		mOverlays.clear();
		mObjects.clear();
		animating = false;
		lastStep = 0;
		lastStepCount = 0;
		populate();

	}

	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		int fingers = e.getPointerCount();
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			animating = true;
			isPinch = false; // Touch DOWN, don't know if it's a pinch yet
		}
		if (e.getAction() == MotionEvent.ACTION_MOVE && fingers == 2) {
			isPinch = true; // Two fingers, def a pinch
		}
		if (e.getAction() == MotionEvent.ACTION_UP) {
			animating = false;
		}
		return super.onTouchEvent(e, mapView);
	}

	protected boolean onTap(int index) {
		if (!isPinch) {
			if (listener != null) {
				if (mObjects.size() >= index && mObjects.get(index) != null) {
					if (item2group.get(index) != null) {
						int[] coords = item2group.get(index);
						try {
							List<OverlayItem> list = grid.get(coords[0]).get(coords[1]);
							if (list.size() > 1) {
								if (mMapView.getZoomLevel() == mMapView.getMaxZoomLevel()) {
									List<SmartCheckStop> objects = new ArrayList<SmartCheckStop>(list.size());
									for (OverlayItem item : list) {
										int idx = mOverlays.indexOf(item);
										if (idx > 0)
											objects.add(mObjects.get(idx));
									}
									listener.onStopObjectsTap(objects);
								} else {
//									MapManager.fitMapWithOverlays(list, mMapView);
								}
								return super.onTap(index);
							}
						} catch (Exception e) {
							return super.onTap(index);
						}
					}
					listener.onStopObjectTap(mObjects.get(index));
					return true;
				}
			}
			return super.onTap(index);
		} else
			return false;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (mOverlays == null || mOverlays.isEmpty())
			return;

		// if not animating do not compute
		if (!animating) {
			Projection proj = mapView.getProjection();
			// coordinates of visible part
			GeoPoint lu = proj.fromPixels(0, 0);
			GeoPoint rd = proj.fromPixels(mapView.getWidth(), mapView.getHeight());
			// grid step
			int step = Math.abs(lu.getLongitudeE6() - rd.getLongitudeE6()) / densityX;

			// check if zoom is being animated
			boolean zooming = false;
			// step changes
			if (step != lastStep) {
				if (lastStep > 0)
					zooming = true;
				lastStepCount = 0;
				lastStep = step;
			} else {// if (lastStep > 0) {
				lastStepCount++;
				// step does not change for several calls in a sequence
				if (lastStepCount < 3)
					zooming = true;
			}

			if (!zooming) {
				item2group.clear();
				// 2D array with some configurable, fixed density
				grid.clear();

				for (int i = 0; i <= densityX; i++) {
					ArrayList<List<OverlayItem>> column = new ArrayList<List<OverlayItem>>(densityY + 1);
					for (int j = 0; j <= densityY; j++) {
						column.add(new ArrayList<OverlayItem>());
					}
					grid.add(column);
				}

				// compute leftmost bound of the affected grid:
				// this is the bound of the leftmost grid cell that intersects
				// with the visible part
				int startX = lu.getLongitudeE6() - (lu.getLongitudeE6() % step);
				if (lu.getLongitudeE6() < 0)
					startX -= step;
				// compute bottom bound of the affected grid
				int startY = rd.getLatitudeE6() - (rd.getLatitudeE6() % step);
				if (lu.getLatitudeE6() < 0)
					startY -= step;
				int endX = startX + (densityX + 1) * step;
				int endY = startY + (densityY + 1) * step;

				int idx = 0;
				try {
					for (OverlayItem m : mOverlays) {
						if (!mGeneric.contains(m) && m.getPoint().getLongitudeE6() >= startX
								&& m.getPoint().getLongitudeE6() <= endX && m.getPoint().getLatitudeE6() >= startY
								&& m.getPoint().getLatitudeE6() <= endY) {
							int binX = Math.abs(m.getPoint().getLongitudeE6() - startX) / step;
							int binY = Math.abs(m.getPoint().getLatitudeE6() - startY) / step;

							item2group.put(idx, new int[] { binX, binY });
							grid.get(binX).get(binY).add(m); // just push the
																// reference
						}
						idx++;
					}
				} catch (ConcurrentModificationException ex) {
					Log.e(getClass().getCanonicalName(), ex.toString());
				}

				if (mapView.getZoomLevel() == mapView.getMaxZoomLevel()) {
					for (int i = 0; i < grid.size(); i++) {
						for (int j = 0; j < grid.get(0).size(); j++) {
							List<OverlayItem> curr = grid.get(i).get(j);
							if (curr.size() == 0)
								continue;

							if (i > 0) {
								if (checkDistanceAndMerge(i - 1, j, curr))
									continue;
							}
							if (j > 0) {
								if (checkDistanceAndMerge(i, j - 1, curr))
									continue;
							}
							if (i > 0 && j > 0) {
								if (checkDistanceAndMerge(i - 1, j - 1, curr))
									continue;
							}
						}
					}
				}
			}
		}

		// drawing:

		for (int i = 0; i < grid.size(); i++) {
			for (int j = 0; j < grid.get(i).size(); j++) {
				List<OverlayItem> markerList = grid.get(i).get(j);
				if (markerList.size() > 1) {
					drawGroup(canvas, mapView, markerList, i, j);
				} else {
					// draw single marker
					drawSingle(canvas, mapView, markerList);
				}
			}
		}

		for (OverlayItem m : mGeneric) {
			drawSingleItem(canvas, mapView, m);
		}
	}

	private boolean checkDistanceAndMerge(int i, int j, List<OverlayItem> curr) {
		List<OverlayItem> src = grid.get(i).get(j);
		if (src.size() == 0)
			return false;

		float[] dist = new float[3];
		Location.distanceBetween(src.get(0).getPoint().getLatitudeE6() / 1E6, src.get(0).getPoint().getLongitudeE6() / 1E6,
				curr.get(0).getPoint().getLatitudeE6() / 1E6, curr.get(0).getPoint().getLongitudeE6() / 1E6, dist);
		if (dist[0] < 20) {
			src.addAll(curr);
			curr.clear();
			return true;
		}
		return false;
	}

	private void drawGroup(Canvas canvas, MapView mapView, List<OverlayItem> markerList, int i, int j) {
		OverlayItem item = markerList.get(0);
		GeoPoint point = item.getPoint();
		Point ptScreenCoord = new Point();
		mapView.getProjection().toPixels(point, ptScreenCoord);

		if (groupMarker == null) {
			groupMarker = mContext.getResources().getDrawable(R.drawable.marker_poi_generic);
			groupMarker.setBounds(-groupMarker.getIntrinsicWidth() / 2, -groupMarker.getIntrinsicHeight(),
					groupMarker.getIntrinsicWidth() / 2, 0);
		}

		drawAt(canvas, groupMarker, ptScreenCoord.x, ptScreenCoord.y, true);
		drawAt(canvas, groupMarker, ptScreenCoord.x, ptScreenCoord.y, false);

		Paint paint = new Paint();
		paint.setTextAlign(Paint.Align.CENTER);
		int scaledTextSize = mContext.getResources().getDimensionPixelSize(R.dimen.mapIconTextSize);
		paint.setTextSize(scaledTextSize);
		paint.setAntiAlias(true);
		paint.setARGB(255, 255, 255, 255);
		// show text to the right of the icon
		int scaledSize = mContext.getResources().getDimensionPixelSize(R.dimen.mapIconText);
		canvas.drawText("" + markerList.size(), ptScreenCoord.x, ptScreenCoord.y - scaledSize, paint);	}

	private void drawSingle(Canvas canvas, MapView mapView, List<OverlayItem> markerList) {
		for (OverlayItem item : markerList) {
			drawSingleItem(canvas, mapView, item);
		}
	}

	protected Point drawSingleItem(Canvas canvas, MapView mapView, OverlayItem item) {
		GeoPoint point = item.getPoint();
		Point ptScreenCoord = new Point();
		mapView.getProjection().toPixels(point, ptScreenCoord);

		drawAt(canvas, item.getMarker(0), ptScreenCoord.x, ptScreenCoord.y, true);
		drawAt(canvas, item.getMarker(0), ptScreenCoord.x, ptScreenCoord.y, false);
		return ptScreenCoord;
	}

	public static boolean isWithin(Point p, MapView mapView) {
		return (p.x > 0 & p.x < mapView.getWidth() & p.y > 0 & p.y < mapView.getHeight());
	}

}