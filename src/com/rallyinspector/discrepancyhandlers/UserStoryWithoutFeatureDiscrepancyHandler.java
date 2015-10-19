package com.rallyinspector.discrepancyhandlers;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.rallyinspector.connector.RallyInspectorConnector;
import com.rallyinspector.util.RallyInspectorApplicationConfiguration;
import com.rallyinspector.util.RallyInspectorPropertiesReaderBean;

public class UserStoryWithoutFeatureDiscrepancyHandler {

	final static Logger logger = Logger.getLogger(UserStoryWithoutFeatureDiscrepancyHandler.class);

	
	public void handleDiscrepancy() {		
		RallyInspectorConnector rallyInspectorConnector = new RallyInspectorConnector();
		ApplicationContext context = new AnnotationConfigApplicationContext(
				RallyInspectorApplicationConfiguration.class);
		RallyInspectorPropertiesReaderBean rallyInspectorProperties = (RallyInspectorPropertiesReaderBean) context
				.getBean("rallyInspectorPropertiesReader");
		
		JSONArray listOfStoriesWithDiscrepancy = invokeStoryWithoutFeatureQuery(rallyInspectorConnector,context,rallyInspectorProperties);
		String responseAfterDbInsert = insertDiscrepanciesIntoDB(listOfStoriesWithDiscrepancy,rallyInspectorConnector,context,rallyInspectorProperties);
		logger.info("Output from the service is: " + responseAfterDbInsert);
	}
	
	/**
	 * Builds the final query filter used to invoke Query Rally REST service. Invokes said REST service. 
	 */
	public JSONArray invokeStoryWithoutFeatureQuery(RallyInspectorConnector rallyInspectorConnector, ApplicationContext context,RallyInspectorPropertiesReaderBean rallyInspectorProperties) {
		
		String queryRallyInvocationUrl = rallyInspectorProperties.getServerUri().concat(rallyInspectorProperties.getQueryRally());	
		String webResourceType = rallyInspectorProperties.getWebresourceType();
		JSONArray results = new JSONArray();
		try {
			// Creates the object used to invoke queryrally service.
			JSONObject finalJson = new JSONObject();
			finalJson.put("queryReqType", "HierarchicalRequirement");			
			finalJson.put("queryReqFetch", "FormattedID,Name,_ref,Project");
			
			JSONObject finalQueryRequestFilter = buildQueryFilter();
			
			finalJson.put("queryReqFilter", finalQueryRequestFilter);			
			finalJson.put("queryReqWorkspaceRef",
					"https://rally1.rallydev.com/slm/webservice/v2.0/workspace/1089940337");

			JSONObject resultJson = rallyInspectorConnector.postData(finalJson, webResourceType, queryRallyInvocationUrl);
			results = resultJson.getJSONArray("response");
			
		} catch (JSONException e) {
			logger.error("Problem creating query", e);
		} catch (NumberFormatException e) {
			logger.error("Problem invoking connector", e);
		}
		return results;
	}
	
	/**
	 * Build up query filter object for User Story without Feature
	 * 
	 * @return
	 * @throws JSONException
	 */
	private JSONObject buildQueryFilter() throws JSONException {
		// Relation 1 - For Schedule Filter
		JSONArray scheduleStateQueryFilters = new JSONArray();

		JSONObject scheduleStateQueryFilterInProg = new JSONObject();
		scheduleStateQueryFilterInProg = new JSONObject();
		scheduleStateQueryFilterInProg.put("field", "ScheduleState");
		scheduleStateQueryFilterInProg.put("operator", "=");
		scheduleStateQueryFilterInProg.put("value", "In-Progress");
		scheduleStateQueryFilters.put(scheduleStateQueryFilterInProg);

		JSONObject scheduleStateQueryFilterComp = new JSONObject();
		scheduleStateQueryFilterComp = new JSONObject();
		scheduleStateQueryFilterComp.put("field", "ScheduleState");
		scheduleStateQueryFilterComp.put("operator", "=");
		scheduleStateQueryFilterComp.put("value", "Completed");
		scheduleStateQueryFilters.put(scheduleStateQueryFilterComp);

		JSONObject scheduleStateQueryFilterAcc = new JSONObject();
		scheduleStateQueryFilterAcc = new JSONObject();
		scheduleStateQueryFilterAcc.put("field", "ScheduleState");
		scheduleStateQueryFilterAcc.put("operator", "=");
		scheduleStateQueryFilterAcc.put("value", "Accepted");
		scheduleStateQueryFilters.put(scheduleStateQueryFilterAcc);

		JSONObject scheduleStateRelation = new JSONObject();
		scheduleStateRelation.put("queryFilters", scheduleStateQueryFilters);
		scheduleStateRelation.put("relationType", false);

		// Relation 2 - For Feature and Date
		JSONArray featureDateQueryFilters = new JSONArray();

		JSONObject featureQueryFilter = new JSONObject();
		featureQueryFilter.put("field", "Feature");
		featureQueryFilter.put("operator", "=");
		featureQueryFilter.put("value", "null");
		featureDateQueryFilters.put(featureQueryFilter);

		JSONObject creationDateQueryFilter = new JSONObject();
		creationDateQueryFilter.put("field", "CreationDate");
		creationDateQueryFilter.put("operator", "=");
		creationDateQueryFilter.put("value", "today-90");
		featureDateQueryFilters.put(creationDateQueryFilter);

		JSONObject featureDateRelation = new JSONObject();
		featureDateRelation.put("queryFilters", featureDateQueryFilters);
		featureDateRelation.put("relationType", true);

		// Final Query Filter build up
		JSONArray finalRelations = new JSONArray();
		finalRelations.put(scheduleStateRelation);
		finalRelations.put(featureDateRelation);

		JSONObject finalQueryFilter = new JSONObject();
		finalQueryFilter.put("relations", finalRelations);
		finalQueryFilter.put("relationType", true);
		return finalQueryFilter;
	}
	
	/**
	 * @param inputs - response object (JSONArray) containing list of all user stories without feature.
	 * @return
	 * 
	 * Invokes Save Discrepancy List REST service to insert records into discrepancy database.
	 */
	public String insertDiscrepanciesIntoDB(JSONArray listOfStoriesWithDiscrepancy,RallyInspectorConnector rallyInspectorConnector, ApplicationContext context,RallyInspectorPropertiesReaderBean rallyInspectorProperties){
		
		String saveDiscrepancyListInvocationUrl = rallyInspectorProperties.getServerUri().concat(rallyInspectorProperties.getSaveListOfDiscrepancies());
		String webResourceType = rallyInspectorProperties.getWebresourceType();
		
		JSONArray discrepancyReports = createDiscrepancyTablePopulator(listOfStoriesWithDiscrepancy);
		String result = rallyInspectorConnector.postData(discrepancyReports, webResourceType, saveDiscrepancyListInvocationUrl);
		return result;
	}

	/**
	 * @param inputs - a list of stories without features retrieved by hitting Query Rally REST service end point.
	 * @return discrepancyReports - a list of JSONObjects, each of which holds information for one user story without feature.
	 * 
	 * Populates a list of stories without feature to insert into discrepancy database.
	 */
	public JSONArray createDiscrepancyTablePopulator(JSONArray listOfStoriesWithDiscrepancy) {	
		
		JSONArray discrepancyReports = new JSONArray();
		int listOfStoriesWithDiscrepancyCount = listOfStoriesWithDiscrepancy.length();
		try {
			JSONObject discrepancyType = new JSONObject();
			discrepancyType.put("id", 1);
			
			for (int i = 0; i < listOfStoriesWithDiscrepancyCount; i++) {

				JSONObject userStory = listOfStoriesWithDiscrepancy.getJSONObject(i);

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
