package com.rallyinspector.scheduler;

import com.rallyinspector.queries.UserStoryWithoutFeatureQuery;

public class RestClientTester {
	
	static UserStoryWithoutFeatureQuery userStoryWithoutFeature = new UserStoryWithoutFeatureQuery();

	public static void main(String[] args) {
		userStoryWithoutFeature.createQueryForPost();
	}

}
