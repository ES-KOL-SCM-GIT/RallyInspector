package com.es.epm;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class RestClientBL {

	// These will be read from application.properties file.
	public static final String WEBRESOURCE_TYPE_JSON = "application/json";
	public static final String SERVER_URI = "http://jarvissaibal:8080/ConnectorFramework/service";
	public static final String CHECK_QUERY_FILTER = "/rallyrestws/checkQueryFilter";
	public static final String QUERY_RALLY = "/rallyrestws/queryrally";

	// Json Objects used
	JSONObject finalJsonObject = new JSONObject();// Object used to invoke
													// queryrally service.
	JSONObject jsonObject = new JSONObject();// Object used to hold constructed
												// queries.
	JSONArray relation = new JSONArray();// Object used to hold list of query
											// filters.
	JSONObject queryFilterJsonObject = new JSONObject();// Object used to
														// construct individual
														// queries.

	RestClientConnector restClientConnector = new RestClientConnector();

	// method for invoking RestClientConnector.java
	public void createQueryForPost() {
		String invocationUrl = SERVER_URI + QUERY_RALLY;

		try {
			// Creates the first filter and stores it in the array.
			queryFilterJsonObject.put("field", "Feature");
			queryFilterJsonObject.put("operator", "=");
			queryFilterJsonObject.put("value", "null");
			relation.put(queryFilterJsonObject);

			// Creates the second filter and stores it in the array.
			queryFilterJsonObject = new JSONObject();
			queryFilterJsonObject.put("field", "ScheduleState");
			queryFilterJsonObject.put("operator", "=");
			queryFilterJsonObject.put("value", "In-Progress");
			relation.put(queryFilterJsonObject);

			// Creates the third filter and stores it in the array.
			queryFilterJsonObject = new JSONObject();
			queryFilterJsonObject.put("field", "CreationDate");
			queryFilterJsonObject.put("operator", "=");
			queryFilterJsonObject.put("value", "today-90");
			relation.put(queryFilterJsonObject);

			// Stores the final array in the object passed as "value" for
			// queryReqFilter
			jsonObject.put("queryFilters", relation);
			jsonObject.put("relationType", true);

			// Creates the object used to invoke queryrally service.
			finalJsonObject.put("queryReqType", "HierarchicalRequirement");
			finalJsonObject.put("queryReqFetch", "FormattedID");
			finalJsonObject.put("queryReqFilter", jsonObject);
			finalJsonObject.put("queryReqWorkspaceRef",
					"https://rally1.rallydev.com/slm/webservice/v2.0/workspace/1089940337");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		restClientConnector.postRallyQueryData(finalJsonObject, WEBRESOURCE_TYPE_JSON, invocationUrl);
	}
}
/*
 * Calendar calendar = Calendar.getInstance(); calendar.add(Calendar.MONTH,-3);
 * String dateValue = calendar.getTime().toString();
 */
/*
 * public JSONObject createQueryFilter(){ try {
 * 
 * 
 * 
 * } catch (JSONException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } return queryFilterJsonObject; }
 */