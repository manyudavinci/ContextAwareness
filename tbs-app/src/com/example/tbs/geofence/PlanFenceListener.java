package com.example.tbs.geofence;

import nl.sense_os.platform.SensePlatform;
import nl.sense_os.service.provider.SNTP;
import nl.sense_os.service.shared.SensorDataPoint;
import nl.sense_os.service.subscription.DataConsumer;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.example.tbs.Constants;
import com.google.android.gms.location.Geofence;

public class PlanFenceListener implements DataConsumer {

	private SensePlatform mSensePlatform;
	private static final String TAG = "PlanFenceListener";

	public PlanFenceListener(SensePlatform sensePlatform) 
	{		
		mSensePlatform = sensePlatform;
	}

	public void outOfBound(Geofence geofence) {
		Log.e(TAG, "Google play services geofence, out of bounds!");
		sendToCommonSense();
	}

	public void sendToCommonSense() {
		final String value = "1";
		final long timestamp = SNTP.getInstance().getTime();
		// Only try to send data when the service is bound
		Log.e(TAG, "Sending Geofence alert data");

		if (mSensePlatform.getService().isBinderAlive()) {
			try {
				Thread sendData = new Thread() {
					public void run() {
						mSensePlatform.addDataPoint(Constants.ALERTS_LOG_SENSOR_NAME, Constants.ALERTS_LOG_SENSOR_DISPLAYNAME, Constants.ALERTS_LOG_SENSOR_DESCRIPTION, Constants.ALERTS_LOG_SENSOR_DATATYPE, null,
								value, timestamp);
					}
				};
				sendData.start();
			} catch (Exception e) {
				Log.e(TAG, "Failed to add data point!", e);
			}
		} else {
			Log.e(TAG, "Error Sending out of bounds data");
		}
	}

	public void startNewSample() {
	}

	public boolean isSampleComplete() {
		return false;
	}

	public void onNewData(SensorDataPoint dataPoint) {
		JSONObject json = dataPoint.getJSONValue();

		// the value to be sent, in json format
		try {
			Log.e(TAG, "Got Geofence alert from GeoFence AIM");
			if (json.getJSONObject("value").getInt("out of range") == 1) {
				Log.e(TAG, "Out of range");
				sendToCommonSense();
			} else {
				Log.e(TAG, "in range");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
