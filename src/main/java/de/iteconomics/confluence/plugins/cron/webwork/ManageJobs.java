package de.iteconomics.confluence.plugins.cron.webwork;

import java.util.List;

import javax.inject.Inject;

import com.atlassian.confluence.spaces.actions.SpaceAdminAction;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.Job;
import de.iteconomics.confluence.plugins.cron.entities.JobType;

public class ManageJobs extends SpaceAdminAction {

	/**
	 *
	 */
	private static final long serialVersionUID = -6410989010347901283L;

	@Inject
	private final JobService jobService;
	@Inject
	private final JobTypeService jobTypeService;

	public ManageJobs(JobService jobService, JobTypeService jobTypeService) {
		this.jobService = jobService;
		this.jobTypeService = jobTypeService;
	}

	public List<Job> getAllJobs() {
		List<Job> jobs = jobService.getAllJobs();
		return jobs;
	}

	public List<JobType> getAllJobTypes() {
		List<JobType> allJobTypes = jobTypeService.getAllJobTypes();
		return allJobTypes;
	}

	public JobType getJobTypeByID(String id) {
		return jobTypeService.getJobTypeByID(id);
	}

	@Override
	public String doDefault() {
		return INPUT;
	}

}
