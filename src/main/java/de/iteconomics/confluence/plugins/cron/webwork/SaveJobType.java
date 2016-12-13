package de.iteconomics.confluence.plugins.cron.webwork;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.confluence.core.ConfluenceActionSupport;

import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.JobType;

public class SaveJobType extends ConfluenceActionSupport {

	/**
	 *
	 */
	private static final long serialVersionUID = -6061253789428293167L;

	@Inject
	private final JobTypeService jobTypeService;

	public SaveJobType(JobTypeService jobTypeService) {
		this.jobTypeService = jobTypeService;
	}

	@Override
	public String execute() {
	    HttpServletRequest request = this.getCurrentRequest();
	    jobTypeService.createJobType(request);
		return SUCCESS;
	}

	public String getGreeting() {
		return "Hello, Stranger!";
	}

	public List<JobType> getAllJobTypes() {
		List<JobType> jobTypes = jobTypeService.getAllJobTypes();
		return jobTypes;
	}
}
