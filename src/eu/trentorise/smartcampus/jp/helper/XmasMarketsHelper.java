package eu.trentorise.smartcampus.jp.helper;

import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import eu.trentorise.smartcampus.jp.R;

public class XmasMarketsHelper {

	// Start: 20/11/2014 00:00:00
	private static final long XMAS_MARKETS_START = 1447974000000L;
	// End: 07/01/2015 00:00:00
	private static final long XMAS_MARKETS_END = 1452121200000L;

	// Mercatini Giordano: 45.889568,11.043297
	// Mercatini Raman: 45.890068,11.043747
	private static final double[] xmasMarketsPosition = { 45.890068, 11.043747 };
	private static final double[] xmasMarketsParkingPosition = { 45.900709, 11.036345 };

	public static boolean isXmasMarketsTime() {
		Calendar nowCalendar = Calendar.getInstance(Locale.getDefault());
		long now = nowCalendar.getTimeInMillis();
		return (now >= XMAS_MARKETS_START && now <= XMAS_MARKETS_END);
	}

	public static Address getXmasMarketAddress(Context ctx) {
		Address address = new Address(Locale.getDefault());
		address.setLatitude(xmasMarketsPosition[0]);
		address.setLongitude(xmasMarketsPosition[1]);
		address.setAddressLine(0, ctx.getString(R.string.xmasmarkets_xmasmarkets));
		return address;
	}

	public static Address getXmasMarketParkingAddress(Context ctx) {
		Address address = new Address(Locale.getDefault());
		address.setLatitude(xmasMarketsParkingPosition[0]);
		address.setLongitude(xmasMarketsParkingPosition[1]);
		address.setAddressLine(0, ctx.getString(R.string.xmasmarkets_querciaparking));
		return address;
	}

}
