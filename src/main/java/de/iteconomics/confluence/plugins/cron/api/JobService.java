package de.iteconomics.confluence.plugins.cron.api;

import java.util.List;

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
}
