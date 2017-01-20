package de.iteconomics.confluence.plugins.cron.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;

import de.iteconomics.confluence.plugins.cron.exceptions.JobException;

final class CronJobRunner implements JobRunner {

	private static Logger logger = LoggerFactory.getLogger(CronJobRunner.class);

	@Override
	public JobRunnerResponse runJob(JobRunnerRequest request) {
		Map<String, Serializable> parameters = request.getJobConfig().getParameters();

		String urlString = (String) parameters.get("url");
		String httpMethod = (String) parameters.get("method");

		URL url = null;
		HttpURLConnection conn = null;

		try {
			url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(httpMethod);
			conn.setRequestProperty("Accept", "text/plain");
		} catch (MalformedURLException e) {
			logger.error("Could not process request. URL is invalid");
			throw new JobException("Could not execute job, invalid url: " + urlString);
		} catch (IOException e) {
			logger.error("Connection failed to url: " + urlString);
			throw new JobException("Could not connect to endpoint");
		}

		conn.disconnect();

		return null;
	}
}