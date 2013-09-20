package com.example.tbs;

import nl.sense_os.platform.SenseApplication;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmUtils {

	public static SenseApplication mSenseApplication;
	static PendingIntent pi;
	static AlarmManager am;
	final static long ONE_SECOND = 1000;
	final static long TWENTYFIVE_SECONDS = ONE_SECOND * 25;
	final static long ONE_MINUTE = ONE_SECOND * 60;

	public static void setAlarms(Context context) {
		am = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));

		pi = PendingIntent.getBroadcast(context, 11, new Intent("com.example.tbs.GPSSETTINGS"), 0);
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, TWENTYFIVE_SECONDS, pi);

		pi = PendingIntent.getBroadcast(context, 12,
				new Intent("com.example.tbs.INTERNETSETTINGS"), 0);
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, TWENTYFIVE_SECONDS, pi);

		pi = PendingIntent
				.getBroadcast(context, 13, new Intent("com.example.tbs.MOCKLOCATIONS"), 0);
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, ONE_SECOND * 5, pi);

		pi = PendingIntent.getBroadcast(context, 14,
				new Intent("com.example.tbs.services.NEW_PLAN"), 0);
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, ONE_MINUTE, pi);

		pi = PendingIntent.getBroadcast(context, 15, new Intent("com.example.tbs.SIM_CHECK"), 0);
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, ONE_MINUTE, pi);

		pi = PendingIntent.getBroadcast(context, 16, new Intent("com.example.tbs.DATE_TIME"), 0);
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, ONE_SECOND, pi);

		pi = PendingIntent.getBroadcast(context, 17, new Intent("com.example.tbs.ROOT_CHECK"), 0);
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, TWENTYFIVE_SECONDS, pi);
	}

	public static void cancelAlarms(Context context) {
		am = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));

		pi = PendingIntent.getBroadcast(context, 11, new Intent("com.example.tbs.GPSSETTINGS"), 0);
		am.cancel(pi);

		pi = PendingIntent.getBroadcast(context, 12,
				new Intent("com.example.tbs.INTERNETSETTINGS"), 0);
		am.cancel(pi);

		pi = PendingIntent
				.getBroadcast(context, 13, new Intent("com.example.tbs.MOCKLOCATIONS"), 0);
		am.cancel(pi);

		pi = PendingIntent.getBroadcast(context, 14,
				new Intent("com.example.tbs.services.NEW_PLAN"), 0);
		am.cancel(pi);

		pi = PendingIntent.getBroadcast(context, 15, new Intent("com.example.tbs.SIM_CHECK"), 0);
		am.cancel(pi);

		pi = PendingIntent.getBroadcast(context, 16, new Intent("com.example.tbs.DATE_TIME"), 0);
		am.cancel(pi);

		pi = PendingIntent.getBroadcast(context, 17, new Intent("com.example.tbs.ROOT_CHECK"), 0);
		am.cancel(pi);

	}

}
