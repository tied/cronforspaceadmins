package de.iteconomics.confluence.plugins.cron.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.Lists;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.Job;
import de.iteconomics.confluence.plugins.cron.entities.JobType;
import de.iteconomics.confluence.plugins.cron.entities.JobTypeParameter;
import de.iteconomics.confluence.plugins.cron.exceptions.JobTypeException;
import net.java.ao.Query;


@Named
public class JobTypeServiceImpl implements JobTypeService {

	@ComponentImport
	private ActiveObjects ao;
	private JobService jobService;
	private static Logger logger = LoggerFactory.getLogger(JobTypeServiceImpl.class);
	private static String namePrefix = "parameter-name-";

	@Inject
	@Override
	public void setAo(ActiveObjects ao) {
		this.ao = ao;
	}

	@Inject
	@Override
	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

	@Override
	public List<JobType> getAllJobTypes() {
		return Lists.newArrayList(ao.find(JobType.class));
	}

	@Override
	public void createJobType(HttpServletRequest request) {
		checkRequiredRequestParametersPresent(request);
		checkNewJobTypeName(request);
		JobType jobType = ao.create(JobType.class);
		setJobTypeValues(jobType, request);
	}

	@Override
	public void updateJobType(HttpServletRequest request) {
		checkRequiredRequestParametersPresent(request);
		JobType jobType = getJobTypeIfExists(request);
		
		boolean changeAffectsJobs = changeAffectsJobs(jobType, request);
		
		setJobTypeValues(jobType, request);
		List<Job> jobs = jobService.getJobsByJobTypeID(jobType.getID());
		
		if (changeAffectsJobs) {
			for (Job job: jobs) {
				job.setJobTypeChanged(true);
				job.save();
			}
		}
	}

	private boolean changeAffectsJobs(JobType jobType, HttpServletRequest request) {
		if (!jobType.getUrl().equals(request.getParameter("url"))) {
			return true;
		}
		if (!jobType.getHttpMethod().equals(request.getParameter("http-method"))) {
			return true;
		}
		if (jobType.isAuthenticationRequired() != (request.getParameter("authentication") != null)) {
			return true;
		}
		if (jobType.isAuthenticationRequired() && !jobType.getUsername().equals(request.getParameter("username"))) {
			return true;
		}
		if (jobType.isAuthenticationRequired() && !jobType.getPassword().equals(request.getParameter("password"))) {
			return true;
		}
		if (parameterNamesNotEqual(jobType, request)) {
			return true;
		}
		
		return false;
	}

	private boolean parameterNamesNotEqual(JobType jobType, HttpServletRequest request) {
		List<String> parameterNamesFromJobType = getParameterNamesFromJobType(jobType);
		List<String> parameterNamesFromRequest = getRequestParameterKeys(request);
		
		return !parameterNamesFromJobType.equals(parameterNamesFromRequest);
	}

	private List<String> getParameterNamesFromJobType(JobType jobType) {
		List<String> parameterNamesFromJobType = new ArrayList<>();
		for (JobTypeParameter jobTypeParameter: jobType.getParameters()) {
			parameterNamesFromJobType.add(jobTypeParameter.getName());
		}
		return parameterNamesFromJobType;
	}

	private void checkRequiredRequestParametersPresent(HttpServletRequest request) {
		for (String parameter: new String[] {"url", "name", "http-method"}) {
			if (request.getParameter(parameter) == null || "".equals(request.getParameter(parameter.trim()))) {
				throw new JobTypeException("Cannot create job type: " + parameter + " may not be null");
			}
		}
	}

	private void setJobTypeValues(JobType jobType, HttpServletRequest request) {
		String name = request.getParameter("name");
		if (name == null) {
			throw new JobTypeException("The name of the job type may not be null.");
		}
		jobType.setName(name);

		String description = request.getParameter("description");
		if (description == null) {
			description = "";
		}
		jobType.setDescription(description);

		String url = getUrl(request);
		jobType.setUrl(url);

		setParameters(jobType, request, url);

		setHttpMethod(jobType, request);

		boolean authenticationRequired = (request.getParameter("authentication") != null);
		jobType.setAuthenticationRequired(authenticationRequired);

		String bundledJobTypeId = request.getParameter("bundled-job-type-id");
		jobType.setBundledJobTypeID(bundledJobTypeId);

		String username = getUsername(request);
		jobType.setUsername(username);

		String password = getPassword(request);
		jobType.setPassword(password);

		jobType.save();
	}

	private String getPassword(HttpServletRequest request) {
		String password = request.getParameter("password");
		if (password== null) {
			password = "";
		} else {
			password = password.trim();
		}
		return password;
	}

	private String getUsername(HttpServletRequest request) {
		String username = request.getParameter("username");
		if (username == null) {
			username = "";
		} else {
			username = username.trim();
		}
		return username;
	}

	private void setHttpMethod(JobType jobType, HttpServletRequest request) {
		String httpMethod = request.getParameter("http-method").trim();
		checkIsValidMethod(httpMethod);
		jobType.setHttpMethod(httpMethod);
	}

	private String getUrl(HttpServletRequest request) {
		String url = request.getParameter("url").trim();
		if (url.charAt(url.length() -1) == '/') {
			url = url.substring(0, url.length() -1);
		}
		if (url.contains(" ")) {
			throw new JobTypeException("Invalid URL: " + url + ". URLs must not contain whitespace.");
		}
		return url;
	}

	private void setParameters(JobType jobType, HttpServletRequest request, String url) {

		JobTypeParameter[] oldParameters = jobType.getParameters();

		for (JobTypeParameter oldParameter: oldParameters) {
			ao.delete(oldParameter);
		}

		
		List<String> parameterNameKeys = getRequestParameterKeys(request); 

		for (String parameterNameKey: parameterNameKeys) {
			JobTypeParameter jobTypeParameter = ao.create(JobTypeParameter.class);
			String parameterName = request.getParameter(parameterNameKey);
			if (parameterName == null) {
				throw new JobTypeException("Parameter name with key " + parameterNameKey + " was null.");
			}
			jobTypeParameter.setName(parameterName.trim());

			
			String parameterNumber = parameterNameKey.substring(namePrefix.length());
			String parameterFriendlyName = request.getParameter("parameter-friendly-name-" + parameterNumber);
			String parameterDescription = request.getParameter("parameter-description-" + parameterNumber);
			boolean isPathParameter = request.getParameter("parameter-path-parameter-" + parameterNumber) != null;

			if (parameterFriendlyName != null && !parameterFriendlyName.trim().equals("")) {
				jobTypeParameter.setFriendlyName(parameterFriendlyName.trim());
			} else {
				jobTypeParameter.setFriendlyName(parameterName.trim());
			}
			if (parameterDescription != null) {
				jobTypeParameter.setDescription(parameterDescription.trim());
			} else {
				jobTypeParameter.setDescription("");
			}
			jobTypeParameter.setPathParameter(isPathParameter);

			jobTypeParameter.setJobType(jobType);
			jobTypeParameter.save();
		}

	}

	private List<String> getRequestParameterKeys(HttpServletRequest request) {
		List<String> requestParameterKeys = Collections.list(request.getParameterNames());
		List<String> parameterNameKeys = new ArrayList<>();

		for (String key: requestParameterKeys) {
			if (key.startsWith(namePrefix)) {
				parameterNameKeys.add(key);
			}
		}
		return parameterNameKeys;
	}

	private void checkIsValidMethod(String httpMethod) {
		if (httpMethod.equals("GET") ||
				httpMethod.equals("POST") ||
				httpMethod.equals("PUT") ||
				httpMethod.equals("DELETE")
			)
		{
			return;
		}

		throw new JobTypeException("Invalid http method: " + httpMethod + ". Only GET, POST, PUT, and DELETE are allowed.");
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
		List<Job> jobs = jobService.getJobsByJobTypeID(jobType.getID());
		for (Job job: jobs) {
			jobService.unregisterJob(job);
		}
		JobTypeParameter[] parameters = jobType.getParameters();
		for (JobTypeParameter parameter: parameters) {
			ao.delete(parameter);
		}
		ao.delete(jobType);
	}

	private JobType getJobTypeIfExists(HttpServletRequest request) {
		String id = request.getParameter("id");
		if (id == null) {
			id = "";
		} else {
			id = id.trim();
		}

		if (!validId(id)) {
			throw new JobTypeException("Job type not found: id " + id + " is not valid.");
		}
		JobType[] jobTypes = ao.find(JobType.class, Query.select().where("id = ?", id));
		if (jobTypes.length != 1) {
			throw new JobTypeException("Job type not found: job type with id" + id + " does not exist.");
		}
		return jobTypes[0];
	}

	private boolean validId(String id) {
		try {
			Integer.parseInt(id);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	@Override
	public JobType getJobTypeByID(String id) {
		checkValidID(id);

		return getJobTypeIfExists(id);
	}

	@Override
	public boolean hasNotificationJobType() {
		JobType notificationJobType = getNotificationJobType();

		return notificationJobType != null;
	}

	@Override
	public String getNotificationJobTypeId() {
		JobType notificationJobType = getNotificationJobType();

		if (notificationJobType == null) {
			return "none";
		}

		return Integer.toString(notificationJobType.getID());
	}

	@Override
	public String getNotificationJobTypeUsername() {
		JobType notificationJobType = getNotificationJobType();

		if (notificationJobType == null) {
			return "";
		}

		return notificationJobType.getUsername();
	}

	private JobType getNotificationJobType() {
		String bundledJobTypeId = "NOTIFICATION";
		JobType[] matches = ao.find(JobType.class, Query.select().where("BUNDLED_JOB_TYPE_ID = ?", bundledJobTypeId));

		if (matches.length > 1) {
			throw new JobTypeException("Cannot get bundled job type: more than one job type with id " + bundledJobTypeId + " were found.");
		}

		if (matches.length == 0) {
			return null;
		}

		return matches[0];
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
	public String[] formatParameters(String unformatted) {
		if (unformatted == null || "".equals(unformatted.trim())) {
			return new String[0];
		}
		String[] parameters = unformatted.trim().split(System.getProperty("line.separator"));
		for (int i=0; i<parameters.length; i++) {
			parameters[i] = parameters[i].trim();
		}
		return parameters;
	}

	@Override
	public JobTypeParameter[] getJobTypeParameters(int id) {
		JobType jobType = getJobTypeByID(Integer.toString(id));
 		return jobType.getParameters();
	}

}