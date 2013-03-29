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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class GetBitmapFromUrl extends AsyncTask<String, Void, Bitmap> {

	// public static Bitmap getBitmapFromURL(String src) {
	// try {
	// URL url = new URL(src);
	// HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	// connection.setDoInput(true);
	// connection.connect();
	// InputStream input = connection.getInputStream();
	// Bitmap myBitmap = BitmapFactory.decodeStream(input);
	// return myBitmap;
	// } catch (IOException e) {
	// Log.e("Exception", e.getMessage());
	// return null;
	// }
	// }

	@Override
	protected Bitmap doInBackground(String... params) {
		try {
			URL url = new URL(params[0]);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			Log.e("Exception", e.getMessage());
			return null;
		}
	}

}
