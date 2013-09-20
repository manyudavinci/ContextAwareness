package com.example.tbs.services;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PlanBroadcastReceiver extends BroadcastReceiver
{
	private final String TAG = "PlanBroadCastReceiver";
	@Override
	public void onReceive(Context c, Intent intent) {		
		Intent startServiceIntent = new Intent("com.example.tbs.services.UPDATE_PLAN");			 
		ComponentName service = c.startService(startServiceIntent);

		if (null == service) {
			Log.w(TAG, "Failed to start data sync service");
		}
	}
}