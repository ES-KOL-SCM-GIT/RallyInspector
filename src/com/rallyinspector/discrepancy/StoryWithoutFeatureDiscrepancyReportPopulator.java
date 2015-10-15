package com.rallyinspector.discrepancy;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class StoryWithoutFeatureDiscrepancyReportPopulator {
	
	final static Logger logger = Logger.getLogger(StoryWithoutFeatureDiscrepancyReportPopulator.class);

	/**
	 * @param inputs - a list of stories without features retrieved by hitting Query Rally REST service end point.
	 * @return discrepancyReports - a list of JSONObjects, each of which holds information for one user story without feature.
	 * 
	 * Populates a list of stories without feature to insert into discrepancy database.
	 */
	public JSONArray createDiscrepancyTablePopulator(JSONArray inputs) {	
		
		JSONArray discrepancyReports = new JSONArray();
		int inputsCount = inputs.length();
		try {
			JSONObject discrepancyType = new JSONObject();
			discrepancyType.put("id", 1);
			
			for (int i = 0; i < inputsCount; i++) {

				JSONObject userStory = inputs.getJSONObject(i);

				JSONObject discrepancyReport = new JSONObject();
				discrepancyReport.put("formattedID", userStory.getString("FormattedID"));
				discrepancyReport.put("artifactName", userStory.getString("Name"));
				discrepancyReport.put("artifactRef", userStory.getString("_ref"));
				discrepancyReport.put("discType", discrepancyType);
				discrepancyReport.put("teamName", userStory.getJSONObject("Project").getString("_refObjectName"));
				
				discrepancyReports.put(discrepancyReport);
			}

		} catch (JSONException e) {
			logger.error("Problem", e);
		}
		return discrepancyReports;
	}
}
