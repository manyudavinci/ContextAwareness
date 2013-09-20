package com.example.tbs;

import nl.sense_os.platform.SenseApplication;
import nl.sense_os.platform.SensePlatform;
import nl.sense_os.service.constants.SensePrefs;
import nl.sense_os.service.constants.SensePrefs.Auth;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class TbsActivity extends Activity {

	public static final int REQ_CODE_LOGOUT = 0;
	private static final String TAG = "TbsActvity";
	final long ONE_SECOND = 1000;
	final long TEN_SECONDS = ONE_SECOND * 10;
	boolean SETTINGS_COMPLETE = true;
	boolean NETWORK_COMPLETE = true;
	boolean LOGIN_COMPLETE = false;

	PendingIntent pi;
	AlarmManager am;
	Vibrator vi;

	public int SETTINGS_SCREEN_INTENT = 0;
	public int NETWORK_SCREEN_INTENT = 1;
	public int LOGIN_SCREEN_INTENT = 2;
	public int STATUS;

	private SensePlatform mSensePlatform;

	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_second);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);			
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "Activity " + requestCode + " result: " + resultCode);
		if (requestCode == SETTINGS_SCREEN_INTENT) {
			SETTINGS_COMPLETE = true;
		} else if (requestCode == NETWORK_SCREEN_INTENT) {
			NETWORK_COMPLETE = true;
		} else if (requestCode == LOGIN_SCREEN_INTENT) {
			if (resultCode == RESULT_OK) {
				Log.d(TAG, "WOOHOO logged in");
			} else {
				// something went wrong with login, close the application
				finish();
			}
		}
	}

	public void openGPSSettings() {
		if (SETTINGS_COMPLETE) {
			Toast.makeText(this, Resources.getSystem().getString(R.string.toast_gps_disabled), Toast.LENGTH_LONG).show();
			Vibrator vi = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
			vi.vibrate(1000);

			SETTINGS_COMPLETE = false;
		} else {
			Toast.makeText(this, Resources.getSystem().getString(R.string.toast_gps_disabled), Toast.LENGTH_LONG).show();
			Vibrator vi = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
			vi.vibrate(1000);
		}
	}

	public void onClickLogout(View v) {
		mSensePlatform = ((SenseApplication) getApplication()).getSensePlatform();
		mSensePlatform.getService().logout();
		AlarmUtils.cancelAlarms(this);
		onDisable();
		startLoginActivity();
	}

	public void onDisable() {
		mSensePlatform = ((SenseApplication) getApplication()).getSensePlatform();
		mSensePlatform.getService().toggleMain(false);
	}

	protected void onResume() {
		super.onResume();
		/*
		 * #DEBUG Toast.makeText(this, "onResume_2", Toast.LENGTH_SHORT) .show();
		 */		
		if (!isLoggedIn() || !isPhoneSimVerified()) {
			// Toast.makeText(this, "Going to FirstActivity from 2", Toast.LENGTH_SHORT).show();
			startLoginActivity();			
		} else {

			AlarmUtils.setAlarms(this);
		}
	}

	protected void onRestart() {
		super.onRestart();		
		/*
		 * #DEBUG Toast.makeText(this, "onRestart_2", Toast.LENGTH_SHORT).show();
		 */		
	}

	private void startLoginActivity() {
		Intent login = new Intent(this, LoginActivity.class);
		startActivityForResult(login, LOGIN_SCREEN_INTENT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.second, menu);
		return true;
	}

	/*
	 * protected void onDestroy() { am.cancel(pi); unregisterReceiver(br); super.onDestroy(); }
	 */
	private boolean isLoggedIn() {
		SharedPreferences prefs = getSharedPreferences(SensePrefs.AUTH_PREFS, MODE_PRIVATE);
		String cookie = prefs.getString(Auth.LOGIN_COOKIE, null);
		return null != cookie && cookie.length() > 0;
	}

	private boolean isPhoneSimVerified() {
		SharedPreferences prefs = getSharedPreferences(SensePrefs.STATUS_PREFS,
				Context.MODE_PRIVATE);
		String storedSN = prefs.getString(Constants.PREF_SIM_SERIAL_NUMBER, "");
		TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String serialNumber = tMgr.getSimSerialNumber();

		// Check the Serial number Verification of phone number
		return (!storedSN.equals("") && serialNumber.equals(serialNumber));
	}
}
