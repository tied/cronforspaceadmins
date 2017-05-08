package de.iteconomics.confluence.plugins.cron.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import de.iteconomics.confluence.plugins.cron.entities.JobParameter;
import de.iteconomics.confluence.plugins.cron.entities.JobType;
import de.iteconomics.confluence.plugins.cron.entities.JobTypeParameter;
import de.iteconomics.confluence.plugins.cron.exceptions.JobException;

import net.java.ao.Query;

@Named
public class JobServiceImpl implements JobService {

	@ComponentImport
	private ActiveObjects ao;
	@ComponentImport
	private SchedulerService schedulerService;
	private JobTypeService jobTypeService;

	final static Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

	@Inject
	@Override
	public void setAo(ActiveObjects ao) {
		this.ao = ao;
	}

	@Inject
	@Override
	public void setJobTypeService(JobTypeService jobTypeService) {
		this.jobTypeService = jobTypeService;
	}

	@Inject
	@Override
	public void setSchedulerService(SchedulerService schedulerService) {
		this.schedulerService = schedulerService;
	}

	@Override
	public List<Job> getAllJobs() {
		return Lists.newArrayList(ao.find(Job.class));
	}

	@Override
	public void createJob(HttpServletRequest request) {
		checkParametersRequiredForCreateNotNull(request);
		checkUniqueJobNamePerSpace(request);

		Job job = ao.create(Job.class);
		initializeJob(job, request);
		job.save();

		registerJob(job);
	}

	@Override
	public void updateJob(HttpServletRequest request) {
		checkParametersRequiredForUpdateNotNull(request);
		Job job = getJobIfExists(request);
		if (isJobNameChanged(job, request)) {
			checkUniqueJobNamePerSpace(request);
		}
		boolean reregisterNecessary = isReregisterNecessary(job, request);
		setJobValues(job, request);
		job.save();

		if (reregisterNecessary) {
			unregisterJob(job);
			registerJob(job);
		}
	}

	private boolean isJobNameChanged(Job job, HttpServletRequest request) {
		return !job.getName().equals(request.getParameter("name").trim());
	}

	private boolean isReregisterNecessary(Job job, HttpServletRequest request) {
		return (!job.getCronExpression().equals(request.getParameter("cron-expression").trim()) ||
				!job.getJobTypeID().equals(request.getParameter("job-type").trim()) ||
				parametersChanged(job, request));
	}

	private boolean parametersChanged(Job job, HttpServletRequest request) {
		JobParameter[] parameters = job.getJobParameters();
		Map<String, String> currentValues = new HashMap<>();

		for (JobParameter parameter: parameters) {
			currentValues.put(parameter.getName(), parameter.getValue());
		}

		return !currentValues.equals(getParametersFromRequest(request));
	}

	private Map<String, String> getParametersFromRequest(HttpServletRequest request) {
		Map<String, String> result = new HashMap<>();
		List<String> parameters = Collections.list(request.getParameterNames());
		String prefix = "parameter-";
		for (String parameter: parameters) {
			if (parameter.startsWith(prefix)) {
				result.put(parameter.substring(prefix.length()), request.getParameter(parameter));
			}
		}
		return result;
	}

	private void checkParametersRequiredForCreateNotNull(HttpServletRequest request) {
		String[] requiredParameters = new String[] {"name", "job-type", "spacekey", "cron-expression"};
		checkRequiredParametersNotNull(request, requiredParameters);
	}

	private void checkParametersRequiredForUpdateNotNull(HttpServletRequest request) {
		String[] requiredParameters = new String[] {"name", "job-type", "cron-expression"};
		checkRequiredParametersNotNull(request, requiredParameters);
	}

	private void checkRequiredParametersNotNull(HttpServletRequest request, String[] requiredParameters) {
		for (String parameter: requiredParameters) {
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
		job.setJobTypeChanged(false);
		setJobValues(job, request);
	}

	private void setJobValues(Job job, HttpServletRequest request) {
		String name = request.getParameter("name").trim();
		String jobTypeID = request.getParameter("job-type").trim();
		String safeID = getSafeJobTypeID(jobTypeID);
		String cronExpression = request.getParameter("cron-expression").trim();

		job.setName(name);
		job.setJobTypeID(safeID);
		job.setCronExpression(cronExpression);
		setParameterValues(job, safeID, request);
	}

	private void setParameterValues(Job job, String safeID, HttpServletRequest request) {
		JobParameter[] oldParameters = job.getJobParameters();
		for (JobParameter oldParameter: oldParameters) {
			ao.delete(oldParameter);
		}

		JobType jobType = jobTypeService.getJobTypeByID(safeID);
		JobTypeParameter[] JobTypeParameters = jobType.getParameters();
		for (JobTypeParameter jobTypeParameter: JobTypeParameters) {
			JobParameter jobParameter = ao.create(JobParameter.class);
			jobParameter.setName(jobTypeParameter.getName());
			jobParameter.setValue(request.getParameter("parameter-" + jobTypeParameter.getName()));
			jobParameter.setJob(job);
			jobParameter.setPathParameter(jobTypeParameter.isPathParameter());
			jobParameter.save();
		}
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
		if (job == null) {
			return;
		}
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
		job.setJobTypeChanged(false);
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

		jobParameters.put("url", jobType.getUrl());
		jobParameters.put("queryString", getQueryString(job));
		jobParameters.put("method", jobType.getHttpMethod());
		jobParameters.put("username", jobType.getUsername());
		jobParameters.put("password", jobType.getPassword());

		return jobParameters;
	}

	private String getQueryString(Job job) {
		Map<String, String> parameters = getParameters(job);

		StringBuilder queryString = new StringBuilder();
		queryString.append("?");
		for (String key: parameters.keySet()) {
			queryString.append(key);
			queryString.append("=");
			queryString.append(parameters.get(key));
			queryString.append("&");
		}
		queryString = queryString.deleteCharAt(queryString.length() - 1);

		return queryString.toString();
	}
	
	private Map<String, String> getParameters(Job job) {
		Map<String, String> parameters = new HashMap<>();
		for (JobParameter jobParameter: job.getJobParameters()) {
			parameters.put(jobParameter.getName(), jobParameter.getValue());
		}
		return parameters;
	}

	private Map<String, String> getPathParameters(Job job) {
		Map<String, String> parameters = new HashMap<>();
		for (JobParameter jobParameter: job.getJobParameters()) {
			if (jobParameter.isPathParameter()) {
				parameters.put(jobParameter.getName(), jobParameter.getValue());
			}
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
			throw new JobException("Job id " + jobTypeIDFromRequest + "is invalid.");
		}
	}

	private void checkUniqueJobNamePerSpace(HttpServletRequest request) {
		String name = request.getParameter("name").trim();
		String spaceKey = request.getParameter("spacekey").trim();

		Job[] jobsWithSameName = ao.find(Job.class, Query.select().where("NAME = ?", name));

		for (Job job: jobsWithSameName) {
			logger.error(job.getSpaceKey());
			logger.error(spaceKey);
			if (job.getSpaceKey().equals(spaceKey)) {
				throw new JobException("A job with name " + name + "already exists.");
			}
		}
	}

	@Override
	public void deleteJob(Job job) {
		unregisterJob(job);
		deleteJobParameters(job);
		ao.delete(job);
	}

	private void deleteJobParameters(Job job) {
		for (JobParameter jobParameter: job.getJobParameters()) {
			ao.delete(jobParameter);
		}
	}

	@Override
	public boolean isParametersInconsistent(Job job) {
		Set<String> jobParameterNames = getJobParameterNames(job);
		JobType jobType = jobTypeService.getJobTypeByID(job.getJobTypeID());
		Set<String> jobTypeParameterNames = getJobTypeParameterNames(jobType);

		return !jobParameterNames.equals(jobTypeParameterNames);

	}
	private Set<String> getJobParameterNames(Job job) {
		Set<String> jobParameterNames = new HashSet<>();
		for (JobParameter jobParameter: job.getJobParameters()) {
			jobParameterNames.add(jobParameter.getName());
		}
		return jobParameterNames;
	}

	private Set<String> getJobTypeParameterNames(JobType jobType) {
		Set<String> jobTypeParameterNames = new HashSet<>();
		for (JobTypeParameter jobTypeParameter: jobType.getParameters()) {
			jobTypeParameterNames.add(jobTypeParameter.getName());
		}
		return jobTypeParameterNames;
	}

	@Override
	public void deleteJob(HttpServletRequest request) {
		Job job = getJobIfExists(request);
		deleteJob(job);
	}

	private Job getJobIfExists(HttpServletRequest request) {
		String id = request.getParameter("id");
		if (id == null) {
			throw new JobException("A job id is required, but was null.");
		} else {
			id = id.trim();
		}
		return getJobIfExists(id);
	}

	@Override
	public Job getJobIfExists(String id) {
		Job[] jobs= ao.find(Job.class, Query.select().where("id = ?", id));

		if (jobs.length != 1) {
			throw new JobException("Job with id " + id + " does not exist.");
		}

		return jobs[0];
	}

	@Override
	public boolean isEnabled(Job job) {
		if (job == null) {
			return false;
		}
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
}