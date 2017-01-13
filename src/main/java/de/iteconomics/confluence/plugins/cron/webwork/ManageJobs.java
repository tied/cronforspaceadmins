package de.iteconomics.confluence.plugins.cron.webwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.spaces.actions.SpaceAdminAction;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobRunnerKey;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.Job;
import de.iteconomics.confluence.plugins.cron.entities.JobType;

@Scanned
public class ManageJobs extends SpaceAdminAction {

	/**
	 *
	 */
	private static final long serialVersionUID = -6410989010347901283L;

	private static final Logger logger = LoggerFactory.getLogger(ManageJobs.class);

	private final JobService jobService;
	private final JobTypeService jobTypeService;
	@ComponentImport
	private final SchedulerService schedulerService;

	@Inject
	public ManageJobs(JobService jobService, JobTypeService jobTypeService, SchedulerService schedulerService) {
		this.jobService = jobService;
		this.jobTypeService = jobTypeService;
		this.schedulerService = schedulerService;
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

	public String getJobNameValidationString() {
		StringBuilder validationStringBuilder = new StringBuilder();
		for (Job job: jobService.getAllJobs()) {
			validationStringBuilder.append(job.getName() + "|");
		}

		String validationString = validationStringBuilder.toString();
		if (validationString.length() > 0) {
			validationString = validationString.substring(0, validationString.length() - 1);
		}

		return validationString;
	}

	@Override
	public String doDefault() {
		return INPUT;
	}

	public List<String> getAllRegisteredJobs() {

		List<String> keysAsStrings = new ArrayList<>();

		Set<JobRunnerKey> keys= schedulerService.getRegisteredJobRunnerKeys();

        for (JobRunnerKey key: keys) {
            keysAsStrings.add(key.toString());
        }

        return keysAsStrings;
	}

	public boolean isEnabled(Job job) {
		return jobService.isEnabled(job);
	}
}
