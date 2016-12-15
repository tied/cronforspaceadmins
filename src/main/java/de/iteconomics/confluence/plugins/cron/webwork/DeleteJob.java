package de.iteconomics.confluence.plugins.cron.webwork;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.confluence.core.ConfluenceActionSupport;

import de.iteconomics.confluence.plugins.cron.api.JobService;


public class DeleteJob extends ConfluenceActionSupport {

	/**
	 *
	 */
	private static final long serialVersionUID = -6061253789428293167L;

	@Inject
	private final JobService jobService;

	public DeleteJob(JobService jobService) {
		this.jobService = jobService;
	}

	@Override
	public String execute() {
	    HttpServletRequest request = this.getCurrentRequest();
	    jobService.deleteJob(request);
		return SUCCESS;
	}
}
