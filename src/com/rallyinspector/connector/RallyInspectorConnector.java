package com.rallyinspector.connector;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
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

	
	/**
	 * Overloaded method. Returns a JSONObject holding the result of the invocation of the REST service.
	 * @param input(JSONObject)
	 * @param webresourceType(String)
	 * @param invocationUrl(String)
	 * @return result (JSONObject)
	 * 
	 */
	public JSONObject postData(JSONObject input, String webresourceType, String invocationUrl) {

		@SuppressWarnings("resource")
		ApplicationContext context = new AnnotationConfigApplicationContext(
				RallyInspectorApplicationConfiguration.class);
		RallyInspectorPropertiesReaderBean rallyInspectorProperties = (RallyInspectorPropertiesReaderBean) context
				.getBean("rallyInspectorPropertiesReader");

		WebResource webResource = client.resource(invocationUrl);
		ClientResponse response = webResource.type(webresourceType).post(ClientResponse.class, input);
		if (response.getStatus() != Integer.parseInt(rallyInspectorProperties.getResponseOk())) {
			throw new RuntimeException("HTTP Error: " + response.getStatus());
		}
		JSONObject result = response.getEntity(JSONObject.class);

		return result;
	}

	
	/**
	 * Overloaded method. Returns a String holding the result of the invocation of the REST service.
	 * 
	 * @param inputs (JSONArray)
	 * @param webresourceType(String)
	 * @param invocationUrl (String)
	 * @return result (String)
	 */
	public String postData(JSONArray inputs, String webresourceType, String invocationUrl) {

		@SuppressWarnings("resource")
		ApplicationContext context = new AnnotationConfigApplicationContext(
				RallyInspectorApplicationConfiguration.class);
		RallyInspectorPropertiesReaderBean rallyInspectorProperties = (RallyInspectorPropertiesReaderBean) context
				.getBean("rallyInspectorPropertiesReader");

		WebResource webResource = client.resource(invocationUrl);
		ClientResponse response = webResource.type(webresourceType).post(ClientResponse.class, inputs);
		if (response.getStatus() != Integer.parseInt(rallyInspectorProperties.getResponseOk())) {
			throw new RuntimeException("HTTP Error: " + response.getStatus());
		}
		String result = response.getEntity(String.class);

		return result;
	}
	
	public JSONArray getData(MultivaluedMap<String,String> inputParams, String invocationUrl ){
		
		@SuppressWarnings("resource")
		ApplicationContext context = new AnnotationConfigApplicationContext(
				RallyInspectorApplicationConfiguration.class);
		RallyInspectorPropertiesReaderBean rallyInspectorProperties = (RallyInspectorPropertiesReaderBean) context
				.getBean("rallyInspectorPropertiesReader");

		WebResource webResource = client.resource(invocationUrl).queryParams(inputParams);
		ClientResponse response = webResource.get(ClientResponse.class);		
		if (response.getStatus() != Integer.parseInt(rallyInspectorProperties.getResponseOk())) {
			throw new RuntimeException("HTTP Error: " + response.getStatus());
		}
		
		JSONArray result = response.getEntity(JSONArray.class);
		
		return result;
	}
	// Add similar methods to fire get and delete operations
}
