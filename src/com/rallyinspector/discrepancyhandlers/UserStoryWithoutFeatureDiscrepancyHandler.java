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

	/**
	 * This method is the hook via which the scheduler will be able to access
	 * the methods which actually handle the discrepancy. It performs several
	 * generic activities,such as initializing an instance of the connector,and
	 * initializing the properties file reader. All calls to business logic
	 * methods are performed from inside this method.
	 */
	public void handleDiscrepancy() {
		RallyInspectorConnector rallyInspectorConnector = new RallyInspectorConnector();
		ApplicationContext context = new AnnotationConfigApplicationContext(
				RallyInspectorApplicationConfiguration.class);
		RallyInspectorPropertiesReaderBean rallyInspectorProperties = (RallyInspectorPropertiesReaderBean) context
				.getBean("rallyInspectorPropertiesReader");

		JSONArray listOfStoriesWithDiscrepancy = invokeStoryWithoutFeatureQuery(rallyInspectorConnector, context,
				rallyInspectorProperties);
		String responseAfterDbInsert = insertDiscrepanciesIntoDB(listOfStoriesWithDiscrepancy, rallyInspectorConnector,
				context, rallyInspectorProperties);
		logger.info("Output from the service is: " + responseAfterDbInsert);
	}

	/**
	 * This method returns a JSONArray holding all the stories conforming to the
	 * current discrepancy type. The ApplicationContext &
	 * RallyInspectorPropertiesReaderBean parameters allow the method to create
	 * the required invocation url inside.
	 * 
	 * @param rallyInspectorConnector
	 * @param context
	 * @param rallyInspectorProperties
	 * @return
	 */
	private JSONArray invokeStoryWithoutFeatureQuery(RallyInspectorConnector rallyInspectorConnector,
			ApplicationContext context, RallyInspectorPropertiesReaderBean rallyInspectorProperties) {

		String queryRallyInvocationUrl = rallyInspectorProperties.getServerUri()
				.concat(rallyInspectorProperties.getQueryRally());
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

			JSONObject resultJson = rallyInspectorConnector.postData(finalJson, webResourceType,
					queryRallyInvocationUrl);
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
	 * This method returns a response String from the Save Discrepancy List REST
	 * service after stories conforming to the discrepancy have been persisted
	 * to database. The JSONArray parameter holds the list of all stories
	 * conforming to the discrepancy being handled.
	 * 
	 * @param listOfStoriesWithDiscrepancy
	 * @param rallyInspectorConnector
	 * @param context
	 * @param rallyInspectorProperties
	 * @return
	 */
	private String insertDiscrepanciesIntoDB(JSONArray listOfStoriesWithDiscrepancy,
			RallyInspectorConnector rallyInspectorConnector, ApplicationContext context,
			RallyInspectorPropertiesReaderBean rallyInspectorProperties) {

		String saveDiscrepancyListInvocationUrl = rallyInspectorProperties.getServerUri()
				.concat(rallyInspectorProperties.getSaveListOfDiscrepancies());
		String webResourceType = rallyInspectorProperties.getWebresourceType();

		JSONArray discrepancyReports = createDiscrepancyTablePopulator(listOfStoriesWithDiscrepancy);
		String result = rallyInspectorConnector.postData(discrepancyReports, webResourceType,
				saveDiscrepancyListInvocationUrl);
		return result;
	}

	/**
	 * This method returns a list of Discrepancy Report objects to be persisted
	 * to database. The Discrepancy Report table has a Foreign key relationship
	 * with the Discrepancy Type table,in the form of Discrepancy Type ID.
	 * Therefore, while creating the Report object, it is required to pass in
	 * the Discrepancy Type object with only the specific Type id populated. The
	 * REST service automatically looks up the Discrepancy Type ID while
	 * inserting records into the Reports table.
	 * 
	 * @param listOfStoriesWithDiscrepancy
	 * @return
	 */
	private JSONArray createDiscrepancyTablePopulator(JSONArray listOfStoriesWithDiscrepancy) {

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
