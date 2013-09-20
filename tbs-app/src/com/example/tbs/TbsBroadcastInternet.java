package com.example.tbs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

 public class TbsBroadcastInternet extends BroadcastReceiver{

	private static final String TAG = "TbsBroadcastInternet";

	public void onReceive(Context c, Intent i) {
		Log.d(TAG, "Received broadcast");
		
		ConnectivityManager cm = (ConnectivityManager) c
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);

		if(false==wifi.isConnected()){
			
			if (mobile.isFailover()) {
				notifyUser(c, c.getString(R.string.stat_notify_network_title), c.getString(R.string.stat_notify_wireless_3G_text));
			}
			if(false==mobile.isConnected()){
				notifyUser(c, c.getString(R.string.stat_notify_network_title), c.getString(R.string.stat_notify_wireless_3G_text));
			}
			if (false == wifiManager.isWifiEnabled()) {
				notifyUser(c, c.getString(R.string.stat_notify_network_title), c.getString(R.string.stat_notify_wireless_wifi_text));
			}	
		}
	}
	
 void notifyUser(Context c, String message, String tickerMessage) {

	 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		 NotificationManager notificationManager = (NotificationManager) 
				 c.getSystemService(Context.NOTIFICATION_SERVICE);

		 Uri sound =RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		 Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
		 PendingIntent pIntent = PendingIntent.getActivity(c, 0, intent, 0);

		 Notification noti = new Notification.Builder(c)
		 .setContentTitle(message).setSound(sound)
		 .setTicker(tickerMessage).setSmallIcon(R.drawable.ic_launcher)
		 .setContentIntent(pIntent).getNotification();

		 // Hide the notification after its selected
		 noti.flags |= Notification.FLAG_AUTO_CANCEL;

		 notificationManager.notify(0, noti);
	 } else {
		 Toast.makeText(c, message, Toast.LENGTH_LONG).show();
	 }
	 Vibrator vi = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
	 vi.vibrate(1000);
	}

}