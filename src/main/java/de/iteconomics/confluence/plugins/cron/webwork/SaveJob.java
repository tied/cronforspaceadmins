package de.iteconomics.confluence.plugins.cron.webwork;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.opensymphony.webwork.ServletActionContext;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.exceptions.JobException;

@Scanned
public class SaveJob extends ConfluenceActionSupport {

	private static Logger logger = LoggerFactory.getLogger(SaveJob.class);

	/**
	 *
	 */
	private static final long serialVersionUID = -6061253789428293167L;

	@Inject
	private final JobService jobService;
	@ComponentImport
	private final SettingsManager settingsManager;

	public SaveJob(JobService jobService, SettingsManager settingsManager) {
		this.jobService = jobService;
		this.settingsManager = settingsManager;
	}

	@Override
	public String execute() {
	    HttpServletRequest request = this.getCurrentRequest();
	    String spaceKey = request.getParameter("spacekey");
	    String baseUrl = settingsManager.getGlobalSettings().getBaseUrl();

	    try {
	    	jobService.createJob(request);
	    } catch (JobException e) {
		    try {
		    	logger.error("There was an error while saving the job");
		    	logger.error(e.getMessage());
		    	ServletActionContext.getResponse().sendRedirect(baseUrl + "/plugins/cron-for-space-admins/ManageJobs.action?key=" + spaceKey + "&save-error=true");
				return NONE;
			} catch (IOException e2) {
				return SUCCESS;
			}
	    }

	    try {
			ServletActionContext.getResponse().sendRedirect(baseUrl + "/plugins/cron-for-space-admins/ManageJobs.action?key=" + spaceKey);
		} catch (IOException e) {
			return SUCCESS;
		}
		return NONE;
	}
}
