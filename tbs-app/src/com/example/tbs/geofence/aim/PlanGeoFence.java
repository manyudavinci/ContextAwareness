package com.example.tbs.geofence.aim;

import java.util.List;

import com.example.tbs.geofence.PlanFenceListener;
import com.example.tbs.services.Plan;

import nl.sense_os.cortex.GeoFenceSensor;
import nl.sense_os.platform.SensePlatform;
import nl.sense_os.service.subscription.*;

public class PlanGeoFence {

	/** The DataProcessor */
	private GeoFenceSensor geoFence;
	/** The name of the DataProcessor */
	public final static String TAG = "TBS Geo-fencing";	
	
	/** The DataProcessor which handles the data coming from the GeoFence DataProcessor */
	private PlanFenceListener planFenceListener;
	
	public PlanGeoFence(SensePlatform sensePlatform)
	{			
		SubscriptionManager sm = SubscriptionManager.getInstance();
		// Check if the DataProcessor is already registered at the Sense Service
		if(sm.isProducerRegistered(PlanGeoFence.TAG))		
			// Get the GeoFence
			geoFence = (GeoFenceSensor) sm.getRegisteredProducers(PlanGeoFence.TAG).get(0);		
		else			
			// Create the actual GeoFence DataProcessor, which will be registered at the Sense Service with the given name (TAG)
			geoFence = new GeoFenceSensor(TAG, sensePlatform.getService().getSenseService());
				
		List<DataConsumer> consumers = sm.getSubscribedConsumers(PlanGeoFence.TAG);
		if(consumers == null || consumers.size() == 0)
		{
			// Create new planFenceListener DataProcessor which is used to display the data on a fragment, and send it to CommonSense
			planFenceListener = new PlanFenceListener(sensePlatform);
			sm.subscribeConsumer(TAG, planFenceListener);
		}
		else
			// Get the getData class which has the fragment for the display
			planFenceListener = (PlanFenceListener) consumers.get(0);
	}	
	
	public void setPlan(Plan plan)
	{		
		// Set the goal location to create a fence around
		geoFence.setGoalLocation(plan.mLatitude,plan.mLongitude);
		// Set the circle diameter range from the goal location as fence
		geoFence.setRange(plan.mRadius);
		geoFence.enable();
	}
	
	public void disableGeoFence()
	{
		geoFence.disable();
	}
}
