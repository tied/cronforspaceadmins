package de.iteconomics.confluence.plugins.cron.impl;

import java.io.Serializable;
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
		checkUniqueJobNamePerSpace(request);

		Job job = ao.create(Job.class);
		initializeJob(job, request);
		job.save();

		registerJob(job);
	}

	@Override
	public void updateJob(HttpServletRequest request) {
		Job job = getJobIfExists(request);
		setJobValues(job, request);
		job.save();

	}

	private void initializeJob(Job job, HttpServletRequest request) {
		String spaceKey = request.getParameter("spacekey");
		String jobKey = spaceKey + ":" + job.getID();

		job.setSpaceKey(spaceKey);
		job.setJobKey(jobKey);

		setJobValues(job, request);
	}

	private void setJobValues(Job job, HttpServletRequest request) {
		String name = request.getParameter("name");
		String jobTypeID = request.getParameter("job-type");
		String safeID = getSafeJobTypeID(jobTypeID);
		String cronExpression = request.getParameter("cron-expression");

		job.setName(name);
		job.setJobTypeID(safeID);
		job.setCronExpression(cronExpression);
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
		schedulerService.unscheduleJob(JobId.of(job.getJobKey()));
		schedulerService.unregisterJobRunner(JobRunnerKey.of(job.getJobKey() + ":runner"));
	}

	@Override
	public void registerJob(Job job) {
		JobRunnerKey jobRunnerKey = JobRunnerKey.of(job.getJobKey() + ":runner");

		registerJobRunner(jobRunnerKey);
		scheduleJob(job, jobRunnerKey);
	}

	private void registerJobRunner(JobRunnerKey jobRunnerKey) {
		JobRunner jobRunner = new CronJobRunner();
		schedulerService.registerJobRunner(jobRunnerKey, jobRunner);
	}

	private void scheduleJob(Job job, JobRunnerKey jobRunnerKey) {
		JobConfig jobConfig = getJobConfig(job, jobRunnerKey);

		try {
			schedulerService.scheduleJob(JobId.of(job.getJobKey()), jobConfig);
		} catch (SchedulerServiceException e) {
			logger.error("could not schedule job with key: " + job.getJobKey());
		}
	}

	private JobConfig getJobConfig(Job job, JobRunnerKey jobRunnerKey) {
		Schedule schedule = Schedule.forCronExpression(job.getCronExpression());

		Map<String, Serializable> jobParameters = getJobParameters(job);
		JobConfig jobConfig = JobConfig.forJobRunnerKey(jobRunnerKey).withSchedule(schedule).withParameters(jobParameters);

		return jobConfig;
	}

	private Map<String, Serializable> getJobParameters(Job job) {
		Map<String, Serializable> jobParameters = new HashMap<>();
		JobType jobType = jobTypeService.getJobTypeByID(job.getJobTypeID());
		jobParameters.put("url", jobType.getUrl());
		jobParameters.put("method", jobType.getHttpMethod());
		return jobParameters;
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
		String name = request.getParameter("name");
		String spaceKey = request.getParameter("spacekey");

		Job[] jobsWithSameName = ao.find(Job.class, Query.select().where("NAME = ?", name));

		for (Job job: jobsWithSameName) {
			if (job.getSpaceKey().equals(spaceKey)) {
				throw new JobException("Cannot create: job with name " + name + "already exists.");
			}
		}
	}

	@Override
	public void deleteJob(HttpServletRequest request) {
		unregisterJob(request);
		Job job = getJobIfExists(request);
		ao.delete(job);
	}

	private Job getJobIfExists(HttpServletRequest request) {
		String id = request.getParameter("id");
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
}