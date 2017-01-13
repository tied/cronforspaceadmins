package de.iteconomics.confluence.plugins.cron.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;

final class CronJobRunner implements JobRunner {

	private static Logger logger = LoggerFactory.getLogger(CronJobRunner.class);

	@Override
	public JobRunnerResponse runJob(JobRunnerRequest request) {
		String urlString = (String) request.getJobConfig().getParameters().get("url");
		URL url = null;
		HttpURLConnection conn = null;

		try {
			url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
		} catch (MalformedURLException e) {
			logger.error("Could not process request. URL is invalid");
		} catch (IOException e) {
			logger.error("Connection failed to url: " + urlString);
		}

		conn.disconnect();

		return null;
	}
}