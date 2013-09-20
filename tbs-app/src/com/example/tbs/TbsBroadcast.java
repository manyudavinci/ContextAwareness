package com.example.tbs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class TbsBroadcast extends BroadcastReceiver {

	private static final String TAG = "TbsBroadcast";

	public void onReceive(Context c, Intent i) {
		Log.d(TAG, "Received broadcast");
		// "android.intent.action.BOOT_COMPLETED".equals
		// (i.getAction())||("com.example.tbs.GPSSETTINGS".equals(i.getAction()))

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

			NotificationManager notificationManager = (NotificationManager) c
					.getSystemService(Context.NOTIFICATION_SERVICE);

			Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

			LocationManager locationManager = (LocationManager) c
					.getSystemService(Context.LOCATION_SERVICE);

			if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

				Vibrator vi = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
				vi.vibrate(1000);

				Intent intent = new Intent(
						android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				PendingIntent pIntent = PendingIntent.getActivity(c, 0, intent, 0);

				Notification noti = new Notification.Builder(c).setContentTitle(c.getString(R.string.stat_notify_activate_gps))
						.setSound(sound).setTicker(c.getString(R.string.stat_notify_activate_gps)
)
						.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent)
						.getNotification();

				// Hide the notification after its selected
				noti.flags |= Notification.FLAG_AUTO_CANCEL;

				notificationManager.notify(0, noti);

			}

			else if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				Vibrator vi = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
				vi.vibrate(1000);

				Intent intent = new Intent(
						android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				PendingIntent pIntent = PendingIntent.getActivity(c, 0, intent, 0);

				Notification noti = new Notification.Builder(c)
						.setContentTitle(c.getString(R.string.stat_notify_activate_network_location)).setSound(sound)
						.setTicker(c.getString(R.string.stat_notify_activate_network_location)).setSmallIcon(R.drawable.ic_launcher)
						.setContentIntent(pIntent).getNotification();

				// Hide the notification after its selected
				noti.flags |= Notification.FLAG_AUTO_CANCEL;

				notificationManager.notify(0, noti);

			}

		} else {

			Toast.makeText(c, c.getString(R.string.stat_notify_activate_location_access), Toast.LENGTH_LONG).show();
		}

	}
}