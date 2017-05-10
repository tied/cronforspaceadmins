package de.iteconomics.confluence.plugins.cron.webwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.Job;
import de.iteconomics.confluence.plugins.cron.entities.JobType;
import de.iteconomics.confluence.plugins.cron.entities.JobTypeParameter;
import de.iteconomics.confluence.plugins.cron.exceptions.JobTypeException;

@Scanned
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
		try {
			return jobTypeService.getAllJobTypes();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ArrayList<>();
		}
	}

	public boolean hasNotificationJobType() {
		try {
			return jobTypeService.hasNotificationJobType();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
	}

	public String getNotificationJobTypeId() {
		try {
			return jobTypeService.getNotificationJobTypeId();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return "";
		}
	}

	public String getNotificationJobTypeUsername() {
		try {
			return jobTypeService.getNotificationJobTypeUsername();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return "";
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

	public JobType getJobTypeByID(String id) {
		try {
			return jobTypeService.getJobTypeByID(id);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	public JobTypeParameter[] getParameters(int id) {
		try {
			return jobTypeService.getJobTypeParameters(id);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new JobTypeParameter[0];
		}
	}

	public boolean isEnabled(Job job) {
		try {
			return jobService.isEnabled(job);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
	}

	public String getJobTypeNameValidationString() {
		StringBuilder validationStringBuilder = new StringBuilder();

		try {
		for (JobType jobType: jobTypeService.getAllJobTypes()) {
			validationStringBuilder.append(jobType.getName() + "|");
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

	public boolean isParametersInconsistent(Job job) {
		try {
			return jobService.isParametersInconsistent(job);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
	}
}
