package de.iteconomics.confluence.plugins.cron.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import com.atlassian.confluence.event.events.space.SpaceRemoveEvent;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.entities.Job;

@Named
public class SpaceRemoveEventListener implements DisposableBean {

	@ComponentImport
	private EventPublisher eventPublisher;
	private JobService jobService;
	private static Logger logger = LoggerFactory.getLogger(SpaceRemoveEventListener.class);

	@Inject
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
		this.eventPublisher.register(this);
	}

	@Inject void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

	@EventListener
	public void spaceRemoveEvent(SpaceRemoveEvent event) {
		logger.error("space is being removed");
		Space space = event.getSpace();
		String spaceKey = space.getKey();
		logger.error("space key: " + spaceKey);
		List<Job> jobs = jobService.getJobs(spaceKey);
		for (Job job: jobs) {
			logger.error("deleting job: " + job.getName());
			jobService.deleteJob(job);
		}
	}

	@Override
	public void destroy() throws Exception {
		this.eventPublisher.unregister(this);
	}

}
