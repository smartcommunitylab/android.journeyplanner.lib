package eu.trentorise.smartcampus.jp.model;

import android.graphics.Rect;

	public class Square implements Comparable<Square> {
		public int mLat;
		public int mLong;
		public int mDiagonal;

		public Square(double mLat, double mLong, double diagonal) {
			super();
			this.mLat = (int) mLat;
			this.mLong = (int) mLong;
			this.mDiagonal = (int) diagonal;
		}

		public Square(double[] location, double diagonal) {
			super();
			this.mLat = (int) location[0];
			this.mLong = (int) location[1];
			this.mDiagonal = (int) diagonal;
		}

		public double[] getLocation() {
			double[] d = { mLat, mLong };
			return d;
		}

		@Override
		public int compareTo(Square another) {
			Rect me = new Rect(mLat, mLong, mLat + mDiagonal, mLong + mDiagonal);
			Rect other = new Rect(another.mLat, another.mLong, another.mLat
					+ another.mDiagonal, another.mLong + another.mDiagonal);
			if (me.contains(other)) {
				me = null;
				other = null;
				return 1;
			}
			if (other.contains(me)) {
				me = null;
				other = null;
				return -1;
			}
			me = null;
			other = null;
			return 0;
		}

		public void union(Square square) {
			this.mLat = (square.mLat + this.mLat) / 2;
			this.mLong = (square.mLong + this.mLong) / 2;
			this.mDiagonal += square.mDiagonal;
		}

	}