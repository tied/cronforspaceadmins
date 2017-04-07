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
    	for (String recipient: data.getRecipients().split(System.getProperty("line.separator"))) {
    		notifier.sendNotification(recipient.trim(), data.getTitle(), data.getMessage());
    	}

    	return "sending notification";
    }

}