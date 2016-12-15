package de.iteconomics.confluence.plugins.cron.webwork;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.atlassian.confluence.core.ConfluenceActionSupport;

import de.iteconomics.confluence.plugins.cron.api.JobTypeService;
import de.iteconomics.confluence.plugins.cron.entities.JobType;

public class ManageJobTypes extends ConfluenceActionSupport {

	/**
	 *
	 */
	private static final long serialVersionUID = -6061253789428293167L;

	@Inject
	private final JobTypeService jobTypeService;

	public ManageJobTypes(JobTypeService jobTypeService) {
		this.jobTypeService = jobTypeService;
	}

	@Override
	public String execute() {
		return SUCCESS;
	}

	public List<JobType> getAllJobTypes() {
		List<JobType> jobTypes = jobTypeService.getAllJobTypes();
		return jobTypes;
	}

	public Map<String, String> getJobTypeAttributes() {
		Map<String, String> result = new HashMap<>();
		result.put("test", "testvalue");
		return result;
	}

	// Todo: Handle exceptions, don't throw them!!
	public List<String> getJobTypeFields() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		List<String> list = new ArrayList<String>();
		list.addAll(jobTypeService.getJobTypeFieldNames());
		return list;
	}
}
