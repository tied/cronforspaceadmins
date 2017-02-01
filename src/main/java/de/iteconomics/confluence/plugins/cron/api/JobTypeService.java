package de.iteconomics.confluence.plugins.cron.api;

import java.util.List;

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
	void updateJobType(HttpServletRequest request);
	void setJobService(JobService jobService);
}
