package com.example.tbs.geofence.play;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import com.example.tbs.geofence.PlanFenceListener;
import com.example.tbs.geofence.play.GeoFenceService;

import nl.sense_os.platform.SenseApplication;

import java.util.List;

public class PlanFenceService extends GeoFenceService {
    
    private static final String TAG = "PlanFenceService";

    public PlanFenceService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	Log.e(TAG, "Got intent for location");
      if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e(TAG, "Location Services error: " + Integer.toString(errorCode));
            /*
             * You can also send the error code to an Activity or Fragment with a broadcast Intent
             */

        } else {
            /*
             * If there is no error, get the transition type and the IDs of the geofence or
             * geofences that triggered the transition
             */
        	
        	
            // Get the type of transition (entry or exit)
            int transitionType = LocationClient.getGeofenceTransition(intent);

            // Test that a valid transition was reported
            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            	Log.e(TAG, "Got enter location");
                // get the geofence
                List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);
                if (null == triggerList || triggerList.size() < 1) {
                    Log.e(TAG, "Error getting the correct geofences!");
                }

                for (Geofence geofence : triggerList) {
                	String reqId = geofence.getRequestId();
                	Log.d(TAG, "Received ENTER trigger for geofence " + reqId);
                	if ( PlanFenceUtils.GEOFENCE_INNER_ID.equals( reqId ) ) 
                	{                    	
                        stopUpdates();
                    } else if ( PlanFenceUtils.GEOFENCE_OUTER_ID.equals( reqId ) ) {
                    	startUpdates();
                    } else {
                    	Log.d( TAG, "Received trigger from unknown fence!?" );
                    }
                }               

            } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            	// ATM we don't actually do anything on exit?
            	
                // get the geofence
                List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);
                if (null == triggerList || triggerList.size() < 1) {
                    Log.e(TAG, "Error getting the correct geofences!");
                }

                for (Geofence geofence : triggerList) {
                	String reqId = geofence.getRequestId();
                    Log.d(TAG, "Received EXIT trigger for geofence " + reqId );
                    if (PlanFenceUtils.GEOFENCE_INNER_ID.equals( reqId ) ) {
                    	Log.d( TAG, "Exit inner fence." );
                    } else if ( PlanFenceUtils.GEOFENCE_OUTER_ID.equals( reqId ) ) {
                    	// No we have a problem!
                    	PlanFenceListener apl = new PlanFenceListener(((SenseApplication) getApplication()).getSensePlatform());
                    	apl.outOfBound(geofence);
                    	startUpdates();
                    } else {
                    	Log.d( TAG, "Received trigger from unknown fence!?" );
                    }
                }

            } else {
                // An invalid transition was reported
                Log.e(TAG, "Geofence transition error: " + Integer.toString(transitionType));
            }
        }
    }
  
}
