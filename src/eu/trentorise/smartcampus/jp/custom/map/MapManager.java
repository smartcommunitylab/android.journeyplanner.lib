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
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.location.Location;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.custom.BetterMapView;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.helper.ParkingsHelper;
import eu.trentorise.smartcampus.jp.model.LocatedObject;
import eu.trentorise.smartcampus.jp.model.ParkingSerial;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

public class MapManager {

	private static MapView mapView;
	private static BetterMapView betterMapView;

	private static MapCache cache;

	public static int ZOOM_DEFAULT = 15;
	public static GeoPoint DEFAULT_POINT = new GeoPoint((int) (46.0696727540531 * 1E6), (int) (11.1212700605392 * 1E6)); // Trento

	public static void initWithParams() {
		int zoom = JPParamsHelper.getZoomLevelMap();
		if (zoom != 0) {
			ZOOM_DEFAULT = zoom;
		}

		List<Double> centerMap = JPParamsHelper.getCenterMap();
		if (centerMap != null) {
			Double latitute = centerMap.get(0);
			Double longitude = centerMap.get(1);
			DEFAULT_POINT = new GeoPoint((int) (latitute * 1E6), (int) (longitude * 1E6));
		}
	}

	public static MapView getMapView() {
		return mapView;
	}

	public static void setMapView(MapView mapView) {
		MapManager.mapView = mapView;
		MapManager.mapView.setClickable(true);
		MapManager.mapView.setBuiltInZoomControls(true);
	}

	public static BetterMapView getBetterMapView() {
		return betterMapView;
	}

	public static void setBetterMapView(BetterMapView betterMapView) {
		MapManager.betterMapView = betterMapView;
	}

	public static GeoPoint requestMyLocation(Context ctx) {
		return JPHelper.getLocationHelper().getLocation();
	}

	public static double[] computeCenter(List<SmartCheckStop> list) {
		double[] ll = null, rr = null;
		if (list != null) {
			for (SmartCheckStop o : list) {
				if (o.getLocation() == null)
					continue;
				if (ll == null) {
					ll = o.getLocation().clone();
					rr = o.getLocation().clone();
				} else {
					ll[0] = Math.min(ll[0], o.getLocation()[0]);
					ll[1] = Math.max(ll[1], o.getLocation()[1]);

					rr[0] = Math.max(rr[0], o.getLocation()[0]);
					rr[1] = Math.min(rr[1], o.getLocation()[1]);
				}
			}
		}
		if (ll != null) {
			return new double[] { (ll[0] + rr[0]) / 2, (ll[1] + rr[1]) / 2 };
		}
		return null;
	}

	public static void fitMapWithOverlays(List<LocatedObject> list, GoogleMap map) {
		double[] ll = null, rr = null;
		if (list != null) {
			for (LocatedObject o : list) {
				double[] location = o.location();
				if (ll == null) {
					ll = location.clone();
					rr = ll.clone();
				} else {
					ll[0] = Math.min(ll[0], location[0]);
					ll[1] = Math.max(ll[1], location[1]);

					rr[0] = Math.max(rr[0], location[0]);
					rr[1] = Math.min(rr[1], location[1]);
				}
			}
		}
		fit(map, ll, rr, list != null && list.size() > 1);
	}

	private static void fit(GoogleMap map, double[] ll, double[] rr, boolean zoomIn) {
		if (ll != null) {
			LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(ll[0], ll[1])).include(new LatLng(rr[0], rr[1]))
					.build();
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 16));
		}
	}

	public static MapCache getCache() {
		if (cache == null)
			cache = new MapCache();
		return cache;
	}

	/*
	 * CLUSTERING
	 */
	public static class ClusteringHelper {
		private static final String TAG = "MapManager.ClusteringHelper";

		private static final int DENSITY_X = 5;
		private static final int DENSITY_Y = 5;

		public static final String TITLE_CLUSTERED = "clusteredmarker";

		private static List<List<List<LocatedObject>>> grid = new ArrayList<List<List<LocatedObject>>>();
		private static SparseArray<int[]> item2group = new SparseArray<int[]>();

		public synchronized static <T extends LocatedObject> List<MarkerOptions> cluster(Context mContext, GoogleMap map,
				Collection<T> objects) {
			item2group.clear();
			// 2D array with some configurable, fixed density
			grid.clear();

			for (int i = 0; i <= DENSITY_X; i++) {
				ArrayList<List<LocatedObject>> column = new ArrayList<List<LocatedObject>>(DENSITY_Y + 1);
				for (int j = 0; j <= DENSITY_Y; j++) {
					column.add(new ArrayList<LocatedObject>());
				}
				grid.add(column);
			}

			LatLng lu = map.getProjection().getVisibleRegion().farLeft;
			LatLng rd = map.getProjection().getVisibleRegion().nearRight;
			int step = (int) (Math.abs((lu.longitude * 1E6) - (rd.longitude * 1E6)) / DENSITY_X);

			// compute leftmost bound of the affected grid:
			// this is the bound of the leftmost grid cell that intersects
			// with the visible part
			int startX = (int) ((lu.longitude * 1E6) - ((lu.longitude * 1E6) % step));
			if (lu.longitude < 0) {
				startX -= step;
			}
			// compute bottom bound of the affected grid
			int startY = (int) ((rd.latitude * 1E6) - ((rd.latitude * 1E6) % step));
			if (lu.latitude < 0) {
				startY -= step;
			}
			int endX = startX + (DENSITY_X + 1) * step;
			int endY = startY + (DENSITY_Y + 1) * step;

			int idx = 0;
			try {
				for (LocatedObject basicObject : objects) {
					LatLng objLatLng = getLatLngFromBasicObject(basicObject);

					if (objLatLng != null && (objLatLng.longitude * 1E6) >= startX && (objLatLng.longitude * 1E6) <= endX
							&& (objLatLng.latitude * 1E6) >= startY && (objLatLng.latitude * 1E6) <= endY) {
						int binX = (int) (Math.abs((objLatLng.longitude * 1E6) - startX) / step);
						int binY = (int) (Math.abs((objLatLng.latitude * 1E6) - startY) / step);

						item2group.put(idx, new int[] { binX, binY });
						// just push the reference
						grid.get(binX).get(binY).add(basicObject);
					}
					idx++;
				}
			} catch (ConcurrentModificationException ex) {
				Log.e(TAG, ex.toString());
			}

			if (map.getCameraPosition().zoom == map.getMaxZoomLevel()) {
				for (int i = 0; i < grid.size(); i++) {
					for (int j = 0; j < grid.get(0).size(); j++) {
						List<LocatedObject> curr = grid.get(i).get(j);
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

			// generate markers
			List<MarkerOptions> markers = new ArrayList<MarkerOptions>();

			for (int i = 0; i < grid.size(); i++) {
				for (int j = 0; j < grid.get(i).size(); j++) {
					List<LocatedObject> markerList = grid.get(i).get(j);
					if (markerList.size() > 1) {
						markers.add(createGroupMarker(mContext, map, markerList, i, j));
					} else if (markerList.size() == 1) {
						// draw single marker
						markers.add(createSingleMarker(markerList.get(0), i, j));
					}
				}
			}

			return markers;
		}

		public static void render(GoogleMap map, List<MarkerOptions> markers) {
			for (MarkerOptions mo : markers) {
				map.addMarker(mo);
			}
		}

		private static MarkerOptions createSingleMarker(LocatedObject item, int x, int y) {
			LatLng latLng = getLatLngFromBasicObject(item);

			int markerIcon = R.drawable.marker_poi_generic;
			if (item instanceof SmartCheckStop) {
				markerIcon = R.drawable.marker_poi_mobility;
			} else if (item instanceof ParkingSerial) {
				markerIcon = ParkingsHelper.getParkingMarker((ParkingSerial) item);
			}

			MarkerOptions marker = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(markerIcon))
					.title(x + ":" + y);
			return marker;
		}

		private static MarkerOptions createGroupMarker(Context mContext, GoogleMap map, List<LocatedObject> markerList, int x,
				int y) {
			LocatedObject item = markerList.get(0);
			LatLng latLng = getLatLngFromBasicObject(item);

			int markerIcon = R.drawable.marker_poi_generic;
			if (item instanceof ParkingSerial) {
				markerIcon = R.drawable.marker_parking_cluster;
			}

			BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(writeOnMarker(mContext, markerIcon,
					Integer.toString(markerList.size())));
			MarkerOptions marker = new MarkerOptions().position(latLng).icon(bd).title(x + ":" + y);
			return marker;
		}

		private static Bitmap writeOnMarker(Context mContext, int drawableId, String text) {
			float scale = mContext.getResources().getDisplayMetrics().density;

			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888,
					true);

			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setTextAlign(Align.CENTER);
			paint.setTextSize(scale * 14);
			paint.setAntiAlias(true);
			paint.setARGB(255, 255, 255, 255);

			Canvas canvas = new Canvas(bitmap);
			Rect bounds = new Rect();
			paint.getTextBounds(text, 0, text.length(), bounds);
			float x = bitmap.getWidth() / 2;
			float y = bitmap.getHeight() / 2;
			canvas.drawText(text, x, y, paint);

			return bitmap;
		}

		public static List<LocatedObject> getFromGridId(String id) {
			try {
				String[] parsed = id.split(":");
				int x = Integer.parseInt(parsed[0]);
				int y = Integer.parseInt(parsed[1]);

				return grid.get(x).get(y);
			} catch (Exception e) {
				return null;
			}
		}

		private static boolean checkDistanceAndMerge(int i, int j, List<LocatedObject> curr) {
			List<LocatedObject> src = grid.get(i).get(j);
			if (src.size() == 0) {
				return false;
			}

			LatLng srcLatLng = getLatLngFromBasicObject(src.get(0));
			LatLng currLatLng = getLatLngFromBasicObject(curr.get(0));

			if (srcLatLng != null && currLatLng != null) {
				float[] dist = new float[3];

				Location.distanceBetween(srcLatLng.latitude, srcLatLng.longitude, currLatLng.latitude, currLatLng.longitude,
						dist);

				if (dist[0] < 20) {
					src.addAll(curr);
					curr.clear();
					return true;
				}
			}
			return false;
		}

		private static LatLng getLatLngFromBasicObject(LocatedObject object) {
			LatLng latLng = null;
			latLng = new LatLng(object.location()[0], object.location()[1]);
			return latLng;
		}

	}

}
