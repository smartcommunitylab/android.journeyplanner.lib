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
package eu.trentorise.smartcampus.jp.helper;

import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.RType;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import eu.trentorise.smartcampus.jp.Config;
import eu.trentorise.smartcampus.jp.R;

public class Utils {

	public static ImageView getImageByTType(Context ctx, TType tType) {
		ImageView imgv = new ImageView(ctx);

		switch (tType) {
		case BICYCLE:
			imgv.setImageResource(R.drawable.ic_mt_bicycle);
			break;
		case CAR:
			imgv.setImageResource(R.drawable.ic_mt_car);
			break;
		case BUS:
			imgv.setImageResource(R.drawable.ic_mt_bus);
			break;
		case WALK:
			imgv.setImageResource(R.drawable.ic_mt_foot);
			break;
		case TRAIN:
			imgv.setImageResource(R.drawable.ic_mt_train);
			break;
		case TRANSIT:
			imgv.setImageResource(R.drawable.ic_mt_bus);
			break;
		default:
		}

		return imgv;
	}

	public static ImageView getImageByLine(Context ctx, String line) {
		ImageView imgv = new ImageView(ctx);
		Bitmap img = writeOnBitmap(ctx, R.drawable.ic_mt_bus, line, 12);
		imgv.setImageBitmap(img);
		// colorizeLineDrawable(ctx, line, imgv);
		return imgv;
	}

	public static ImageView getImageForParkingStation(Context ctx, String price) {
		ImageView imgv = new ImageView(ctx);
		Bitmap img = writeOnBitmap(ctx, R.drawable.ic_mt_parking, price, 12);
		imgv.setImageBitmap(img);
		return imgv;
	}

	// private static void colorizeLineDrawable(Context ctx, String line,
	// ImageView imgv) {
	// TypedArray colors =
	// ctx.getResources().obtainTypedArray(R.array.smart_check_12_colors);
	// TypedArray lines =
	// ctx.getResources().obtainTypedArray(R.array.smart_check_12_numbers);
	// for (int index = 0; index < lines.length(); index++) {
	// if (line.equals(lines.getString(index).replace("0", ""))) {
	// int c = colors.getColor(index, 0xFFF);
	// imgv.setColorFilter(c);
	// }
	// }
	// lines.recycle();
	// colors.recycle();
	// }

	private static Bitmap writeOnBitmap(Context mContext, int drawableId, String text, Integer size) {
		float scale = mContext.getResources().getDisplayMetrics().density;
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);

		if (text != null) {
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setTextAlign(Align.CENTER);
			paint.setTextSize(scale * (size != null ? size : 12));
			paint.setAntiAlias(true);
			paint.setARGB(255, 0, 0, 0);

			Canvas canvas = new Canvas(bitmap);
			Rect bounds = new Rect();
			paint.getTextBounds(text, 0, text.length(), bounds);
			float x = bitmap.getWidth() / 2;
			float y = bitmap.getHeight() / 2 + Utils.convertDpToPixel(5f, mContext);
			canvas.drawText(text, x, y, paint);
		}

		return bitmap;
	}

	public static TextView getTextViewByTType(Context ctx, TType tType) {
		TextView imgv = new TextView(ctx);
		Resources r = ctx.getResources();
		switch (tType) {
		case BICYCLE:
			imgv.setCompoundDrawables(null, r.getDrawable(R.drawable.ic_mt_bicycle), null, null);
			break;
		case CAR:
			imgv.setCompoundDrawables(null, r.getDrawable(R.drawable.ic_mt_car), null, null);
			break;
		case BUS:
			imgv.setCompoundDrawables(null, r.getDrawable(R.drawable.ic_mt_bus), null, null);
			break;
		case WALK:
			imgv.setCompoundDrawables(null, r.getDrawable(R.drawable.ic_mt_foot), null, null);
			break;
		case TRAIN:
			imgv.setCompoundDrawables(null, r.getDrawable(R.drawable.ic_mt_train), null, null);
			break;
		case TRANSIT:
			imgv.setCompoundDrawables(null, r.getDrawable(R.drawable.ic_mt_bus), null, null);
			break;
		default:
		}

		return imgv;
	}

	public static ImageView getImageByAgencyId(Context ctx, int agencyId) {
		ImageView imgv = new ImageView(ctx);
		String agencyIdString = Integer.toString(agencyId);

		if (RoutesHelper.AGENCYIDS_BUSES.contains(agencyIdString)) {
			imgv.setImageResource(R.drawable.ic_mt_bus);
		} else if (RoutesHelper.AGENCYIDS_TRAINS.contains(agencyIdString)) {
			imgv.setImageResource(R.drawable.ic_mt_train);
		}

		return imgv;
	}

	public static String getTTypeUIString(Context ctx, TType tType) {
		String uis = null;

		switch (tType) {
		case TRANSIT:
			uis = ctx.getString(R.string.ttype_transit);
			break;
		case CAR:
			uis = ctx.getString(R.string.ttype_car);
			break;
		case BICYCLE:
			uis = ctx.getString(R.string.ttype_bicycle);
			break;
		// case SHAREDBIKE:
		// uis = ctx.getString(R.string.ttype_sharedbike);
		// break;
		case SHAREDBIKE_WITHOUT_STATION:
			uis = ctx.getString(R.string.ttype_sharedbike_wo_station);
			break;
		case CARWITHPARKING:
			uis = ctx.getString(R.string.ttype_car_w_parking);
			break;
		// case SHAREDCAR:
		// uis = ctx.getString(R.string.ttype_sharedcar);
		// break;
		case SHAREDCAR_WITHOUT_STATION:
			uis = ctx.getString(R.string.ttype_sharedcar_wo_station);
			break;
		case BUS:
			uis = ctx.getString(R.string.ttype_bus);
			break;
		case TRAIN:
			uis = ctx.getString(R.string.ttype_train);
			break;
		case WALK:
			uis = ctx.getString(R.string.ttype_walk);
			break;
		case GONDOLA:
			uis = ctx.getString(R.string.ttype_gondola);
			break;
		case SHUTTLE:
			uis = ctx.getString(R.string.ttype_shuttle);
			break;
		default:
			break;
		}

		return uis;
	}

	public static String getRTypeUIString(Context ctx, RType rType) {
		String uis = null;

		switch (rType) {
		case fastest:
			uis = ctx.getString(R.string.rtype_fastest);
			break;
		case greenest:
			uis = ctx.getString(R.string.rtype_greenest);
			break;
		case healthy:
			uis = ctx.getString(R.string.rtype_healthy);
			break;
		case leastChanges:
			uis = ctx.getString(R.string.rtype_least_changes);
			break;
		case leastWalking:
			uis = ctx.getString(R.string.rtype_least_walking);
			break;
		case safest:
			uis = ctx.getString(R.string.rtype_safest);
			break;
		}

		return uis;
	}

	public static Date sjDateString2date(String s) {
		SimpleDateFormat df = Config.FORMAT_DATE_SMARTPLANNER;
		try {
			return df.parse(s);
		} catch (ParseException e) {
			return null;
		}
	}

	public static Date sjTimeString2date(String s) {
		SimpleDateFormat df = Config.FORMAT_TIME_SMARTPLANNER;
		try {
			return df.parse(s);
		} catch (ParseException e) {
			return null;
		}
	}

	public static String millis2time(long millis) {
		SimpleDateFormat df = Config.FORMAT_TIME_UI;
		Date date = new Date(millis);
		return df.format(date);
	}

	public static String uiTime2ServerTime(CharSequence timeStr) {
		try {
			Date time = timeStr != null ? Config.FORMAT_TIME_UI.parse(timeStr.toString()) : new Date();
			return Config.FORMAT_TIME_SMARTPLANNER.format(time);
		} catch (ParseException e) {
			return Config.FORMAT_TIME_SMARTPLANNER.format(new Date());
		}
	}

	public static String serverTime2UITime(CharSequence timeStr) {
		try {
			Date time = timeStr != null ? Config.FORMAT_TIME_SMARTPLANNER.parse(timeStr.toString()) : new Date();
			return Config.FORMAT_TIME_UI.format(time);
		} catch (ParseException e) {
			return Config.FORMAT_TIME_UI.format(new Date());
		}
	}

	public static String uiDate2ServerDate(CharSequence dateStr) {
		try {
			Date date = dateStr != null ? Config.FORMAT_DATE_UI.parse(dateStr.toString()) : new Date();
			return Config.FORMAT_DATE_SMARTPLANNER.format(date);
		} catch (ParseException e) {
			return Config.FORMAT_DATE_SMARTPLANNER.format(new Date());
		}
	}

	public static String serverDate2UIDate(CharSequence dateStr) {
		try {
			Date date = dateStr != null ? Config.FORMAT_DATE_SMARTPLANNER.parse(dateStr.toString()) : new Date();
			return Config.FORMAT_DATE_UI.format(date);
		} catch (ParseException e) {
			return Config.FORMAT_DATE_UI.format(new Date());
		}
	}

	public static boolean validFromDateTime(Date fromDate, Date fromTime) {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		// minutes in the past span
		now.add(Calendar.MINUTE, Config.PAST_MINUTES_SPAN);

		Calendar time = Calendar.getInstance();
		time.setTime(fromTime);
		Calendar from = Calendar.getInstance();
		from.setTime(fromDate);
		from.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
		from.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
		from.set(Calendar.SECOND, 0);
		from.set(Calendar.MILLISECOND, 0);

		return from.compareTo(now) < 0 ? false : true;
	}

	public static boolean validFromDateTimeToDateTime(Date fromDate, Date fromTime, Date toDate, Date toTime) {
		Calendar fromTimeCal = Calendar.getInstance();
		fromTimeCal.setTime(fromTime);
		Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(fromDate);
		fromCal.set(Calendar.HOUR_OF_DAY, fromTimeCal.get(Calendar.HOUR_OF_DAY));
		fromCal.set(Calendar.MINUTE, fromTimeCal.get(Calendar.MINUTE));
		fromCal.set(Calendar.SECOND, 0);
		fromCal.set(Calendar.MILLISECOND, 0);

		Calendar toTimeCal = Calendar.getInstance();
		toTimeCal.setTime(toTime);
		Calendar toCal = Calendar.getInstance();
		toCal.setTime(toDate);
		toCal.set(Calendar.HOUR_OF_DAY, toTimeCal.get(Calendar.HOUR_OF_DAY));
		toCal.set(Calendar.MINUTE, toTimeCal.get(Calendar.MINUTE));
		toCal.set(Calendar.SECOND, 0);
		toCal.set(Calendar.MILLISECOND, 0);

		return fromCal.before(toCal);
	}

	public static Comparator<Route> getRouteComparator() {
		Comparator<Route> comparator = new Comparator<Route>() {
			public int compare(Route o1, Route o2) {
				String s1 = o1.getRouteShortName();
				String s2 = o2.getRouteShortName();

				int thisMarker = 0;
				int thatMarker = 0;
				int s1Length = s1.length();
				int s2Length = s2.length();

				while (thisMarker < s1Length && thatMarker < s2Length) {
					String thisChunk = getChunk(s1, s1Length, thisMarker);
					thisMarker += thisChunk.length();

					String thatChunk = getChunk(s2, s2Length, thatMarker);
					thatMarker += thatChunk.length();

					// If both chunks contain numeric characters, sort them
					// numerically
					int result = 0;
					if (Character.isDigit(thisChunk.charAt(0)) && Character.isDigit(thatChunk.charAt(0))) {
						// Simple chunk comparison by length.
						int thisChunkLength = thisChunk.length();
						result = thisChunkLength - thatChunk.length();
						// If equal, the first different number counts
						if (result == 0) {
							for (int i = 0; i < thisChunkLength; i++) {
								result = thisChunk.charAt(i) - thatChunk.charAt(i);
								if (result != 0) {
									return result;
								}
							}
						}
					} else {
						result = thisChunk.compareTo(thatChunk);
					}

					if (result != 0)
						return result;
				}

				return s1Length - s2Length;
			}
		};

		return comparator;
	}

	private static String getChunk(String s, int slength, int marker) {
		StringBuilder chunk = new StringBuilder();
		char c = s.charAt(marker);
		chunk.append(c);
		marker++;
		if (Character.isDigit(c)) {
			while (marker < slength) {
				c = s.charAt(marker);
				if (!Character.isDigit(c))
					break;
				chunk.append(c);
				marker++;
			}
		} else {
			while (marker < slength) {
				c = s.charAt(marker);
				if (Character.isDigit(c))
					break;
				chunk.append(c);
				marker++;
			}
		}
		return chunk.toString();
	}

	public static boolean containsAlerts(Leg leg) {
		return (leg.getAlertAccidentList() != null && !leg.getAlertAccidentList().isEmpty())
				|| (leg.getAlertDelayList() != null && !leg.getAlertDelayList().isEmpty())
				|| (leg.getAlertParkingList() != null && !leg.getAlertParkingList().isEmpty())
				|| (leg.getAlertRoadList() != null && !leg.getAlertRoadList().isEmpty())
				|| (leg.getAlertStrikeList() != null && !leg.getAlertStrikeList().isEmpty());
	}

	/**
	 * This method converts dp unit to equivalent pixels, depending on device
	 * density.
	 * 
	 * @param dp
	 *            A value in dp (density independent pixels) unit. Which we need
	 *            to convert into pixels
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on
	 *         device density
	 */
	public static int convertDpToPixel(float dp, Context context) {
		// Resources resources = context.getResources();
		// DisplayMetrics metrics = resources.getDisplayMetrics();
		// float px = dp * (metrics.densityDpi / 160f);
		// return px;
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	/**
	 * This method converts device specific pixels to density independent
	 * pixels.
	 * 
	 * @param px
	 *            A value in px (pixels) unit. Which we need to convert into db
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / (metrics.densityDpi / 160f);
		return dp;
	}
}
