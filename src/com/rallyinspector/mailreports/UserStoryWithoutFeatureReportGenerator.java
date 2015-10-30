package com.rallyinspector.mailreports;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.rallyinspector.connector.RallyInspectorConnector;
import com.rallyinspector.util.RallyInspectorApplicationConfiguration;
import com.rallyinspector.util.RallyInspectorPropertiesReaderBean;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class UserStoryWithoutFeatureReportGenerator {

	final static Logger logger = Logger.getLogger(UserStoryWithoutFeatureReportGenerator.class);

	public void handleReportCreation() {
		RallyInspectorConnector rallyInspectorConnector = new RallyInspectorConnector();
		ApplicationContext context = new AnnotationConfigApplicationContext(
				RallyInspectorApplicationConfiguration.class);
		RallyInspectorPropertiesReaderBean rallyInspectorProperties = (RallyInspectorPropertiesReaderBean) context
				.getBean("rallyInspectorPropertiesReader");

		MultivaluedMap<String, String> finalQueryParams = buildQueryParams();
		JSONArray listOfStoriesWithDiscrepancy = getPersistedStoriesWithoutFeature(rallyInspectorConnector, context,
				rallyInspectorProperties, finalQueryParams);

		int listOfStoriesWithDiscrepancyCount = listOfStoriesWithDiscrepancy.length();
		Map<String, List<String>> discrepanciesByTeamMap = new HashMap<String, List<String>>();
		for (int i = 0; i < listOfStoriesWithDiscrepancyCount; i++) {
			try {
				String key = listOfStoriesWithDiscrepancy.getJSONObject(i).getString("teamName");
				if (discrepanciesByTeamMap.get(key) == null) {
					discrepanciesByTeamMap.put(key, new ArrayList<String>());
				}
				discrepanciesByTeamMap.get(key)
						.add(listOfStoriesWithDiscrepancy.getJSONObject(i).getString("artifactName"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		for (Map.Entry<String, List<String>> entry : discrepanciesByTeamMap.entrySet()) {
			composeAndSendMail(entry.getKey(), entry.getValue());
		}

	}

	private void composeAndSendMail(String team, List<String> artifactNames) {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", "mail.lexmark.com");
		properties.put("mail.smtp.port", "25");
		Session session = Session.getDefaultInstance(properties);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress("sayan.guha@lexmark.com"));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress("sayan.guha@lexmark.com"));
			message.setSubject("Discrepancy Report For Team: " + team);

			/*
			 * Multipart mp = new MimeMultipart(); MimeBodyPart htmlPart = new
			 * MimeBodyPart();
			 */
			StringBuilder sb = new StringBuilder();
			for (String s : artifactNames) {
				sb.append(s);
				sb.append("\t");
			}
			// logger.info(sb.toString());
			/*
			 * htmlPart.setContent("<table><tr><td></td></tr></table>",
			 * "text/html"); mp.addBodyPart(htmlPart);
			 */
			message.setText(sb.toString());
			// message.setContent(mp);

			Transport.send(message);
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

	private MultivaluedMap<String, String> buildQueryParams() {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date date = new Date();

		queryParams.add("date", dateFormat.format(date));
		return queryParams;
	}

	private JSONArray getPersistedStoriesWithoutFeature(RallyInspectorConnector rallyInspectorConnector,
			ApplicationContext context, RallyInspectorPropertiesReaderBean rallyInspectorProperties,
			MultivaluedMap<String, String> finalQueryParams) {

		String discrepancyReportByDateInvocationUrl = rallyInspectorProperties.getServerUri()
				.concat(rallyInspectorProperties.getGetDiscrepancyReportsByDate());
		JSONArray results = rallyInspectorConnector.getData(finalQueryParams, discrepancyReportByDateInvocationUrl);
		return results;
	}
}
