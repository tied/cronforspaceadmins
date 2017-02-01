package de.iteconomics.confluence.plugins.cron.api;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.tx.Transactional;

import de.iteconomics.confluence.plugins.cron.entities.Job;

@Transactional
public interface JobService {

	List<Job> getAllJobs();
	void createJob(HttpServletRequest request);
	void deleteJob(HttpServletRequest request);
	void setAo(ActiveObjects ao);
	void registerAllJobs();
	boolean isEnabled(Job job);
	Set<Job> getDisabledJobs();
	void registerJob(Job job);
	void registerJob(HttpServletRequest request);
	void unregisterJob(Job job);
	void updateJob(HttpServletRequest request);
	void unregisterJob(HttpServletRequest request);
}
