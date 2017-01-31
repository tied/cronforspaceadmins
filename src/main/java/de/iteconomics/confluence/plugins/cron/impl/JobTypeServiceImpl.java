package de.iteconomics.confluence.plugins.cron.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.Lists;

import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.JobType;
import de.iteconomics.confluence.plugins.cron.exceptions.JobTypeException;
import net.java.ao.Query;


@Named
public class JobTypeServiceImpl implements JobTypeService {

	@ComponentImport
	private ActiveObjects ao;

	private static Logger logger = LoggerFactory.getLogger(JobTypeServiceImpl.class);

	@Inject
	@Override
	public void setAo(ActiveObjects ao) {
		this.ao = ao;
	}

	@Override
	public List<JobType> getAllJobTypes() {
		return Lists.newArrayList(ao.find(JobType.class));
	}

	@Override
	public void createJobType(HttpServletRequest request) {
		checkNewJobTypeName(request);
		JobType jobType = ao.create(JobType.class);
		setJobTypeValues(jobType, request);
		jobType.save();
	}

	private void setJobTypeValues(JobType jobType, HttpServletRequest request) {
		String name = request.getParameter("name").trim();
		String url = request.getParameter("url").trim();
		if (url.charAt(url.length() -1) == '/') {
			url = url.substring(0, url.length() -1);
		}
		if (url.contains(" ")) {
			throw new JobTypeException("Invalid URL: " + url + ". URLs must not contain whitespace.");
		}
		String parameterNames = request.getParameter("parameters").trim();
		String httpMethod = request.getParameter("http-method").trim();
		checkIsValidMethod(httpMethod);
		String allParameters = getAllParameters(url, parameterNames);
		assertNoDuplicateParameterNames(allParameters);

		jobType.setName(name);
		jobType.setHttpMethod(httpMethod);
		jobType.setParameterNames(allParameters);
		jobType.setUrl(url);
	}

	private void checkIsValidMethod(String httpMethod) {
		if (httpMethod.equals("GET") ||
				httpMethod.equals("GET") ||
				httpMethod.equals("GET") ||
				httpMethod.equals("GET")
			)
		{
			return;
		}

		throw new JobTypeException("Invalid http method: " + httpMethod + ". Only GET, POST, PUT, and DELETE are allowed.");
	}

	private String getAllParameters(String url, String parameterNames) {
		List<String> pathParameters = new ArrayList<>();
		Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(url);
		while (m.find()) {
			String param = m.group().substring(1, m.group().length() -1);
			pathParameters.add(param);
		}

		String allParameters = parameterNames;
		for (String pathParameter: pathParameters) {
			logger.error("adding path parameter: " + pathParameter);
			allParameters += System.getProperty("line.separator");
			allParameters += pathParameter;
		}

		logger.error("all parameters: " + allParameters);

		return allParameters;
	}

	private void assertNoDuplicateParameterNames(String parameterNames) {

		List<String> parameters = Arrays.asList(parameterNames.split(System.getProperty("line.separator")));

		if (parameters.size() != new HashSet<String>(parameters).size()) {
			throw new JobTypeException("All parameter names must be unique across all kinds of parameters");
		}

	}

	private void checkNewJobTypeName(HttpServletRequest request) {
		String name = request.getParameter("name").trim();

		JobType[] jobTypes = ao.find(JobType.class, Query.select().where("name = ?", name));

		if (jobTypes.length > 0) {
			throw new JobTypeException("Cannot create: job type with name " + name + "already exists.");
		}
	}

	@Override
	public void deleteJobType(HttpServletRequest request) {
		JobType jobType = getJobTypeIfExists(request);
		ao.delete(jobType);
	}

	private JobType getJobTypeIfExists(HttpServletRequest request) {
		String id = request.getParameter("id");
		if (id == null) {
			id = "";
		} else {
			id = id.trim();
		}
		JobType[] jobTypes = ao.find(JobType.class, Query.select().where("id = ?", id));
		if (jobTypes.length != 1) {
			throw new JobTypeException("Cannot delete: job type with id" + id + " does not exist.");
		}
		return jobTypes[0];
	}

	@Override
	public JobType getJobTypeByID(String id) {
		checkValidID(id);

		return getJobTypeIfExists(id);
	}

	private void checkValidID(String id) {
		try {
			Integer.parseInt(id);
		} catch (NumberFormatException e ) {
			throw new JobTypeException("Cannot parse job type id to int: " + id);
		}
	}

	private JobType getJobTypeIfExists(String id) {
		JobType[] matches = ao.find(JobType.class, Query.select().where("ID = ?", id));

		if (matches.length != 1) {
			throw new JobTypeException("Cannot get job type: job type with id " + id + " does not exist.");
		}

		return matches[0];
	}

	@Override
	public void updateJobType(HttpServletRequest request) {
		JobType jobType = getJobTypeIfExists(request);
		setJobTypeValues(jobType, request);
		jobType.save();
	}
}