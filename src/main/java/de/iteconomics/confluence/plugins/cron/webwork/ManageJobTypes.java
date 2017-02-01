package de.iteconomics.confluence.plugins.cron.webwork;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.ConfluenceActionSupport;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.Job;
import de.iteconomics.confluence.plugins.cron.entities.JobType;
import de.iteconomics.confluence.plugins.cron.exceptions.JobTypeException;

public class ManageJobTypes extends ConfluenceActionSupport {

	/**
	 *
	 */
	private static final long serialVersionUID = -6061253789428293167L;
	private static final Logger logger = LoggerFactory.getLogger(ManageJobTypes.class);
	private final JobTypeService jobTypeService;
	private final JobService jobService;

	@Inject
	public ManageJobTypes(JobTypeService jobTypeService, JobService jobService) {
		this.jobTypeService = jobTypeService;
		this.jobService = jobService;
	}

	@Override
	public String execute() {
		return SUCCESS;
	}

	public List<JobType> getAllJobTypes() {
		return jobTypeService.getAllJobTypes();
	}

	public List<Job> getAllJobs() {
		return jobService.getAllJobs();
	}

	public JobType getJobTypeByID(String id) {
		try {
			return jobTypeService.getJobTypeByID(id);
		} catch (JobTypeException e) {
			logger.error("There is not JobType with the id " + id + ".");
			return null;
		}
	}

	public boolean isEnabled(Job job) {
		return jobService.isEnabled(job);
	}

	public String getJobTypeNameValidationString() {
		StringBuilder validationStringBuilder = new StringBuilder();
		for (JobType jobType: jobTypeService.getAllJobTypes()) {
			validationStringBuilder.append(jobType.getName() + "|");
		}

		String validationString = validationStringBuilder.toString();
		if (validationString.length() > 0) {
			validationString = validationString.substring(0, validationString.length() - 1);
		}

		return validationString;
	}

}
