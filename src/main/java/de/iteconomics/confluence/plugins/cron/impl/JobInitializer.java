package de.iteconomics.confluence.plugins.cron.impl;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import com.atlassian.confluence.event.events.plugin.PluginFrameworkStartedEvent;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import de.iteconomics.confluence.plugins.cron.api.JobService;
import de.iteconomics.confluence.plugins.cron.entities.Job;

@Named
public class JobInitializer implements DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger(JobInitializer.class);
	@ComponentImport
	private EventPublisher eventPublisher;
	private JobService jobService;
	@ComponentImport
	private TransactionTemplate transactionTemplate;
	@ComponentImport
	private SpaceManager spaceManager;

	@Inject
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
		this.eventPublisher.register(this);
	}

	@Inject
	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

	@Inject
	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	@Inject
	public void setSpaceManager(SpaceManager spaceManager) {
		this.spaceManager = spaceManager;
	}

	@EventListener
	public void pluginFrameworkStartedEvent(PluginFrameworkStartedEvent event) {
		Set<Job> disabledJobs = transactionTemplate.execute(new TransactionCallback<Set<Job>>() {

			@Override
			public Set<Job> doInTransaction() {
				return jobService.getDisabledJobs();
			}
		});

		transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction() {
				logger.error("about to register jobs...");
				for (Job job: disabledJobs) {
					logger.error("registering job: " + job.getJobKey());
					jobService.registerJob(job);
				}
				return null;
			}
		});
	}

	@Override
	public void destroy() throws Exception {
		eventPublisher.unregister(this);
	}
}