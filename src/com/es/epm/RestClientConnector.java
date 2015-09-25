package com.es.epm;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestClientConnector {
	
	public static final int HTTP_STATUS_OK = 200;

	private final Client client = Client.create();

	/*
	 * Method used to invoke queryrally service. Takes JSONObject, Url and web
	 * resource type created in RestClientBL.java as parameters.
	 */
	public void postRallyQueryData(JSONObject finalJsonObject, String webResourceType, String Url) {
		WebResource webResource = client.resource(Url);
		ClientResponse response = webResource.type(webResourceType).post(ClientResponse.class, finalJsonObject);
		if (response.getStatus() != HTTP_STATUS_OK) {
			throw new RuntimeException("HTTP Error: " + response.getStatus());
		}
		String result = response.getEntity(String.class);
		try {
			JSONObject resultJsonObject = new JSONObject(result);
			JSONArray resultJsonArray = resultJsonObject.getJSONArray("response");
			int count = resultJsonArray.length();
			System.out.println("Response from the Server: ");
			System.out.println(count);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// Add similar methods to fire get and delete operations
}
