package de.iteconomics.confluence.plugins.cron.api;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.scheduler.SchedulerService;

import de.iteconomics.confluence.plugins.cron.entities.Job;

@Transactional
public interface JobService {

	List<Job> getAllJobs();
	void createJob(HttpServletRequest request);
	void deleteJob(HttpServletRequest request);
	void setAo(ActiveObjects ao);
	void registerJob(Job job);
	void registerJob(HttpServletRequest request);
	void registerAllJobs();
	boolean isEnabled(Job job);
	Set<Job> getDisabledJobs();
	void unregisterJob(Job job);
	void updateJob(HttpServletRequest request);
	void unregisterJob(HttpServletRequest request);
	List<Job> getJobs(String spaceKey);
	List<Job> getJobsByJobTypeID(int jobTypeID);
	void deleteJob(Job job);
	void setJobTypeService(JobTypeService jobTypeService);
	void setSchedulerService(SchedulerService schedulerService);
	Job getJobIfExists(String id);
	boolean isParametersInconsistent(Job job);
}
