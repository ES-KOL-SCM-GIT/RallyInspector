package com.rallyinspector.queries;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.rallyinspector.connector.RallyInspectorConnector;
import com.rallyinspector.discrepancy.StoryWithoutFeatureDiscrepancyReportPopulator;
import com.rallyinspector.util.RallyInspectorApplicationConfiguration;
import com.rallyinspector.util.RallyInspectorPropertiesReaderBean;

public class UserStoryWithoutFeatureQuery {

	final static Logger logger = Logger.getLogger(UserStoryWithoutFeatureQuery.class);

	RallyInspectorConnector rallyInspectorConnector = new RallyInspectorConnector();
	StoryWithoutFeatureDiscrepancyReportPopulator discrepancyReportPopulator = new StoryWithoutFeatureDiscrepancyReportPopulator();
	
	ApplicationContext context = new AnnotationConfigApplicationContext(
			RallyInspectorApplicationConfiguration.class);
	RallyInspectorPropertiesReaderBean rallyInspectorProperties = (RallyInspectorPropertiesReaderBean) context
			.getBean("rallyInspectorPropertiesReader");
	
	String webResourceType = rallyInspectorProperties.getWebresourceType();

	/**
	 * Builds the final query filter used to invoke Query Rally REST service. Invokes said REST service. 
	 */
	public void createStoryWithoutFeatureQuery() {
		
		String queryRallyInvocationUrl = rallyInspectorProperties.getServerUri().concat(rallyInspectorProperties.getQueryRally());		
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
			JSONArray results = resultJson.getJSONArray("response");
			
			String result = insertDiscrepanciesIntoDB(results);			
			logger.info("Output from the service is: " + result);

		} catch (JSONException e) {
			logger.error("Problem creating query", e);
		} catch (NumberFormatException e) {
			logger.error("Problem invoking connector", e);
		}
	}
	
	/**
	 * @param inputs - response object (JSONArray) containing list of all user stories without feature.
	 * @return
	 * 
	 * Invokes Save Discrepancy List REST service to insert records into discrepancy database.
	 */
	public String insertDiscrepanciesIntoDB(JSONArray inputs){
		
		String saveDiscrepancyListInvocationUrl = rallyInspectorProperties.getServerUri().concat(rallyInspectorProperties.getSaveListOfDiscrepancies());
		
		JSONArray discrepancyReports = discrepancyReportPopulator.createDiscrepancyTablePopulator(inputs);
		String result = rallyInspectorConnector.postData(discrepancyReports, webResourceType, saveDiscrepancyListInvocationUrl);
		return result;
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
}
