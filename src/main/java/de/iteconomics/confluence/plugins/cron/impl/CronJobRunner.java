package de.iteconomics.confluence.plugins.cron.impl;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import de.iteconomics.confluence.plugins.cron.exceptions.CronJobRunnerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class CronJobRunner implements JobRunner {

	private static Logger logger = LoggerFactory.getLogger(CronJobRunner.class);

	private String getJsonFromQueryString(String queryString) {
		if (queryString == null || queryString.equals("")) {
			return "";
		}
		String[] elements = queryString.split("&");
		JsonObjectBuilder builder = Json.createBuilderFactory(new HashMap<String, Object>()).createObjectBuilder();
		for (String element: elements) {
			String[] keyValuePair = element.split("=");
			if (keyValuePair.length != 2) {
				throw new CronJobRunnerException("Unable to run Job: invalid parameters string: " + queryString);
			}
			String key = keyValuePair[0];
			String name = keyValuePair[1];
			builder.add(key, name);
		}

		return builder.build().toString();
	}

	@Override
	public JobRunnerResponse runJob(JobRunnerRequest request) {
		Map<String, Serializable> parameters = request.getJobConfig().getParameters();
		String urlString = (String) parameters.get("url");
		String queryString = (String) parameters.get("queryString");
		if (queryString == null) {
			queryString = "";
		}
		String requestBody = "";
		String httpMethod = (String) parameters.get("method");
		String username = (String) parameters.get("username");
		String password = (String) parameters.get("password");

		if (httpMethod.equals("GET") || httpMethod.equals("DELETE")) {
			if (!queryString.equals("")) {
				try {
					urlString += "?";
					for (String keyValuePair: queryString.split("&")) {
						String key = keyValuePair.split("=")[0];
						String value = keyValuePair.split("=")[1];
						urlString += key;
						urlString += "=";
						urlString += URLEncoder.encode(value, "UTF-8");
						urlString += "&";
					}
					urlString = urlString.substring(0, urlString.length() - 1);
				} catch (UnsupportedEncodingException e) {
					logger.error("could url encode: " + queryString);;
				}
			}
		}

		Client client = Client.create();
		if (!username.equals("")) {
			client.addFilter(new HTTPBasicAuthFilter(username, password));
		}
		URI uri;
		try {
			uri = new URI(urlString);
		} catch (URISyntaxException e) {
			throw new CronJobRunnerException("Cannot run job. Invalid URI: " + urlString);
		}

		WebResource webResource = client.resource(uri);
		Builder builder = webResource.accept(MediaType.MEDIA_TYPE_WILDCARD);

		if (httpMethod.equals("POST") || httpMethod.equals("PUT")) {
			if (!queryString.equals("")) {
				// use substring() to remove the '?'
				requestBody = getJsonFromQueryString(queryString.substring(1));
				builder.type(MediaType.APPLICATION_JSON);
			}
		}

		String response = "";
		if (httpMethod.equals("GET")) {
			response = builder.get(String.class);
		} else if (httpMethod.equals("POST")) {
			if (requestBody.equals("")) {
				response = builder.post(String.class);
			} else {
				response = builder.post(String.class, requestBody);
			}
		} else if (httpMethod.equals("PUT")) {
			if (requestBody.equals("")) {
				response = builder.post(String.class);
			} else {
				response = builder.post(String.class, requestBody);
			}
		} else if (httpMethod.equals("DELETE")) {
			response = builder.get(String.class);
		} else {
			throw new CronJobRunnerException("Cannot run job. Unsupported http method: " +
					httpMethod + ". Only GET, POST, PUT and DELETE are supported.");
		}

		logger.error("response: " + response);

		return null;
	}
}