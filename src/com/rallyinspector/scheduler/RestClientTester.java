package com.rallyinspector.scheduler;

import com.rallyinspector.discrepancyhandlers.UserStoryWithoutFeatureDiscrepancyHandler;

public class RestClientTester {
	
	static UserStoryWithoutFeatureDiscrepancyHandler userStoryWithoutFeature = new UserStoryWithoutFeatureDiscrepancyHandler();

	public static void main(String[] args) {
		userStoryWithoutFeature.handleDiscrepancy();
	}

}
