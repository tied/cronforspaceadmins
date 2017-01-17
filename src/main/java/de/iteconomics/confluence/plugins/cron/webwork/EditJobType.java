package de.iteconomics.confluence.plugins.cron.webwork;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.confluence.core.ConfluenceActionSupport;

import de.iteconomics.confluence.plugins.cron.api.JobTypeService;


public class EditJobType extends ConfluenceActionSupport {

	/**
	 *
	 */
	private static final long serialVersionUID = -6061253789428293167L;

	@Inject
	private final JobTypeService jobTypeService;

	public EditJobType(JobTypeService jobTypeService) {
		this.jobTypeService = jobTypeService;
	}

	@Override
	public String execute() {
	    HttpServletRequest request = this.getCurrentRequest();

	    jobTypeService.updateJobType(request);

		return SUCCESS;
	}
}
