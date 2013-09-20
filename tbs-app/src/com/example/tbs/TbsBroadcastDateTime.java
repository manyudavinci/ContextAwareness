package com.example.tbs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.Settings.SettingNotFoundException;

public class TbsBroadcastDateTime extends BroadcastReceiver {
	private static int x = 100;
	private static int y = 200;

	@Override
	public void onReceive(Context c, Intent intent) {
		// TODO Auto-generated method stub

		try {
			x = android.provider.Settings.System.getInt(c.getContentResolver(),
					android.provider.Settings.System.AUTO_TIME);

			y = android.provider.Settings.System.getInt(c.getContentResolver(),
					android.provider.Settings.System.AUTO_TIME_ZONE);
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Toast.makeText(c, "y" + y, Toast.LENGTH_LONG).show();

		if (x == 0 || y == 0) {

			NotificationManager notificationManager = (NotificationManager) c
					.getSystemService(Context.NOTIFICATION_SERVICE);

			Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

			intent = new Intent(android.provider.Settings.ACTION_DATE_SETTINGS);
			PendingIntent pIntent = PendingIntent.getActivity(c, 0, intent, 0);

			String message = c.getString(R.string.stat_notify_enable_network_date_timezone);			
						
			Notification noti = new Notification.Builder(c).setContentTitle(message)
					.setSound(sound).setTicker(message)
					.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent)
					.getNotification();

			// Hide the notification after its selected
			noti.flags |= Notification.FLAG_AUTO_CANCEL;

			notificationManager.notify(0, noti);

			Vibrator vi = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
			vi.vibrate(1000);
			
			Intent i = new Intent(c, DisableSense.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra("alert_type", "6");
			c.startActivity(i);

			// Toast.makeText(c, "Disable Mock Location" + x, Toast.LENGTH_LONG).show();

		}

	}

}
