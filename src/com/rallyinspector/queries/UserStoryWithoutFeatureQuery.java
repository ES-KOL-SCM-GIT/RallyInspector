package com.rallyinspector.queries;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.rallyinpsector.discrepancy.DiscrepancyReportPopulator;
import com.rallyinspector.connector.RallyInspectorConnector;
import com.rallyinspector.util.RallyInspectorApplicationConfiguration;
import com.rallyinspector.util.RallyInspectorPropertiesReaderBean;

public class UserStoryWithoutFeatureQuery {

	final static Logger logger = Logger.getLogger(UserStoryWithoutFeatureQuery.class);

	RallyInspectorConnector restClientConnector = new RallyInspectorConnector();
	DiscrepancyReportPopulator discrepancyReportPopulator = new DiscrepancyReportPopulator();

	// System.out.println(rallyInspectorProperties.getServerUri());
	// method for invoking RallyInspectorConnector.java
	public void createQueryForPost() {

		@SuppressWarnings("resource")
		ApplicationContext context = new AnnotationConfigApplicationContext(
				RallyInspectorApplicationConfiguration.class);
		RallyInspectorPropertiesReaderBean rallyInspectorProperties = (RallyInspectorPropertiesReaderBean) context
				.getBean("rallyInspectorPropertiesReader");

		String queryRallyInvocationUrl = rallyInspectorProperties.getServerUri().concat(rallyInspectorProperties.getQueryRally());
		String webResourceType = rallyInspectorProperties.getWebresourceType();
		String saveDiscrepancyListInvocationUrl = rallyInspectorProperties.getServerUri().concat(rallyInspectorProperties.getSaveListOfDiscrepancies());
		try {

			// Creates the object used to invoke queryrally service.
			JSONObject finalJsonObject = new JSONObject();
			finalJsonObject.put("queryReqType", "HierarchicalRequirement");			
			finalJsonObject.put("queryReqFetch", "FormattedID,Name,_ref,Project");
			
			JSONObject finalQueryFilterObj = buildQueryFilterObject();
			//logger.debug("Query Filter Object is : " + finalQueryFilterObj);
			
			finalJsonObject.put("queryReqFilter", finalQueryFilterObj);			
			finalJsonObject.put("queryReqWorkspaceRef",
					"https://rally1.rallydev.com/slm/webservice/v2.0/workspace/1089940337");

			//logger.debug("Final JSON Object is : " + finalJsonObject);

			JSONObject resultJsonObject = restClientConnector.postData(finalJsonObject, webResourceType, queryRallyInvocationUrl);
			JSONArray resultJsonArray = resultJsonObject.getJSONArray("response");
			
			JSONArray discrepancyReportArray = discrepancyReportPopulator.createDiscrepancyTablePopulatorObject(resultJsonArray);
			String result = restClientConnector.postData(discrepancyReportArray, webResourceType, saveDiscrepancyListInvocationUrl);
			logger.info("Output from the service is: " + result);
			/*logger.info("Output from the service is: ");
			for (int i = 0; i < resultJsonArray.length(); i++) {
				JSONObject userStory = resultJsonArray.getJSONObject(i);
				//logger.info(userStory.get("Project"));
				//logger.info("jsonObject " + i + ": " + userStory.getString("FormattedID") + ": "
						//+ userStory.getString("Name") );
			}*/

		} catch (JSONException e) {
			logger.error("Problem creating query", e);
		} catch (NumberFormatException e) {
			logger.error("Problem invoking connector", e);
		}
	}

	/**
	 * Build up query filter object for User Story without Feature
	 * 
	 * @return
	 * @throws JSONException
	 */
	private JSONObject buildQueryFilterObject() throws JSONException {
		// Relation 1 - For Schedule Filter
		JSONArray scheduleStateQueryFilterArray = new JSONArray();

		JSONObject scheduleStateQueryFilterInProg = new JSONObject();
		scheduleStateQueryFilterInProg = new JSONObject();
		scheduleStateQueryFilterInProg.put("field", "ScheduleState");
		scheduleStateQueryFilterInProg.put("operator", "=");
		scheduleStateQueryFilterInProg.put("value", "In-Progress");
		scheduleStateQueryFilterArray.put(scheduleStateQueryFilterInProg);

		JSONObject scheduleStateQueryFilterComp = new JSONObject();
		scheduleStateQueryFilterComp = new JSONObject();
		scheduleStateQueryFilterComp.put("field", "ScheduleState");
		scheduleStateQueryFilterComp.put("operator", "=");
		scheduleStateQueryFilterComp.put("value", "Completed");
		scheduleStateQueryFilterArray.put(scheduleStateQueryFilterComp);

		JSONObject scheduleStateQueryFilterAcc = new JSONObject();
		scheduleStateQueryFilterAcc = new JSONObject();
		scheduleStateQueryFilterAcc.put("field", "ScheduleState");
		scheduleStateQueryFilterAcc.put("operator", "=");
		scheduleStateQueryFilterAcc.put("value", "Accepted");
		scheduleStateQueryFilterArray.put(scheduleStateQueryFilterAcc);

		JSONObject scheduleStateRelationObject = new JSONObject();
		scheduleStateRelationObject.put("queryFilters", scheduleStateQueryFilterArray);
		scheduleStateRelationObject.put("relationType", false);

		// Relation 2 - For Feature and Date
		JSONArray fetureDateQueryFilterArray = new JSONArray();

		JSONObject fetureQueryFilter = new JSONObject();
		fetureQueryFilter.put("field", "Feature");
		fetureQueryFilter.put("operator", "=");
		fetureQueryFilter.put("value", "null");
		fetureDateQueryFilterArray.put(fetureQueryFilter);

		JSONObject creationDateQueryFilter = new JSONObject();
		creationDateQueryFilter.put("field", "CreationDate");
		creationDateQueryFilter.put("operator", "=");
		creationDateQueryFilter.put("value", "today-90");
		fetureDateQueryFilterArray.put(creationDateQueryFilter);

		JSONObject fetureDateRelationObject = new JSONObject();
		fetureDateRelationObject.put("queryFilters", fetureDateQueryFilterArray);
		fetureDateRelationObject.put("relationType", true);

		// Final Query Filter build up
		JSONArray finalRelationArray = new JSONArray();
		finalRelationArray.put(scheduleStateRelationObject);
		finalRelationArray.put(fetureDateRelationObject);

		JSONObject finalQueryFilterObj = new JSONObject();
		finalQueryFilterObj.put("relations", finalRelationArray);
		finalQueryFilterObj.put("relationType", true);
		return finalQueryFilterObj;
	}
}
