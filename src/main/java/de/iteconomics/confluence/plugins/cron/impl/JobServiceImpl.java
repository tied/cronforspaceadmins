package de.iteconomics.confluence.plugins.cron.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.Lists;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.entities.Job;
import de.iteconomics.confluence.plugins.cron.exceptions.JobException;
import net.java.ao.Query;


@Named
public class JobServiceImpl implements JobService {

	@ComponentImport
	private ActiveObjects ao;

//	private final static Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

	@Inject
	@Override
	public void setAo(ActiveObjects ao) {
		this.ao = ao;
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

		Job job;
//		checkNewJobName(request);
		job = ao.create(Job.class);
		job.setName(name);
		job.setJobTypeID(jobTypeID);
		job.setCronExpression(cronExpression);

		job.save();
	}


	// change to only check for jobs in the same space
	private void checkNewJobName(HttpServletRequest request) {
		String name = request.getParameter("name");

		Job[] jobs = ao.find(Job.class, Query.select().where("name = ?", name));

		if (jobs.length > 0) {
			throw new JobException("Cannot create: job with name " + name + "already exists.");
		}
	}

	@Override
	public void deleteJob(HttpServletRequest request) {
		String name = request.getParameter("name");
		Job[] jobs= ao.find(Job.class, Query.select().where("name = ?", name));
		if (jobs.length != 1)
			throw new JobException("Cannot delete: job with name " + name + " does not exist.");
		if (jobs.length == 1) {
			ao.delete(jobs[0]);
		}
	}
}
