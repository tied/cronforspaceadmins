package de.iteconomics.confluence.plugins.cron.api;

import com.atlassian.mywork.model.Notification;

public interface Notifier {

	Notification sendNotification(String username, String message);
}
