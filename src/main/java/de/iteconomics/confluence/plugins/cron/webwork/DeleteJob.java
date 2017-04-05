package de.iteconomics.confluence.plugins.cron.webwork;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.opensymphony.webwork.ServletActionContext;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.exceptions.JobException;


public class DeleteJob extends ConfluenceActionSupport {

	/**
	 *
	 */
	private static final long serialVersionUID = -6061253789428293167L;
	private static Logger logger = LoggerFactory.getLogger(DeleteJob.class);

	@Inject
	private final JobService jobService;

	public DeleteJob(JobService jobService) {
		this.jobService = jobService;
	}

	@Override
	public String execute() {
		HttpServletRequest request = this.getCurrentRequest();
		String spaceKey = request.getParameter("spacekey");
	    String url = settingsManager.getGlobalSettings().getBaseUrl();
	    String fromJobTypeAdminPage = request.getParameter("fromJobTypeAdminPage");

		Map<String, String> queryParameters = new HashMap<>();

	    if ("false".equals(fromJobTypeAdminPage)) {
	    	url += "/plugins/cron-for-space-admins/ManageJobs.action";
	    	queryParameters.put("key", spaceKey);
	    } else {
	    	url += "/plugins/cron-for-space-admins/ManageJobTypes.action";
	    }

	    try {
	    	jobService.deleteJob(request);
	    } catch (JobException e) {
    		return redirectWithError(url, queryParameters);
	    }

		return redirect(url, queryParameters);
	}

	private String redirectWithError(String url, Map<String, String> queryParameters) {
		queryParameters.put("delete-error", "true");
		return redirect(url, queryParameters);
	}

	private String redirect(String url, Map<String, String> queryParameters) {
		url += buildQueryString(queryParameters);

		try {
			ServletActionContext.getResponse().sendRedirect(url);
			return NONE;
		} catch (IOException e) {
			logger.error("Redirect failed to url: " + url);
			return SUCCESS;
		}
	}

	private String buildQueryString(Map<String, String> queryParameters) {
		boolean isFirst = true;
		String queryString = "";

		for (String key: queryParameters.keySet()) {
			if (isFirst) {
				queryString += "?";
				isFirst = false;
			} else {
				queryString += "&";
			}
			queryString += key;
			queryString += "=";
			queryString += queryParameters.get(key);
		}

		return queryString;
	}

	public String execute_old() {
	    HttpServletRequest request = this.getCurrentRequest();
	    String spaceKey = request.getParameter("spacekey");
	    String url = settingsManager.getGlobalSettings().getBaseUrl();
	    String fromJobTypeAdminPage = request.getParameter("fromJobTypeAdminPage");

	    if ("false".equals(fromJobTypeAdminPage)) {
	    	url += "/plugins/cron-for-space-admins/ManageJobs.action?key=" + spaceKey;
	    } else {
	    	url += "/plugins/cron-for-space-admins/ManageJobTypes.action";
	    }

	    try {
	    	jobService.deleteJob(request);
	    } catch (JobException e) {
	    	try {
	    		ServletActionContext.getResponse().sendRedirect(url + "&delete-error=true");
	    		return NONE;
	    	} catch (IOException e2) {
	    		return SUCCESS;
	    	}
	    }

	    try {
			ServletActionContext.getResponse().sendRedirect(url);
		} catch (IOException e) {
			return SUCCESS;
		}

		return NONE;
	}
}
