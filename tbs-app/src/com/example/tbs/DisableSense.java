package com.example.tbs;

import nl.sense_os.platform.SenseApplication;
import nl.sense_os.platform.SensePlatform;
import nl.sense_os.service.R;
import nl.sense_os.service.provider.SNTP;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class DisableSense extends Activity {

	private final String TAG = "DisableSense";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent myIntent = getIntent();
		Toast.makeText(this,"Afmelden...", Toast.LENGTH_LONG).show();
		if (myIntent != null) {
			Thread disable = new Thread() {
				public void run() {
					onDisable(myIntent.getStringExtra(Constants.DISABLE_SENSE_ALERT_TYPE));
				}
			};
			disable.start();
		}
	}

	public void onDisable(String alert_type) {
		final SensePlatform mSensePlatform = ((SenseApplication) getApplication())
				.getSensePlatform();
		// log the reason to the alert log sensor to commonSense
		if (mSensePlatform.getService().isBinderAlive()) {

			mSensePlatform.addDataPoint(Constants.ALERTS_LOG_SENSOR_NAME,
					Constants.ALERTS_LOG_SENSOR_DISPLAYNAME,
					Constants.ALERTS_LOG_SENSOR_DESCRIPTION, Constants.ALERTS_LOG_SENSOR_DATATYPE,
					null, alert_type, SNTP.getInstance().getTime());
		}

		Intent flush = new Intent(getString(R.string.action_sense_send_data));
		startService(flush);
		
		AlarmUtils.cancelAlarms(this);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage());
		}

		mSensePlatform.getService().toggleMain(false);
		mSensePlatform.getService().logout();
		finish();
	}
}
