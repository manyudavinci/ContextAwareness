package com.example.tbs.geofence.play;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.service.provider.SNTP;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
//import android.location.Location;
//import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.example.tbs.services.Plan;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

public class PlanFenceUtils implements ConnectionCallbacks, OnConnectionFailedListener,
        OnAddGeofencesResultListener, OnRemoveGeofencesResultListener {

    public static final String GEOFENCE_OUTER_ID = "plan_fence_outer";
    public static final String GEOFENCE_INNER_ID = "plan_fence_inner";
    private static PlanFenceUtils sInstance;
    private static final String TAG = "PlanFenceUtils";
    private final float RADIUS_MARGIN = 100;

    public static PlanFenceUtils getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new PlanFenceUtils(context);
        }
        return sInstance;
    }

    private Context mContext;
    private Plan mPlan;
    private LocationClient mLocClient;

    private PlanFenceUtils(Context context) {
        mContext = context;
    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        if (statusCode == LocationStatusCodes.SUCCESS) {
            for (String id : geofenceRequestIds) {
                Log.v(TAG, "Added geofence " + id);
            }
        } else {
            for (String id : geofenceRequestIds) {
                Log.e(TAG, "Failed to set up geofence " + id);
            }
        }
    }

    @SuppressLint("NewApi")
	@Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "Connected to Play Services");

//        mLocClient.setMockMode(true);
//        Location loc= new Location(LocationManager.GPS_PROVIDER);
//        loc.setLatitude(51.9035);
//        loc.setLongitude(4.4598);
//        loc.setAccuracy(5);
//        loc.setElapsedRealtimeNanos((long)1000*1000);
//        loc.setTime(SNTP.getInstance().getTime()*1000);
//        
//        mLocClient.setMockLocation(loc);
        // add the geofence
        Intent intent = new Intent(mContext, PlanFenceService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Geofence.Builder builder = new Geofence.Builder();
        List<Geofence> geofences = new ArrayList<Geofence>();
        
        // inner
        if (mPlan.mRadius-RADIUS_MARGIN > 0)
        {
	        builder.setCircularRegion(mPlan.mLatitude, mPlan.mLongitude,(float) mPlan.mRadius-RADIUS_MARGIN);
	        builder.setRequestId(GEOFENCE_INNER_ID);
	      //builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
	        builder.setExpirationDuration((long)mPlan.mEndTime-(SNTP.getInstance().getTime()/1000));
	        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
	        geofences.add(builder.build());
        }
        Log.e(TAG, mPlan.toString());
        // outer
        builder.setCircularRegion(mPlan.mLatitude, mPlan.mLongitude,(float) mPlan.mRadius);
        builder.setRequestId(GEOFENCE_OUTER_ID);        
        //builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        builder.setExpirationDuration((long)mPlan.mEndTime-(SNTP.getInstance().getTime()/1000));
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
        geofences.add(builder.build());

        Log.v(TAG, "Adding geofences");        
       
        mLocClient.addGeofences(geofences, pendingIntent, this);
//        loc= new Location(LocationManager.GPS_PROVIDER);
//        loc.setLatitude(50.9035);
//        loc.setLongitude(4.4598);
//        loc.setAccuracy(5);
//        loc.setElapsedRealtimeNanos((long)1000*1000);
//        loc.setTime(SNTP.getInstance().getTime()*1000);
//        
//        mLocClient.setMockLocation(loc);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "Failed to connect to the Google Play services location client!");
    }

    @Override
    public void onDisconnected() {
        Log.v(TAG, "Disconnected from Google Play services");
    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {
        if (statusCode == LocationStatusCodes.SUCCESS) {
            Log.d(TAG, "Removed geofence");
        } else if (statusCode == LocationStatusCodes.GEOFENCE_NOT_AVAILABLE) {
            Log.d(TAG, "Geofence not available for removal");
        } else {
            Log.w(TAG, "Error removing geofence!");
        }
    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
        if (statusCode == LocationStatusCodes.SUCCESS) {
            if (null != geofenceRequestIds) {
                for (String geofenceRequestId : geofenceRequestIds) {
                    Log.d(TAG, "Removed geofence " + geofenceRequestId);
                }
            }
        } else if (statusCode == LocationStatusCodes.GEOFENCE_NOT_AVAILABLE) {
            Log.d(TAG, "Geofence not available for removal");
        } else {
            Log.w(TAG, "Error removing geofence!");
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(TAG, "Google Play services is available.");
            // Continue
            return true;

        } else {
            // Google Play services was not available for some reason
            Log.e(TAG, "Cannot connect to Google Play services! error=" + resultCode);
            return false;
        }
    }

    public void setGeofenceEnabled(Plan plan, boolean enabled) {

        if (enabled) {
            if (!servicesConnected()) {
                return;
            }

            mPlan = plan;

            mLocClient = new LocationClient(mContext, this, this);
            // Request a connection from the client to Location Services
            mLocClient.connect();
        } else {

            mPlan = null;

            // check that the location client is initialized
            if (null != mLocClient) {
                List<String> geofenceRequestIds = new ArrayList<String>();
                geofenceRequestIds.add(GEOFENCE_INNER_ID);
                mLocClient.removeGeofences(geofenceRequestIds, this);
                mLocClient.disconnect();
            }
        }
    }
}
