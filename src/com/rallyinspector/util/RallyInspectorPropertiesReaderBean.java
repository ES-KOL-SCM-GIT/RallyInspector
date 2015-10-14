package com.rallyinspector.util;

public class RallyInspectorPropertiesReaderBean {
	
	private String serverUri;
	private String responseOk;
	private String webresourceType;
	private String checkQueryFilter;
	private String queryRally;
	private String saveListOfDiscrepancies;
	
	public String getServerUri() {
		return serverUri;
	}
	public void setServerUri(String serverUri) {
		this.serverUri = serverUri;
	}
	public String getResponseOk() {
		return responseOk;
	}
	public void setResponseOk(String responseOk) {
		this.responseOk = responseOk;
	}
	public String getWebresourceType() {
		return webresourceType;
	}
	public void setWebresourceType(String webresourceType) {
		this.webresourceType = webresourceType;
	}
	public String getCheckQueryFilter() {
		return checkQueryFilter;
	}
	public void setCheckQueryFilter(String checkQueryFilter) {
		this.checkQueryFilter = checkQueryFilter;
	}
	public String getQueryRally() {
		return queryRally;
	}
	public void setQueryRally(String queryRally) {
		this.queryRally = queryRally;
	}
	public String getSaveListOfDiscrepancies() {
		return saveListOfDiscrepancies;
	}
	public void setSaveListOfDiscrepancies(String saveListOfDiscrepancies) {
		this.saveListOfDiscrepancies = saveListOfDiscrepancies;
	}
}

/*InputStream inputStream;
String result = "";
String propertyFile = "application.properties";

public String getHttpStatus() throws IOException {
	try {
		Properties properties = new Properties();
		
		inputStream = getClass().getClassLoader().getResourceAsStream(propertyFile);
		if (inputStream != null) {
			properties.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propertyFile + "' not found in the classpath");
		}

		String responseCode = properties.getProperty("HTTP_STATUS_OK");
		result = responseCode;
	} catch (Exception e) {
		System.out.println("Exception: " + e);
	} finally {
		inputStream.close();
	}
	return result;
}*/
