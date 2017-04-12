package de.iteconomics.confluence.plugins.rest;

import de.iteconomics.confluence.plugins.cron.api.Notifier;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource of message.
 */
@Path("/notification")
public class NotificationRESTService {

	private static Logger logger = LoggerFactory.getLogger(NotificationRESTService.class);

	@Inject
	private Notifier notifier;

	public NotificationRESTService(Notifier notifier) {
		this.notifier = notifier;
	}

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.TEXT_PLAIN})
    public String getAnotherMessage(NotificationModel data, @Context UriInfo ui)
    {
    	if (data == null) {
    		return "No notification data - not sending notifications";
    	}

    	String recipients = data.getRecipients();
    	String title = data.getTitle();
    	String message = data.getMessage();

    	if (recipients == null || "".equals(recipients.trim())) {
    		return "No recipient - not sending notifications";
    	}
    	if (title == null) {
    		title = "";
    	}
    	if (message == null) {
    		message = "";
    	}

    	for (String recipient: data.getRecipients().trim().split(System.getProperty("line.separator"))) {
    		notifier.sendNotification(recipient.trim(), title.trim(), message.trim());
    	}

    	return "sending notification";
    }

}