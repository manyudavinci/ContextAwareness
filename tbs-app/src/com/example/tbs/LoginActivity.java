package com.example.tbs;

import nl.sense_os.platform.SenseApplication;
import nl.sense_os.platform.SensePlatform;
import nl.sense_os.platform.TrivialSensorRegistrator;
import nl.sense_os.service.ISenseServiceCallback;
import nl.sense_os.service.commonsense.SenseApi;
import nl.sense_os.service.commonsense.SensorRegistrator;
import nl.sense_os.service.constants.SensePrefs;
import nl.sense_os.service.constants.SensePrefs.Main.Advanced;
import nl.sense_os.service.constants.SensePrefs.Main.Location;
import nl.sense_os.service.constants.SensePrefs.Main.Motion;
import nl.sense_os.service.constants.SensePrefs.Main.PhoneState;
import nl.sense_os.service.provider.SNTP;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	private static final String TAG = "LoginActivity";
	public static String myNumber;
	public static String myMsg;
	public static boolean isVerified = false;
	private final static SmsReceived smsReceived = new SmsReceived();
	public final static String EXTRA_MESSAGE = "com.example.togglelocation";

	public int LOGIN_CHECK = 5;

	// Values for email and password at the time of the login attempt
	private String mEmail;
	private String mPassword;

	// UI references
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	// Sense specific members
	private SenseApplication mApplication;
	private SensePlatform mSensePlatform;
	private ISenseServiceCallback mServiceCallback = new ISenseServiceCallback.Stub() {

		@Override
		public void onChangeLoginResult(int result) throws RemoteException {

			busy = false;

			if (result == -2) {
				// login forbidden
				onLoginFailure(true);

			} else if (result == -1) {
				// login failed
				onLoginFailure(false);

			} else {
				phoneNumberCheck();
			}
		}

		@Override
		public void onRegisterResult(int result) throws RemoteException {
			// not used
		}

		@Override
		public void statusReport(int status) throws RemoteException {
			// not used
		}
	};

	private boolean busy;

	/**
	 * Attempts to sign in or register the account specified by the login form. If there are form
	 * errors (invalid email, missing fields, etc.), the errors are presented and no actual login
	 * attempt is made.
	 */
	private void attemptLogin() {
		if (busy) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);

			// log in (you only need to do this once, Sense will remember the
			// login)
			try {
				mApplication.getSensePlatform().login(mEmail, SenseApi.hashPassword(mPassword),
						mServiceCallback);
				// this is an asynchronous call, we get a callback when the
				// login is complete
				busy = true;
			} catch (IllegalStateException e) {
				Log.w(TAG, "Failed to log in at SensePlatform!", e);
				onLoginFailure(false);
			} catch (RemoteException e) {
				Log.w(TAG, "Failed to log in at SensePlatform!", e);
				onLoginFailure(false);
			}
		}
	}

	@SuppressWarnings("unused")
	private void flushData() {
		Log.v(TAG, "Flush buffers");
		mApplication.getSensePlatform().flushData();
		Toast.makeText(this, R.string.msg_flush_data, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_first);

	}

	public void onStart() {

		super.onStart();

		checkWifi();

		mApplication = (SenseApplication) getApplication();
		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});

	}

	private void onLoginFailure(final boolean forbidden) {
		Log.d(TAG, "Login failure");

		// update UI
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showProgress(false);

				if (forbidden) {
					mPasswordView.setError(getString(R.string.error_incorrect_password));
					mPasswordView.requestFocus();
				} else {
					Toast.makeText(LoginActivity.this, R.string.login_failure, Toast.LENGTH_LONG)
					.show();
				}
			}
		});
	}

	private void onLoginSuccess() {
		AlarmUtils.setAlarms(this);
		
		mSensePlatform = ((SenseApplication) getApplication()).getSensePlatform();
		// location preferences
		mSensePlatform.getService().setPrefBool(Location.GPS, true);
		mSensePlatform.getService().setPrefBool(Location.NETWORK, true);
		mSensePlatform.getService().setPrefBool(Location.AUTO_GPS, true);

		// motion preferences
		mSensePlatform.getService().setPrefBool(Motion.FALL_DETECT, false);
		mSensePlatform.getService().setPrefBool(Motion.BURSTMODE, false);
		mSensePlatform.getService().setPrefBool(Motion.ACCELEROMETER, false);
		mSensePlatform.getService().setPrefBool(Motion.GYROSCOPE, false);
		mSensePlatform.getService().setPrefBool(Motion.ORIENTATION, false);
		mSensePlatform.getService().setPrefBool(Motion.MOTION_ENERGY, false);
		mSensePlatform.getService().setPrefBool(Motion.LINEAR_ACCELERATION, false);

		// Phone State preferences
		mSensePlatform.getService().setPrefBool(PhoneState.PROXIMITY, false);
		mSensePlatform.getService().setPrefBool(PhoneState.BATTERY, true);
		mSensePlatform.getService().setPrefBool(PhoneState.SCREEN_ACTIVITY, false);
		mSensePlatform.getService().setPrefBool(PhoneState.CALL_STATE, false);
		mSensePlatform.getService().setPrefBool(PhoneState.SIGNAL_STRENGTH, false);
		mSensePlatform.getService().setPrefBool(PhoneState.DATA_CONNECTION, false);
		mSensePlatform.getService().setPrefBool(PhoneState.IP_ADDRESS, false);

		// sample and upload rate
		mSensePlatform.getService().setPrefString(SensePrefs.Main.SAMPLE_RATE, "-2"); // sample
		// every
		// 1 sec
		mSensePlatform.getService().setPrefString(SensePrefs.Main.SYNC_RATE, "-2"); // upload every
		// 1 min

		// enable Location, Phone state, Motion and start sensing
		mSensePlatform.getService().toggleLocation(true);
		mSensePlatform.getService().togglePhoneState(true);
		mSensePlatform.getService().toggleMotion(true);
		mSensePlatform.getService().toggleAmbience(false);
		mSensePlatform.getService().toggleDeviceProx(false);
		mSensePlatform.getService().toggleMain(true);

		// Check for the alerts log sensor
		SensorRegistrator registrator = new TrivialSensorRegistrator(getApplication());
		registrator.checkSensor(Constants.ALERTS_LOG_SENSOR_NAME,
				Constants.ALERTS_LOG_SENSOR_DISPLAYNAME, Constants.ALERTS_LOG_SENSOR_DATATYPE,
				Constants.ALERTS_LOG_SENSOR_DESCRIPTION, "1", null,
				SenseApi.getDefaultDeviceUuid(getApplication()));

		Log.d(TAG, "Logged in succesfully");

		// update UI
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_LONG)
				.show();
				//showProgress(false);				
			}
		});
		finish();				
	}

	public void onPhoneNumberCheckFail() {
		Log.d(TAG, "Phone number check failure");

		// update UI
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showProgress(false);

				Log.e(TAG, getString(R.string.error_phone_number_not_correct));
				Toast.makeText(LoginActivity.this,
						getString(R.string.error_phone_number_not_correct), Toast.LENGTH_LONG)
						.show();
			}
		});

		mSensePlatform = ((SenseApplication) getApplication()).getSensePlatform();
		mSensePlatform.getService().toggleMain(false);
		mSensePlatform.getService().logout();
	}

	public void onPhoneNumberCheckSuccess(String serialNumber) {
		if (serialNumber != null) {
			SharedPreferences prefs = getSharedPreferences(SensePrefs.STATUS_PREFS,
					Context.MODE_PRIVATE);
			Editor edit = prefs.edit();
			edit.putString(Constants.PREF_SIM_SERIAL_NUMBER, serialNumber);
			edit.commit();
		}
		Log.d(TAG, "Phone number and serial check successful");
		onLoginSuccess();
	}

	public void phoneNumberCheck() {

		final Context context = this;

		new Thread() {

			public void run() {

				// check the serial number
				SharedPreferences prefs = getSharedPreferences(SensePrefs.STATUS_PREFS,
						Context.MODE_PRIVATE);
				String storedSN = prefs.getString(Constants.PREF_SIM_SERIAL_NUMBER, "");
				TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				String serialNumber = tMgr.getSimSerialNumber();

				// Check the Serial number Verification of phone number
				if (!storedSN.equals("") && serialNumber.equals(serialNumber)) {
					onPhoneNumberCheckSuccess(null);
					return;
				}

				// receive the sms
				IntentFilter filter = new IntentFilter();
				filter.addAction("android.provider.Telephony.SMS_RECEIVED");
				filter.setPriority(1000);
				registerReceiver(smsReceived, filter);

				try {
					Looper.prepare();

					// get the phone number
					JSONObject json = SenseApi.getUser(context);
					myNumber = json.getString("mobile");

					// if we have a number do the check
					if (myNumber.length() > 0) {
						// send SMS with random number
						myMsg = "" + Math.random();
						SmsManager smsManager = SmsManager.getDefault();
						smsManager.sendTextMessage(myNumber, null, myMsg, null, null);

						// wait 2 minutes for the message
						int i = 0;
						while (i++ < 120 && !isVerified) {
							Log.d(TAG, "Waiting..." + i);
							Thread.sleep(1000);
						}

						// unregister for sms
						unregisterReceiver(smsReceived);

						if (!isVerified)
							onPhoneNumberCheckFail();
						else
							onPhoneNumberCheckSuccess(serialNumber);
					} else
						// No phone number set
						onPhoneNumberCheckSuccess("");

				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "Error verifing phone number");
					unregisterReceiver(smsReceived);
					onPhoneNumberCheckFail();
				}
			}
		}.start();
	}

	public void checkWifi() {
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (!(wifiManager.isWifiEnabled())) {

			Vibrator vi = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vi.vibrate(1000);
			Toast.makeText(this, getString(R.string.stat_notify_wireless_wifi_text), Toast.LENGTH_LONG).show();

			Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
			PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
			
			Notification noti = new Notification.Builder(this)
					.setContentTitle(getString(R.string.stat_notify_network_title)).setSound(sound)
					.setTicker(getString(R.string.stat_notify_wireless_wifi_text)).setSmallIcon(R.drawable.ic_launcher)
					.setContentIntent(pIntent).getNotification();

			// Hide the notification after its selected
			noti.flags |= Notification.FLAG_AUTO_CANCEL;

			notificationManager.notify(0, noti);

		}

		if (!wifi.isConnected() && !mobile.isConnected()) {

			Vibrator vi = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vi.vibrate(1000);
			Toast.makeText(this,getString(R.string.stat_notify_wireless_3G_text), Toast.LENGTH_LONG).show();

			Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
			PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

			Notification noti = new Notification.Builder(this)
					.setContentTitle(getString(R.string.stat_notify_network_title)).setSound(sound)
					.setTicker(getString(R.string.stat_notify_wireless_3G_text)).setSmallIcon(R.drawable.ic_launcher)
					.setContentIntent(pIntent).getNotification();

			// Hide the notification after its selected
			noti.flags |= Notification.FLAG_AUTO_CANCEL;

			notificationManager.notify(0, noti);
		}

	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.first, menu);
		return true;
	}

}
