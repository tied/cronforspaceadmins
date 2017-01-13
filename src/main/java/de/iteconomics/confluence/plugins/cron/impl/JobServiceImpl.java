package de.iteconomics.confluence.plugins.cron.impl;

import java.io.Serializable;
import java.util.ArrayList;
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
		String name = request.getParameter("name");
		String jobTypeID = request.getParameter("job-type");
		String cronExpression = request.getParameter("cron-expression");
		String spaceKey = request.getParameter("spacekey");
		String jobKey = jobTypeID + ":" + spaceKey + ":" + name;
		Job job;
		checkUniqueJobNamePerSpace(request, spaceKey);
		String safeID = getSafeJobTypeID(jobTypeID);
		job = ao.create(Job.class);
		job.setName(name);
		job.setJobTypeID(safeID);
		job.setCronExpression(cronExpression);
		job.setSpaceKey(spaceKey);
		job.setJobKey(jobKey);
		job.save();
		registerJob(job);
	}

	@Override
	public void registerAllJobs() {
		List<Job> allJobs = getAllJobs();
		for (Job job: allJobs) {
			registerJob(job);
		}
	}

	@Override
	public void registerJob(Job job) {
		JobRunnerKey jobRunnerKey = JobRunnerKey.of(job.getJobKey() + ":runner");
		Schedule schedule = Schedule.forCronExpression(job.getCronExpression());
		Map<String, Serializable> jobParameters = new HashMap<>();
		JobType jobType = jobTypeService.getJobTypeByID(job.getJobTypeID());
		String url = jobType.getUrl();
		jobParameters.put("url", url);
		JobConfig jobConfig = JobConfig.forJobRunnerKey(jobRunnerKey).withSchedule(schedule).withParameters(jobParameters);
		JobId jobId = JobId.of(job.getJobKey());
		JobRunner jobRunner = new CronJobRunner();
		try {
			schedulerService.registerJobRunner(jobRunnerKey, jobRunner);
			schedulerService.scheduleJob(jobId, jobConfig);
		} catch (SchedulerServiceException e) {
			logger.error(e.getMessage());
		}
	}

	private String getSafeJobTypeID(String jobTypeIDFromRequest) {
		if (jobTypeIDFromRequest == null) {
			throw new JobException("Cannot create: job type id is 'null'.");
		}
		int jobTypeID;
		try {
			jobTypeID = Integer.parseInt(jobTypeIDFromRequest);
		} catch (NumberFormatException e) {
			throw new JobException("Cannot create: job id " + jobTypeIDFromRequest + "is invalid.");
		}
		List<JobType> allJobTypes = jobTypeService.getAllJobTypes();
		for (JobType jobType: allJobTypes) {
			if (jobType.getID() == (jobTypeID)) {
				return jobTypeIDFromRequest;
			}
		}

		throw new JobException("Cannot create: There is no job type with the id " + jobTypeIDFromRequest + ".");
	}


	private void checkUniqueJobNamePerSpace(HttpServletRequest request, String spaceKey) {
		String name = request.getParameter("name");

		Job[] jobs = ao.find(Job.class, Query.select().where("name = ?", name));

		List<Job> jobsInSpace = new ArrayList<>();
		for (Job job: jobs) {
			if (job.getSpaceKey().equals(spaceKey)) {
				jobsInSpace.add(job);
			}
		}
		if (jobsInSpace.size() > 0) {
			throw new JobException("Cannot create: job with name " + name + "already exists.");
		}
	}

	@Override
	public void deleteJob(HttpServletRequest request) {
		String id = request.getParameter("id");
		Job[] jobs= ao.find(Job.class, Query.select().where("id = ?", id));
		if (jobs.length != 1)
			throw new JobException("Cannot delete: job with id " + id + " does not exist.");
		if (jobs.length == 1) {
			ao.delete(jobs[0]);
		}
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
}
