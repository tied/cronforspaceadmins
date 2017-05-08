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
import de.iteconomics.confluence.plugins.cron.exceptions.JobTypeException;

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

	public List<Job> getJobs(String spaceKey) {
		try {
			return jobService.getJobs(spaceKey);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ArrayList<>();
		}
	}

	public List<Job> getAllJobs() {
		try {
			return jobService.getAllJobs();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ArrayList<>();
		}
	}

	public List<JobType> getAllJobTypes() {
		try {
			return jobTypeService.getAllJobTypes();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ArrayList<>();
		}
	}

	public boolean isParametersInconsistent(Job job) {
		try {
			return jobService.isParametersInconsistent(job);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
	}

	public JobType getJobTypeByID(String id) {
		try {
			return jobTypeService.getJobTypeByID(id);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	public String getJobNameValidationString() {
		StringBuilder validationStringBuilder = new StringBuilder();
		try {
			for (Job job: jobService.getAllJobs()) {
				validationStringBuilder.append(job.getName() + "|");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return "";
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

		try {
			Set<JobRunnerKey> keys= schedulerService.getRegisteredJobRunnerKeys();
	        for (JobRunnerKey key: keys) {
	            keysAsStrings.add(key.toString());
	        }
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

        return keysAsStrings;
	}

	public boolean isEnabled(Job job) {
		try {
			return jobService.isEnabled(job);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
	}

}