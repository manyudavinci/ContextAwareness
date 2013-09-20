package com.example.tbs.services;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class Plan {
	static private String FIELD_START_TIME = "start_time";
	static private String FIELD_END_TIME = "end_time";
	static private String FIELD_lATITUDE = "latitude";
	static private String FIELD_LONGITUDE = "longitude";
	static private String FIELD_RADIUS = "radius";
	public double mStartTime;
	public double mEndTime;
	public double mLatitude;
	public double mLongitude;
	public double mRadius;

	public Plan(double startTime, double endTime, double latitude, double longitude, double radius) {
		mStartTime = startTime;
		mEndTime = endTime;
		mLatitude = latitude;
		mLongitude = longitude;
		mRadius = radius;
	}

	public Plan(JSONObject json) throws JSONException {
		mStartTime = json.getDouble(FIELD_START_TIME);
		mEndTime = json.getDouble(FIELD_END_TIME);
		mLatitude = json.getDouble(FIELD_lATITUDE);
		mLongitude = json.getDouble(FIELD_LONGITUDE);
		mRadius = json.getDouble(FIELD_RADIUS);
	}

	public String toString() {
		Date start = new Date(((long)mStartTime * 1000));
		Date end = new Date(((long)mEndTime * 1000));
		return "From " + start + " to " + end + " at (" + mLatitude + "," + mLongitude
				+ ") +- " + mRadius;
	}
	
	public boolean equals(Plan plan)
	{
		boolean isEqual = true;
		isEqual &= mStartTime 	== plan.mStartTime;
		isEqual &= mEndTime 	== plan.mEndTime;
		isEqual &= mLatitude 	== plan.mLatitude;
		isEqual &= mLongitude 	== plan.mLongitude;
		isEqual &= mRadius 		== plan.mRadius;
		return isEqual;
	}

}
