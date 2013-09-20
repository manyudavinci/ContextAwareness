package com.example.tbs.geofence.play;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.tbs.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public abstract class GeoFenceService
extends IntentService
implements LocationListener
, GooglePlayServicesClient.ConnectionCallbacks
, GooglePlayServicesClient.OnConnectionFailedListener {

	private static final String TAG = GeoFenceService.class.getSimpleName();
	// desired interval of location updates (within outer perimeter)
	public static final int GEOFENCE_TARGET_INTERVAL = 3000;

	// maximum interval of location updates (within outer perimeter)
	public static final int GEOFENCE_LIMIT_INTERVAL = 500;
	
	// holds accuracy and frequency settings
	private LocationRequest mLocationRequest;

	// the client for which we'll start and stop updates
	private LocationClient mLocationClient;

	// whether we are currently getting updates
	private boolean mUpdatesRequested;

	// whether the onConnected callback has been callbacked
	//private boolean mServiceConnected;

	public GeoFenceService() {
		this( TAG );
	}

	public GeoFenceService( String tag ) {
		super( tag );
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );
		mLocationRequest.setInterval( GEOFENCE_TARGET_INTERVAL );
		mLocationRequest.setFastestInterval( GEOFENCE_LIMIT_INTERVAL );
		mLocationClient = new LocationClient( this, this, this );
		mUpdatesRequested = false;
		//mServiceConnected = false;
	}

	@Override
	public void onLocationChanged( Location location ) {
		// we're intentionally not actually doing anything with these location updates;
		// see http://kwazylabs.com/blog/google_geofencing/
		Log.e(TAG, "Location change: "+location.toString());
	}

	@Override
	public void onConnected( Bundle connectionHint ) {
		//mServiceConnected = true;
		if ( mUpdatesRequested ) {
			mLocationClient.requestLocationUpdates( mLocationRequest, this );
		} else {
			mLocationClient.removeLocationUpdates( this );
		}
	}

	@Override
	public void onDisconnected() {
		//mServiceConnected = false;
	}

	@Override
	public void onConnectionFailed( ConnectionResult result ) {
		// TODO do we need to do anything here?
		Log.e( TAG, "connection failed!" );
	}

	// request location updates
	protected void startUpdates() {
		Log.i( TAG, "starting location updates" );
		mUpdatesRequested = true;
		if ( mLocationClient.isConnected() ) {
			mLocationClient.requestLocationUpdates( mLocationRequest, this );
		}
		// remove:
		 notifyDebug( "debug", "start location update" );
	}

	// tseuqer location updates
	protected void stopUpdates() {
		Log.i( TAG, "stopping location updates" );
		mUpdatesRequested = false;
		if ( mLocationClient.isConnected() ) {
			mLocationClient.removeLocationUpdates( this );
		}
		// remove:
		 notifyDebug( "debug", "stop location updates" );
	}

	// TODO Remove
	private void notifyDebug( String title, String text ) {
		// build notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder( this.getApplication() );
		builder.setContentTitle( title );
		builder.setContentText( text );
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setDefaults(Notification.DEFAULT_ALL);
		builder.setAutoCancel(true);
		NotificationManager nm = (NotificationManager) getApplication()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle(builder);
		bigText.setBigContentTitle( title );
		bigText.bigText( text );
		nm.notify(TAG, 1, bigText.build());
	}

}
