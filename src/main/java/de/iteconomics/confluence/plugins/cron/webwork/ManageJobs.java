package de.iteconomics.confluence.plugins.cron.webwork;

import java.util.List;

import javax.inject.Inject;

import com.atlassian.confluence.spaces.actions.SpaceAdminAction;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.entities.Job;

public class ManageJobs extends SpaceAdminAction {

	/**
	 *
	 */
	private static final long serialVersionUID = -6410989010347901283L;

	@Inject
	private final JobService jobService;

	public ManageJobs(JobService jobService) {
		this.jobService = jobService;
	}

	public List<Job> getAllJobs() {
		List<Job> jobs = jobService.getAllJobs();
		return jobs;
	}

	@Override
	public String doDefault() {
		return INPUT;
	}

}
