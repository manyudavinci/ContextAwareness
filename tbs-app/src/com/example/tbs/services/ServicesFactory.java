package com.example.tbs.services;

import nl.sense_os.platform.SenseApplication;

public class ServicesFactory {
	static private PlanService mPlanService;
	
	static public void initAll(SenseApplication senseApplication) {
		getPlanService(senseApplication);
	}

	static public PlanService getPlanService(SenseApplication senseApplication) {
		if (mPlanService == null)
			mPlanService = new PlanService(senseApplication);
		return mPlanService;
	}

}
