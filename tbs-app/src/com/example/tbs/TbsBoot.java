package com.example.tbs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Sets the alarms for TbsBroadcast, TbsBroadcastInternet
 * 
 * @author abhi
 */
public class TbsBoot extends BroadcastReceiver {

	private static final String TAG = "TbsBoot";

	@Override
	public void onReceive(Context c, Intent i) {
		Log.d(TAG, "Received broadcast boot");
		// TODO check if sense is logged in
		AlarmUtils.setAlarms(c);
	}
}