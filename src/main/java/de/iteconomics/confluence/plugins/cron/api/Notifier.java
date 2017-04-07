package de.iteconomics.confluence.plugins.cron.api;

import com.atlassian.mywork.model.Notification;

public interface Notifier {

	Notification sendNotification(String recipient, String title, String message);
}
