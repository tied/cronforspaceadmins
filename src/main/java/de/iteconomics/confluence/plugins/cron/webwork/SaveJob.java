package de.iteconomics.confluence.plugins.cron.webwork;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.ConfluenceActionSupport;

import de.iteconomics.confluence.plugins.cron.api.JobService;


public class SaveJob extends ConfluenceActionSupport {

	private static Logger logger = LoggerFactory.getLogger(SaveJob.class);

	/**
	 *
	 */
	private static final long serialVersionUID = -6061253789428293167L;

	@Inject
	private final JobService jobService;

	public SaveJob(JobService jobService) {
		this.jobService = jobService;
	}

	@Override
	public String execute() {
		logger.error("######### execute() in SaveJob called");
	    HttpServletRequest request = this.getCurrentRequest();
	    jobService.createJob(request);
		return SUCCESS;
	}
}
