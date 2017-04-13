package de.iteconomics.confluence.plugins.rest;

import de.iteconomics.confluence.plugins.cron.api.Notifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.user.Group;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.Sets;

/**
 * A resource of message.
 */
@Path("/notification")
@Scanned
public class NotificationRESTService {

	private static Logger logger = LoggerFactory.getLogger(NotificationRESTService.class);

	private Notifier notifier;
	@ComponentImport
	private UserAccessor userAccessor;

	@Inject
	public NotificationRESTService(Notifier notifier, UserAccessor userAccessor) {
		this.notifier = notifier;
		this.userAccessor = userAccessor;
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

    	logger.error("recipients: " + recipients);

    	if (recipients == null || "".equals(recipients.trim())) {
    		return "No recipient - not sending notifications";
    	}
    	if (title == null) {
    		title = "";
    	}
    	if (message == null) {
    		message = "";
    	}

    	for (String recipient: getRecipients(data.getRecipients())) {
    		logger.error("notify recipients: " + recipient);
    		notifier.sendNotification(recipient.trim(), title.trim(), message.trim());
    	}

    	return "sending notification";
    }

    private Set<String> getRecipients(String recipientsString) {
    	String[] recipients = recipientsString.trim().split(System.getProperty("line.separator"));
    	Set<String> uniqueRecipients = new HashSet<>();

    	for (String recipient: recipients) {
    		recipient = recipient.trim();
    		if (isGroup(recipient)) {
    			for (String member: getMembers(recipient)) {
    				uniqueRecipients.add(member);
    			}
    		} else if (isUser(recipient)){
    			uniqueRecipients.add(recipient);
    		}
    	}

    	return uniqueRecipients;
    }

	private boolean isUser(String recipient) {
		return userAccessor.getUserByName(recipient) != null;
	}

	private List<String> getMembers(String recipient) {
		Group group = userAccessor.getGroup(recipient);
		List<String> members = userAccessor.getMemberNamesAsList(group);
		return members;
	}

	private boolean isGroup(String recipient) {
		return (userAccessor.getGroup(recipient) != null);
	}

}