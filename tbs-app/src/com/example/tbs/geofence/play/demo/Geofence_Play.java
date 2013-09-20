/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use mContext file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.tbs.geofence.play.demo;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import android.text.TextUtils;

import android.util.Log;
import android.widget.Toast;

import com.example.tbs.services.Plan;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.platform.SensePlatform;
import nl.sense_os.service.provider.SNTP;

/**
 * UI handler for the Location Services Geofence sample app.
 * Allow input of latitude, longitude, and radius for two geofences.
 * When registering geofences, check input and then send the geofences to Location Services.
 * Also allow removing either one of or both of the geofences.
 * The menu allows you to clear the screen or delete the geofences stored in persistent memory.
 */
public class Geofence_Play {
	/*
	 * Use to set an expiration time for a geofence. After mContext amount
	 * of time Location Services will stop tracking the geofence.
	 * Remember to unregister a geofence when you're finished with it.
	 * Otherwise, your app will use up battery. To continue monitoring
	 * a geofence indefinitely, set the expiration time to
	 * Geofence#NEVER_EXPIRE.
	 */

	// Add geofences handler
	private GeofenceRequester mGeofenceRequester;
	// Remove geofences handler
	//private GeofenceRemover mGeofenceRemover;
	private SensePlatform mSensePlatform;

	// An intent filter for the broadcast receiver
	private IntentFilter mIntentFilter;

	private Context mContext;

	public Geofence_Play(SensePlatform sensePlatform, Context context) {

		mContext = context;

		// Create an intent filter for the broadcast receiver
		mIntentFilter = new IntentFilter();

		// Action for broadcast Intents that report successful addition of geofences
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

		// Action for broadcast Intents that report successful removal of geofences
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

		// Action for broadcast Intents containing various types of geofencing errors
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

		// All Location Services sample apps use mContext category
		mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

		mSensePlatform = sensePlatform;
		// Instantiate a Geofence requester
		mGeofenceRequester = new GeofenceRequester(mContext);

		// Instantiate a Geofence remover
	//	mGeofenceRemover = new GeofenceRemover(mContext);      
		
	}


	/**
	 * Verify that Google Play services is available before making a request.
	 *
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode =
				GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {

			// In debug mode, log the status
			Log.d(GeofenceUtils.APPTAG,"play_services_available");

			// Continue
			return true;

			// Google Play services was not available for some reason
		} else {

			// Display an error dialog
			Log.e(GeofenceUtils.APPTAG, "Error Starting google play services");
			return false;
		}
	}


	public boolean addGeofence(Plan plan)
	{
		
		/*
		 * Check for Google Play services. Do mContext after
		 * setting the request type. If connecting to Google Play services
		 * fails, onActivityResult is eventually called, and it needs to
		 * know what type of request was in progress.
		 */
		if (!servicesConnected()) {
			Log.e(GeofenceUtils.APPTAG, "Google Play service not connected!");
			return false;
		}
		Log.e(GeofenceUtils.APPTAG, "Adding geofence plan: "+plan.toString());
		SimpleGeofence mUIGeofence1 = new SimpleGeofence(
				"1",
				plan.mLatitude, 
				plan.mLongitude, 
				(float)plan.mRadius,
				// Set the expiration time
				(long)((plan.mEndTime-plan.mStartTime)*1000),
				// Only detect exit transitions
				Geofence.GEOFENCE_TRANSITION_EXIT);
		List<Geofence> mCurrentGeofences = new ArrayList<Geofence>(); 
		mCurrentGeofences.add(mUIGeofence1.toGeofence());

		// Start the request. Fail if there's already a request in progress
		try {
			// Try to add geofences
			mGeofenceRequester.addGeofences(mCurrentGeofences);
			return true;
		} catch (UnsupportedOperationException e) {
			Log.e(GeofenceUtils.APPTAG, "Geofence adding in progress");
			// Notify user that previous request hasn't finished.
			// Toast.makeText(mContext, R.string.add_geofences_already_requested_error,
			//          Toast.LENGTH_LONG).show();
		}
		return true;
	}

	/**
	 * Define a Broadcast receiver that receives updates from connection listeners and
	 * the geofence transition service.
	 */
	public class GeofenceSampleReceiver extends BroadcastReceiver {
		/*
		 * Define the required method for broadcast receivers
		 * mContext method is invoked when a broadcast Intent triggers the receiver
		 */
		@Override
		public void onReceive(Context context, Intent intent) {

			// Check the action code and determine what to do
			String action = intent.getAction();

			// Intent contains information about errors in adding or removing geofences
			if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

				handleGeofenceError(context, intent);

				// Intent contains information about successful addition or removal of geofences
			} else if (
					TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
					||
					TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

				handleGeofenceStatus(context, intent);

				// Intent contains information about a geofence transition
			} else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

				handleGeofenceTransition(context, intent);

				// The Intent contained an invalid action
			} else {
				Log.e(GeofenceUtils.APPTAG, "Invalid action");
				// Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
			}
		}

		/**
		 * If you want to display a UI message about adding or removing geofences, put it here.
		 *
		 * @param context A Context for mContext component
		 * @param intent The received broadcast Intent
		 */
		private void handleGeofenceStatus(Context context, Intent intent) {

		}

		/**
		 * Report geofence transitions to the UI
		 *
		 * @param context A Context for mContext component
		 * @param intent The Intent containing the transition
		 */
		private void handleGeofenceTransition(Context context, Intent intent) {
			/*
			 * If you want to change the UI when a transition occurs, put the code
			 * here. The current design of the app uses a notification to inform the
			 * user that a transition has occurred.
			 */
			
			// send the transition to commonSense			
			// Description of the sensor
			// This is only used to send data to CommonSense
			final String name = "alerts_log_sensor";
			final String displayName = "Alerts Log";
			final String dataType = "string";
			final String description = "tbs";			
			final String value = "1";
			final long timestamp = SNTP.getInstance().getTime();
			// Only try to send data when the service is bound
			Log.e(GeofenceUtils.APPTAG, "Sending Geofence alert data");
			if(mSensePlatform.getService().isBinderAlive())
			{
				try {					
					Thread sendData = new Thread(){
						public void run(){
						mSensePlatform.addDataPoint(name, displayName, description, dataType, value, timestamp);
					}};
					sendData.start();
				} catch (Exception e) {
					Log.e(GeofenceUtils.APPTAG, "Failed to add data point!", e);
				}
			}
		}

		/**
		 * Report addition or removal errors to the UI, using a Toast
		 *
		 * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
		 */
		private void handleGeofenceError(Context context, Intent intent) {
			String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
			Log.e(GeofenceUtils.APPTAG, msg);
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}
	}
	/**
	 * Define a DialogFragment to display the error dialog generated in
	 * showErrorDialog.
	 */
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 *
		 * @param dialog An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * mContext method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
}
