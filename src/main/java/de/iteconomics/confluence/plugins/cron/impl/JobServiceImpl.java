package de.iteconomics.confluence.plugins.cron.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.Schedule;

import com.google.common.collect.Lists;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.Job;
import de.iteconomics.confluence.plugins.cron.entities.JobType;
import de.iteconomics.confluence.plugins.cron.exceptions.JobException;

import net.java.ao.Query;

@Named
public class JobServiceImpl implements JobService {

	@ComponentImport
	private ActiveObjects ao;
	@ComponentImport
	private SchedulerService schedulerService;
	@Inject
	private JobTypeService jobTypeService;

	final static Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

	@Inject
	@Override
	public void setAo(ActiveObjects ao) {
		this.ao = ao;
	}

	public void setJobTypeService(JobTypeService jobTypeService) {
		this.jobTypeService = jobTypeService;
	}

	@Inject
	public void setSchedulerService(SchedulerService schedulerService) {
		this.schedulerService = schedulerService;
	}

	@Override
	public List<Job> getAllJobs() {
		return Lists.newArrayList(ao.find(Job.class));
	}

	@Override
	public void createJob(HttpServletRequest request) {
		checkRequiredRequestParametersNotNull(request);
		checkUniqueJobNamePerSpace(request);

		Job job = ao.create(Job.class);
		initializeJob(job, request);
		job.save();

		registerJob(job);
	}

	@Override
	public void updateJob(HttpServletRequest request) {
		checkRequiredRequestParametersNotNull(request);
		Job job = getJobIfExists(request);
		boolean reregisterNecessary = isReregisterNecessary(job, request);
		setJobValues(job, request);
		job.save();

		if (reregisterNecessary) {
			unregisterJob(job);
			registerJob(job);
		}
	}

	private boolean isReregisterNecessary(Job job, HttpServletRequest request) {
		return (!job.getCronExpression().equals(request.getParameter("cron-expression").trim()) ||
				!job.getJobTypeID().equals(request.getParameter("job-type").trim()) ||
				!job.getParameters().equals(getParameterString(request))
				);
	}

	private void checkRequiredRequestParametersNotNull(HttpServletRequest request) {
		for (String parameter: new String[] {"name", "job-type", "spacekey", "cron-expression"}) {
			if (request.getParameter(parameter) == null) {
				throw new JobException("Cannot create job: " + parameter + " may not be null.");
			}
		}
	}

	private void initializeJob(Job job, HttpServletRequest request) {
		String spaceKey = request.getParameter("spacekey").trim();
		String jobKey = spaceKey + ":" + job.getID();

		job.setSpaceKey(spaceKey);
		job.setJobKey(jobKey);
		setJobValues(job, request);
	}

	private void setJobValues(Job job, HttpServletRequest request) {
		String name = request.getParameter("name").trim();
		String jobTypeID = request.getParameter("job-type").trim();
		String safeID = getSafeJobTypeID(jobTypeID);
		String cronExpression = request.getParameter("cron-expression").trim();
		String parameterString = getParameterString(request);

		job.setName(name);
		job.setJobTypeID(safeID);
		job.setCronExpression(cronExpression);
		job.setParameters(parameterString);
	}

	private String getParameterString(HttpServletRequest request) {
		String prefix = "parameter-";
		String parameterString = "";
		Enumeration<?> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String key = ((String) parameterNames.nextElement()).trim();
			if (key.startsWith(prefix)) {
				String[] values = request.getParameterValues(key);
				String value = values[0].trim();
				parameterString += key.substring(prefix.length());
				parameterString += "=";
				parameterString += (value);
				parameterString += "&";
			}
		}
		if (parameterString.length() > 0) {
			parameterString = parameterString.substring(0, parameterString.length() -1);
		}

		return parameterString;
	}

	@Override
	public void registerAllJobs() {
		List<Job> allJobs = getAllJobs();
		for (Job job: allJobs) {
			registerJob(job);
		}
	}

	@Override
	public void unregisterJob(HttpServletRequest request) {
		Job job = getJobIfExists(request);
		unregisterJob(job);
	}

	@Override
	public void unregisterJob(Job job) {
		job.setActive(false);
		job.save();
		schedulerService.unscheduleJob(JobId.of(job.getJobKey()));
		schedulerService.unregisterJobRunner(JobRunnerKey.of(job.getJobKey() + ":runner"));
	}

	@Override
	public void registerJob(Job job) {
		JobRunnerKey jobRunnerKey = JobRunnerKey.of(job.getJobKey() + ":runner");

		registerJobRunner(jobRunnerKey);
		scheduleJob(job, jobRunnerKey);
		job.setActive(true);
		job.save();
	}

	private void registerJobRunner(JobRunnerKey jobRunnerKey) {
		JobRunner jobRunner = new CronJobRunner();
		schedulerService.registerJobRunner(jobRunnerKey, jobRunner);
	}

	private void scheduleJob(Job job, JobRunnerKey jobRunnerKey) {
		JobConfig jobConfig = createJobConfig(job, jobRunnerKey);

		try {
			schedulerService.scheduleJob(JobId.of(job.getJobKey()), jobConfig);
		} catch (SchedulerServiceException e) {
			logger.error("could not schedule job with key: " + job.getJobKey());
		}
	}

	private JobConfig createJobConfig(Job job, JobRunnerKey jobRunnerKey) {
		Schedule schedule = Schedule.forCronExpression(job.getCronExpression());

		Map<String, Serializable> jobParameters = getJobParameters(job);
		JobConfig jobConfig = JobConfig.forJobRunnerKey(jobRunnerKey).withSchedule(schedule).withParameters(jobParameters);

		return jobConfig;
	}

	private Map<String, Serializable> getJobParameters(Job job) {
		Map<String, Serializable> jobParameters = new HashMap<>();

		JobType jobType = jobTypeService.getJobTypeByID(job.getJobTypeID());
		Set<String> pathParameterNames = getPathParameterNames(jobType.getUrl());
		Map<String, String> parametersMap = getParametersAsMap(job.getParameters());
		String url = getUrlWithParameters(jobType.getUrl(), parametersMap);
		String nonPathParameters = getNonPathParameters(job.getParameters(), pathParameterNames);

		jobParameters.put("url", url);
		jobParameters.put("queryString", nonPathParameters);
		jobParameters.put("method", jobType.getHttpMethod());
		jobParameters.put("username", jobType.getUsername());
		jobParameters.put("password", jobType.getPassword());

		return jobParameters;
	}

	private String getNonPathParameters(String parameters, Set<String> pathParameterNames) {
		String result = "";
		for (String keyValuePair: parameters.split("&")) {
			String key = keyValuePair.split("=")[0];
			if (!pathParameterNames.contains(key)) {
				result += keyValuePair;
				result += "&";
			}
		}
		if (result.length() > 0) {
			result = result.substring(0, result.length() - 1);
		}

		return result;
	}

	private Set<String> getPathParameterNames(String url) {
		Set<String> parameters= new HashSet<>();
		Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(url);
		while (m.find()) {
			String param = m.group().substring(1, m.group().length() - 1);
			parameters.add(param);
		}

		return parameters;
	}

	private String getUrlWithParameters(String url, Map<String, String> parametersMap) {
		String result = url;

		for (String parameter: getPathParameterNames(url)) {
			result = result.replace("{" + parameter + "}", parametersMap.get(parameter));
		}

		return result;
	}

	private Map<String, String> getParametersAsMap(String parametersString) {
		Map<String, String> parameters = new HashMap<>();
		if (parametersString == null || parametersString.equals("")) {
			return parameters;
		}

		for (String element: parametersString.split("&")) {
			String[] keyValuePair = element.split("=");
			if (keyValuePair.length != 2) {
				throw new JobException("Invalid parameter string: " + parametersString);
			}
			String key = keyValuePair[0];
			String value = keyValuePair[1];
			parameters.put(key, value);
		}

		return parameters;
	}

	private String getSafeJobTypeID(String jobTypeIDFromRequest) {
		if (jobTypeIDFromRequest == null) {
			throw new JobException("Cannot create: job type id is 'null'.");
		}

		if (!isValidJobTypeId(jobTypeIDFromRequest)) {
			throw new JobException("Cannot create: There is no job type with the id " + jobTypeIDFromRequest + ".");
		}

		return jobTypeIDFromRequest;
	}


	private boolean isValidJobTypeId(String jobTypeIDFromRequest) {
		int jobTypeID = asInt(jobTypeIDFromRequest);

		return jobTypeExists(jobTypeID);
	}

	private boolean jobTypeExists(int jobTypeID) {
		boolean jobTypeExists = false;
		List<JobType> allJobTypes = jobTypeService.getAllJobTypes();
		for (JobType jobType: allJobTypes) {
			if (jobType.getID() == jobTypeID) {
				jobTypeExists = true;
			}
		}

		return jobTypeExists;
	}

	private int asInt(String jobTypeIDFromRequest) {
		try {
			return Integer.parseInt(jobTypeIDFromRequest);
		} catch (NumberFormatException e) {
			throw new JobException("Cannot create: job id " + jobTypeIDFromRequest + "is invalid.");
		}
	}

	private void checkUniqueJobNamePerSpace(HttpServletRequest request) {
		String name = request.getParameter("name").trim();
		String spaceKey = request.getParameter("spacekey").trim();

		Job[] jobsWithSameName = ao.find(Job.class, Query.select().where("NAME = ?", name));

		for (Job job: jobsWithSameName) {
			if (job.getSpaceKey().equals(spaceKey)) {
				throw new JobException("Cannot create: job with name " + name + "already exists.");
			}
		}
	}

	@Override
	public void deleteJob(Job job) {
		unregisterJob(job);
		ao.delete(job);
	}

	@Override
	public void deleteJob(HttpServletRequest request) {
		Job job = getJobIfExists(request);
		deleteJob(job);
	}

	private Job getJobIfExists(HttpServletRequest request) {
		String id = request.getParameter("id").trim();
		Job[] jobs= ao.find(Job.class, Query.select().where("id = ?", id));

		if (jobs.length != 1) {
			throw new JobException("Cannot delete: job with id " + id + " does not exist.");
		}

		return jobs[0];
	}

	@Override
	public boolean isEnabled(Job job) {
		JobRunnerKey jobRunnerKey = JobRunnerKey.of(job.getJobKey() + ":runner");
		Set<JobRunnerKey> registeredJobRunnerKeys = schedulerService.getRegisteredJobRunnerKeys();

		boolean isEnabled = false;
		for (JobRunnerKey key: registeredJobRunnerKeys) {
			if (key.equals(jobRunnerKey)) {
				isEnabled = true;
				break;
			}
		}

		return isEnabled;
	}

	@Override
	public Set<Job> getDisabledJobs() {
		Set<Job> disabledJobs = new HashSet<>();
		for (Job job: getAllJobs()) {
			if (!isEnabled(job)) {
				disabledJobs.add(job);
			}
		}
		return disabledJobs;
	}

	@Override
	public void registerJob(HttpServletRequest request) {
		Job job = getJobIfExists(request);
		registerJob(job);
	}

	@Override
	public List<Job> getJobs(String spaceKey) {
		Job[] jobs = ao.find(Job.class, Query.select().where("SPACE_KEY = ?", spaceKey));
		return Arrays.asList(jobs);
	}

	@Override
	public List<Job> getJobsByJobTypeID(int jobTypeID) {
		Job[] jobs= ao.find(Job.class, Query.select().where("JOB_TYPE_ID = ?", jobTypeID));
		return Arrays.asList(jobs);
	}

	@Override
	public String[] formatParameters(String unformatted) {
		return unformatted.replaceAll("=", ": ").split("&");
	}
}