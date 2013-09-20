package com.example.tbs;

import nl.sense_os.service.constants.SensePrefs;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

public class TbsBroadcastSIMCheck extends BroadcastReceiver {

	private static final String TAG = "TbsBroadcastSIMCheck";

	public void onReceive(Context c, Intent i) {
		Log.d(TAG, "Received broadcast");

		SharedPreferences prefs = c.getSharedPreferences(SensePrefs.STATUS_PREFS, Context.MODE_PRIVATE);
		String storedSN =  prefs.getString(Constants.PREF_SIM_SERIAL_NUMBER, "");
		TelephonyManager tMgr =(TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
		String serialNumber = tMgr.getSimSerialNumber();

		// Check the Serial number Verification of phone number
		if (!storedSN.equals("") && !serialNumber.equals(serialNumber))
		{
			i = new Intent(c, DisableSense.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra(Constants.DISABLE_SENSE_ALERT_TYPE, "5");
			c.startActivity(i);
		}
	}
}
