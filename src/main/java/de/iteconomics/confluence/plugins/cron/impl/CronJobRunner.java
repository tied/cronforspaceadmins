package de.iteconomics.confluence.plugins.cron.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MediaType;

import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class CronJobRunner implements JobRunner {

	private static Logger logger = LoggerFactory.getLogger(CronJobRunner.class);

	private String getJsonFromQueryString(String queryString) {
		String[] keyValuePairs = queryString.split("&");
		JsonObjectBuilder builder = Json.createBuilderFactory(new HashMap<String, Object>()).createObjectBuilder();
		for (String keyValuePair: keyValuePairs) {
			String key = keyValuePair.split("=")[0];
			String name = keyValuePair.split("=")[1];
			builder.add(key, name);
		}

		return builder.build().toString();
	}

	@Override
	public JobRunnerResponse runJob(JobRunnerRequest request) {
		Map<String, Serializable> parameters = request.getJobConfig().getParameters();
		String urlString = (String) parameters.get("url");

		String queryString = (String) parameters.get("queryString");
		String requestBody = "";

		String httpMethod = (String) parameters.get("method");
		if (httpMethod.equals("GET")) {
			urlString += "?";
			urlString += queryString;
		} else if (httpMethod.equals("POST")) {
			requestBody = queryString;
		}

		Client client = Client.create();
		logger.error("http method: " + httpMethod);
		logger.error("url: " + urlString);
		WebResource webResource = client.resource(urlString);

		if (!requestBody.equals("")) {
			String response = webResource
				.accept(MediaType.MEDIA_TYPE_WILDCARD)
				.type(MediaType.APPLICATION_JSON)
				.post(String.class, getJsonFromQueryString(queryString));
		} else {
			String response = webResource.accept(MediaType.MEDIA_TYPE_WILDCARD)
				.get(String.class);
		}

		return null;
	}
}