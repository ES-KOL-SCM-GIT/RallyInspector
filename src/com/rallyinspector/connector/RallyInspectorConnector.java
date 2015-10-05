package com.rallyinspector.connector;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.rallyinspector.util.RallyInspectorApplicationConfiguration;
import com.rallyinspector.util.RallyInspectorPropertiesReaderBean;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class RallyInspectorConnector {
	
	final static Logger logger = Logger.getLogger(RallyInspectorConnector.class);

	private final Client client = Client.create();

	//RallyInspectorPropertiesReaderBean rallyInspectorPropertiesReader = new RallyInspectorPropertiesReaderBean();

	/*
	 * Method used to invoke queryrally service. Takes JSONObject, Url and web
	 * resource type created in UserStoryWithoutFeatureQuery.java as parameters.
	 * rallyInspectorPropertiesReader.getHttpStatus())
	 */
	/*public void postData(JSONObject finalJsonObject, String webResourceType, String Url)
			throws NumberFormatException, IOException {
		
	}*/

	public void postData(JSONObject finalJsonObject, String webresourceType, String invocationUrl) {
		
		@SuppressWarnings("resource")
		ApplicationContext context = new AnnotationConfigApplicationContext(RallyInspectorApplicationConfiguration.class);
		RallyInspectorPropertiesReaderBean rallyInspectorProperties =  (RallyInspectorPropertiesReaderBean)context.getBean("rallyInspectorPropertiesReader");
		
		WebResource webResource = client.resource(invocationUrl);
		ClientResponse response = webResource.type(webresourceType).post(ClientResponse.class, finalJsonObject);
		if (response.getStatus()!= Integer.parseInt(rallyInspectorProperties.getResponseOk())) {
			throw new RuntimeException("HTTP Error: " + response.getStatus());
		}
		JSONObject resultJsonObject = response.getEntity(JSONObject.class);
		try {
			JSONArray resultJsonArray = resultJsonObject.getJSONArray("response");
			//System.out.println("Response from the Server: ");
			logger.info("Output from the service is:");
			for (int i = 0; i < resultJsonArray.length(); i++) {
				JSONObject userStory = resultJsonArray.getJSONObject(i);
				//System.out.println("jsonObject " + i + ": " + userStory.getString("FormattedID") + ": " + userStory.getString("Name"));
				logger.info("jsonObject " + i + ": " + userStory.getString("FormattedID") + ": " + userStory.getString("Name"));
			}
		} catch (JSONException e) {
			//e.printStackTrace();
			logger.error("Problem in Connector",e);
		}
		
	}

	/*int responseCode = response.getStatus();
	System.out.println(responseCode);
	int test= Integer.parseInt(env.getProperty("HTTP_STATUS_OK"));
	System.out.println(test);*/
	// Add similar methods to fire get and delete operations
}
