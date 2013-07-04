package eu.trentorise.smartcampus.jp.model;

import android.graphics.Rect;
import android.location.Location;

	public class Square{
		private double mLat;
		private double mLong;
		private double mDiagonal;

		public Square(double mLat, double mLong, double diagonal) {
			super();
			this.mLat = mLat;
			this.mLong = mLong;
			this.mDiagonal = diagonal;

		}

		public Square(double[] location, double diagonal) {
			super();
			this.mLat = location[0];
			this.mLong = location[1];
			this.mDiagonal = diagonal;

		}

		public double[] getLocation() {
			double[] d = { mLat, mLong };
			return d;
		}

		public boolean compareTo(Square another) {
			if(another.mDiagonal>mDiagonal)
				return true;
			if(Math.abs((mLat-another.mLat))>mDiagonal)
				return true;
			if(Math.abs((mLong-another.mLong))>mDiagonal)
				return true;
			return !(Math.abs((mLat-another.mLat))==mDiagonal)&&(Math.abs((mLong-another.mLong))==mDiagonal);
		}


		public void add(Square another) {
			if(another.mDiagonal>mDiagonal)
				mDiagonal=another.mDiagonal;
			else{
				float[] results = new float[1];
				Location.distanceBetween(mLat, mLong, another.mLat, another.mLong, results);
				if(results[0]>mDiagonal)
					mDiagonal=mDiagonal+another.mDiagonal-results[0];
			}
			double[] location=midPoint(mLat, mLong, another.mLat, another.mLong);
			this.mLat = location[0];
			this.mLong = location[1];

		}

		/*
		 * this function use Harvesine formula
		 */
		public static double[] midPoint(double lat1,double lon1,double lat2,double lon2){

			double dLon = Math.toRadians(lon2 - lon1);

			//convert to radians
			lat1 = Math.toRadians(lat1);
			lat2 = Math.toRadians(lat2);
			lon1 = Math.toRadians(lon1);

			double Bx = Math.cos(lat2) * Math.cos(dLon);
			double By = Math.cos(lat2) * Math.sin(dLon);
			double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
			double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

			double[] out={Math.toDegrees(lat3),Math.toDegrees(lon3)};
			return out;
			}
	}