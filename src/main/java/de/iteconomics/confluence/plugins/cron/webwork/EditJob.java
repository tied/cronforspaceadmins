package de.iteconomics.confluence.plugins.cron.webwork;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.opensymphony.webwork.ServletActionContext;

import de.iteconomics.confluence.plugins.cron.api.JobService;

@Scanned
public class EditJob extends ConfluenceActionSupport {

	/**
	 *
	 */
	private static final long serialVersionUID = -6061253789428293167L;

	private final JobService jobService;

	@Inject
	public EditJob(JobService jobService) {
		this.jobService = jobService;
	}

	@Override
	public String execute() {
	    HttpServletRequest request = this.getCurrentRequest();

	    jobService.updateJob(request);

	    String spaceKey = request.getParameter("spacekey");
	    String baseUrl = settingsManager.getGlobalSettings().getBaseUrl();

	    try {
			ServletActionContext.getResponse().sendRedirect(baseUrl + "/plugins/cron-for-space-admins/ManageJobs.action?key=" + spaceKey);
		} catch (IOException e) {
			return SUCCESS;
		}

	    return NONE;
	}
}