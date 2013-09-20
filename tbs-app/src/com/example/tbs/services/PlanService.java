package com.example.tbs.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nl.sense_os.platform.SenseApplication;
import nl.sense_os.platform.SensePlatform;
import nl.sense_os.service.constants.SensorData.SensorNames;
import nl.sense_os.service.provider.SNTP;
import nl.sense_os.service.shared.SensorDataPoint;
import nl.sense_os.service.subscription.DataConsumer;
import nl.sense_os.service.subscription.SubscriptionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.tbs.AlarmUtils;
import com.example.tbs.Constants;
import com.example.tbs.geofence.aim.PlanGeoFence;
//import com.example.tbs.geofence.play.PlanFenceUtils;
//import com.example.tbs.geofence.Geofence_Play;

public class PlanService extends Service implements ServiceConnection {
	static private final String TAG = "PlanChecker";
	static private String PLANNING_SENSOR_NAME = "planning_sensor";
	private List<Plan> mPlans = new ArrayList<Plan>();
	private Plan mCurrentPlan = null;
	private SensePlatform mSensePlatform;
	private boolean isConnected = false;
	private long mLastPlanDownloadTime = 0; // in seconds
	private long mPlanDownloadInterval = 60 * 5; // in seconds, 5 minutes
	private BatteryStatus batteryStatus = null;

	@Override
	public void onCreate() {
		mSensePlatform = new SensePlatform(this, this);
		AlarmUtils.setAlarms(this);
	}

	public PlanService() {
	}

	public PlanService(SenseApplication senseApplication) {
		mSensePlatform = senseApplication.getSensePlatform();
	}

	private void update() {
		try {
			new Thread() {
				public void run() {
					if(((SNTP.getInstance().getTime()/1000)-mLastPlanDownloadTime) >= mPlanDownloadInterval)					
					{
						// update SNTP clock offset
						SNTP.getInstance().requestTime(SNTP.HOST_WORLDWIDE, 2000);
						updateList();
					}
					updateCurrentPlan();
				}
			}.start();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void updateList() {
		JSONArray planDataPoints = null;
		try {

			// Get the plans from common sense
			planDataPoints = mSensePlatform.getData(PLANNING_SENSOR_NAME, false, 1000);
			if (planDataPoints.length() != 0)
				mLastPlanDownloadTime = (SNTP.getInstance().getTime() / 1000);
			// parse the plans into newPlanList
			List<Plan> newPlanList = new ArrayList<Plan>();
			for (int i = 0; i < planDataPoints.length(); i++) {
				try {
					JSONObject jsonPlan = new JSONObject(planDataPoints.getJSONObject(i).getString(
							"value"));

					Plan p = new Plan(jsonPlan);
					if ((p.mEndTime > SNTP.getInstance().getTime() / 1000)) {
						Log.d(TAG, "Adding plan to list:" + p.toString());
						newPlanList.add(p);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			// sort list on startTime
			Collections.sort(newPlanList, new Comparator<Plan>() {
				public int compare(Plan p1, Plan p2) {

					if (p1.mStartTime < p2.mStartTime) {
						return -1;

					} else
						return 1;
				}
			});

			// Sorted List
			mPlans = newPlanList;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update the current plan
	 * 
	 * Check if there is a plan finished or needs to be started and update the geofence
	 */
	private void updateCurrentPlan() {
		try {
			if (mCurrentPlan != null
					&& mCurrentPlan.mEndTime <= (SNTP.getInstance().getTime() / 1000))
				// end of the plan
				endCurrentPlan();
			else if (mCurrentPlan != null)
				// re-enable geofence in case the SenseService is restarted
				new PlanGeoFence(mSensePlatform);

			if (mPlans.size() > 0) {
				Plan nextPlan = mPlans.get(0);
				if (nextPlan.mStartTime <= (SNTP.getInstance().getTime() / 1000)) {
					setCurrentPlan(nextPlan);

					if (mPlans.size() > 0)
						mPlans.remove(0);
				}
			}
		} catch (Exception e) {
			// mPlans could be edited in a different thread..
			Log.e(TAG, "Error updating plan");
		}
	}

	private void setCurrentPlan(Plan plan) {
		if (mCurrentPlan == null || !mCurrentPlan.equals(plan)) {
			Log.d(TAG, "Starting plan: " + plan.toString());
			mCurrentPlan = plan;
			PlanGeoFence pgf = new PlanGeoFence(mSensePlatform);
			pgf.setPlan(plan);
			// add a battery status check
			SubscriptionManager sm = SubscriptionManager.getInstance();
			if (batteryStatus == null) {
				// Create new planFenceListener DataProcessor which is used to display the data on a
				// fragment, and send it to CommonSense
				batteryStatus = new BatteryStatus();
				sm.subscribeConsumer(SensorNames.BATTERY_SENSOR, batteryStatus);
			}

			// Don't use google play services ATM
			// Disable Google Play services Geo-fence because it creates an exception
			// PlanFenceUtils.getInstance(this).setGeofenceEnabled((Plan) plan, true);
		}
	}

	private void endCurrentPlan() {
		Log.d(TAG, "Stopping plan: " + mCurrentPlan.toString());
		PlanGeoFence pgf = new PlanGeoFence(mSensePlatform);
		pgf.disableGeoFence();
		// PlanFenceUtils.getInstance(this).setGeofenceEnabled(null, false);
		mCurrentPlan = null;
		SubscriptionManager sm = SubscriptionManager.getInstance();
		sm.unsubscribeConsumer(SensorNames.BATTERY_SENSOR, batteryStatus);
		batteryStatus = null;
	}

	@Override
	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		isConnected = true;
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "SensePlatform disconnected");
		isConnected = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "PlanService start");
		if (isConnected)
			update();
		else
			Log.d(TAG, "SensePlatform not connected");
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		if (isConnected)
			mSensePlatform.close();
	}

	public class BatteryStatus implements DataConsumer {

		private long last_send_time = 0;
		private long send_interval = 1000 * 60 * 15; // 15 minutes

		@Override
		public void startNewSample() {
		}

		@Override
		public boolean isSampleComplete() {
			return false;
		}

		@Override
		public void onNewData(SensorDataPoint dataPoint) {
			JSONObject json = dataPoint.getJSONValue();
			try {

				if (SNTP.getInstance().getTime() - last_send_time < send_interval)
					return;

				if (Integer.parseInt(json.getString("level")) < 15) {
					final String value = "2";
					final long timestamp = SNTP.getInstance().getTime();
					// Only try to send data when the service is bound
					if (mSensePlatform.getService().isBinderAlive()) {

						Thread sendData = new Thread() {
							public void run() {
								mSensePlatform.addDataPoint(Constants.ALERTS_LOG_SENSOR_NAME,
										Constants.ALERTS_LOG_SENSOR_DISPLAYNAME,
										Constants.ALERTS_LOG_SENSOR_DESCRIPTION,
										Constants.ALERTS_LOG_SENSOR_DATATYPE, null, value,
										timestamp);
								last_send_time = SNTP.getInstance().getTime();
							}
						};
						sendData.start();

					} else {
						Log.e(TAG, "Error Sending low battery alert");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
