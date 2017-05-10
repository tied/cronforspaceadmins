package de.iteconomics.confluence.plugins.cron.webwork;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;

import de.iteconomics.confluence.plugins.cron.api.JobTypeService;

@Scanned
public class SaveJobType extends ConfluenceActionSupport {

	/**
	 *
	 */
	private static final long serialVersionUID = -6061253789428293167L;

	private final JobTypeService jobTypeService;
	
	@Inject
	public SaveJobType(JobTypeService jobTypeService) {
		this.jobTypeService = jobTypeService;
	}

	@Override
	public String execute() {
	    HttpServletRequest request = this.getCurrentRequest();
	    jobTypeService.createJobType(request);
		return SUCCESS;
	}
}
