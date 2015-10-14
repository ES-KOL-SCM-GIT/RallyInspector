package com.rallyinspector.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@ComponentScan(basePackages = { "com.rallyinspector.*" })
@PropertySource("classpath:application.properties")
public class RallyInspectorApplicationConfiguration {
		
	@Autowired
	private Environment env;
	
	@Bean(name="rallyInspectorPropertiesReader")
	public RallyInspectorPropertiesReaderBean getRallyInspectorProperties(){
		
		RallyInspectorPropertiesReaderBean rallyInspectorProperties = new RallyInspectorPropertiesReaderBean();
		rallyInspectorProperties.setServerUri(env.getProperty("SERVER_URI"));
		rallyInspectorProperties.setResponseOk(env.getProperty("HTTP_STATUS_OK"));
		rallyInspectorProperties.setWebresourceType(env.getProperty("WEBRESOURCE_TYPE_JSON"));
		
		rallyInspectorProperties.setCheckQueryFilter(env.getProperty("CHECK_QUERY_FILTER"));
		rallyInspectorProperties.setQueryRally(env.getProperty("QUERY_RALLY"));
		rallyInspectorProperties.setSaveListOfDiscrepancies(env.getProperty("SAVE_LIST_OF_DISCREPANCY_REPORTS"));
		
		return rallyInspectorProperties;
	}
}
