package de.iteconomics.confluence.plugins.cron.api;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;

import de.iteconomics.confluence.plugins.cron.entities.JobType;;

@Transactional
public interface JobTypeService {

	List<JobType> getAllJobTypes();
	void createJobType(HttpServletRequest request);
	void deleteJobType(HttpServletRequest request);
	void setAo(ActiveObjects ao);
	JobType getJobTypeByID(String id);
	Map<String, String> getJobTypeAttributes(JobType jobType) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	List<String> getJobTypeFieldNames();
}
