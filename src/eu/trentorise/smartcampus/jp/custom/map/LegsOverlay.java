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
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

import eu.trentorise.smartcampus.jp.R;

public class LegsOverlay extends com.google.android.maps.Overlay {
	private Context ctx;
	private List<List<GeoPoint>> legsPoints = new ArrayList<List<GeoPoint>>();
	private int index = 0;

	public LegsOverlay(List<String> polylines, int index, Context ctx) {
		this.ctx = ctx;
		setPath(polylines, index);
	}

	public void setPath(List<String> polylines, int index) {
		for (String polyline : polylines) {
			List<GeoPoint> legPoints = decodePoly(polyline);
			legsPoints.add(legPoints);
		}

		this.index = index;
	}

	public void resetPath() {
		this.legsPoints = new ArrayList<List<GeoPoint>>();
		this.index = 0;
	}

	@Override
	public boolean draw(Canvas canvas, MapView mv, boolean shadow, long when) {
		super.draw(canvas, mv, shadow);

		for (int i = 0; i < legsPoints.size(); i++) {
			// default
			int color = ctx.getResources().getColor(R.color.path);
			if (i < index) {
				// past
				color = ctx.getResources().getColor(R.color.path_done);
			} else if (i == index) {
				// actual
				color = ctx.getResources().getColor(R.color.path_actual);
			}
			
			List<GeoPoint> legPoints = legsPoints.get(i);
			if (i!=index)
				drawPath(legPoints, color, mv, canvas);

			// markers
			// start
			if (i == 0) {
				Projection p = mv.getProjection();
				Point loc = p.toPixels(legPoints.get(0), null);
				Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_start).copy(
						Bitmap.Config.ARGB_8888, true);
				canvas.drawBitmap(bitmap, loc.x - (bitmap.getWidth() / 2), loc.y - bitmap.getHeight(), null);
			} 

			// stop
			if (i == (legsPoints.size() - 1)) {
				Projection p = mv.getProjection();
				Point loc = p.toPixels(legPoints.get(legPoints.size() - 1), null);
				Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_stop).copy(
						Bitmap.Config.ARGB_8888, true);
				canvas.drawBitmap(bitmap, loc.x - (bitmap.getWidth() / 2), loc.y - bitmap.getHeight(), null);
			}

		}
		if (index==-1)//show start leg
			drawPath(legsPoints.get(index+1), ctx.getResources().getColor(R.color.path_actual), mv, canvas);
		else if (index == legsPoints.size())//show end leg
			drawPath(legsPoints.get(legsPoints.size()-1), ctx.getResources().getColor(R.color.path_actual), mv, canvas);
		else drawPath(legsPoints.get(index), ctx.getResources().getColor(R.color.path_actual), mv, canvas);

		return true;
	}

	public void drawPath(List<GeoPoint> points, int color, MapView mv, Canvas canvas) {
		int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
		Paint paint = new Paint();

		paint.setColor(color);

		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(6); // TODO: use Config
		for (int i = 0; i < points.size(); i++) {
			Point point = new Point();
			mv.getProjection().toPixels(points.get(i), point);
			x2 = point.x;
			y2 = point.y;
			if (i > 0) {
				canvas.drawLine(x1, y1, x2, y2, paint);
			}
			x1 = x2;
			y1 = y2;
		}
	}

	private List<GeoPoint> decodePoly(String encoded) {
		List<GeoPoint> poly = new ArrayList<GeoPoint>();

		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			if (index >= len) {
				break;
			}
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) * 1E6));
			poly.add(p);
		}

		return poly;
	}

	public void adaptMap(MapController mMapController) {
		int minLatitude = (int) (+81 * 1E6);
		int maxLatitude = (int) (-81 * 1E6);
		int minLongitude = (int) (+181 * 1E6);
		int maxLongitude = (int) (-181 * 1E6);

		List<GeoPoint> points = new ArrayList<GeoPoint>();

		// zoom on active leg
		if (index >= 0 && index < legsPoints.size()) {
			points.addAll(legsPoints.get(index));
		} else if (index == -1) {
			// zoom on start
			points.add(legsPoints.get(0).get(0));
		} else if (index == legsPoints.size()) {
			// zoom on stop
			List<GeoPoint> legPoints = legsPoints.get(legsPoints.size() - 1);
			points.add(legPoints.get(legPoints.size() - 1));
		} else {
			// zoom on all itinerary
			for (List<GeoPoint> list : legsPoints) {
				points.addAll(list);
			}
		}

		for (GeoPoint point : points) {
			int latitude = point.getLatitudeE6();
			int longitude = point.getLongitudeE6();

			if (latitude != 0 && longitude != 0) {
				minLatitude = (minLatitude > latitude) ? latitude : minLatitude;
				maxLatitude = (maxLatitude < latitude) ? latitude : maxLatitude;
				minLongitude = (minLongitude > longitude) ? longitude : minLongitude;
				maxLongitude = (maxLongitude < longitude) ? longitude : maxLongitude;
			}
		}

		if (points.size() == 1) {
			mMapController.setZoom(18);
		} else {
			mMapController.zoomToSpan((maxLatitude - minLatitude), (maxLongitude - minLongitude));
		}
		mMapController.animateTo(new GeoPoint((maxLatitude + minLatitude) / 2, (maxLongitude + minLongitude) / 2));
	}
}
