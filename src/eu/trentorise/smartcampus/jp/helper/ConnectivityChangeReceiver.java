package eu.trentorise.smartcampus.jp.helper;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectivityChangeReceiver extends BroadcastReceiver
{
  @Override
  public void onReceive( Context context, Intent intent )
  {
    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
    if (activeNetInfo != null && activeNetInfo.isConnected()) {
    	Log.i("ConnectivityChangeReceiver", "Connected, checking DB");
        try {
    		RoutesDBHelper.init(context.getApplicationContext());
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
  }
}