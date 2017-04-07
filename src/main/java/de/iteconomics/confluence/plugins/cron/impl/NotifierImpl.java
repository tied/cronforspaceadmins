package de.iteconomics.confluence.plugins.cron.impl;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iteconomics.confluence.plugins.cron.api.Notifier;

import com.atlassian.mywork.service.LocalNotificationService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.mywork.model.Notification;
import com.atlassian.mywork.model.NotificationBuilder;


@Named
public class NotifierImpl implements Notifier {

	private static Logger logger = LoggerFactory.getLogger(NotifierImpl.class);
	@ComponentImport
	private LocalNotificationService notificationService;
	private static final String PLUGIN_KEY = "de.iteconomics.de.confluence.plugins.cron";

	@Inject
	public NotifierImpl(LocalNotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Override
	public Notification sendNotification(String recipient, String title, String message) {
		logger.error("Notifying...");
		Notification notification = null;
		try {
			notification = notificationService.createOrUpdate(recipient, new NotificationBuilder()
			        .application(PLUGIN_KEY) // a unique key that identifies your plugin
			        .title(title)
			        .itemTitle(title)
			        .description(message)
			        .groupingId("de.iteconomics.de.confluence.plugins.cron") // a key to aggregate notifications
			        .createNotification()).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return notification;
	}

}
