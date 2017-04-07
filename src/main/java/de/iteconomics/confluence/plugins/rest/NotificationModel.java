package de.iteconomics.confluence.plugins.rest;

public class NotificationModel {

    private String recipients;
    private String title;
    private String message;

    public NotificationModel() {
    }

    public NotificationModel(String recipients, String title, String message) {
    	this.recipients = recipients;
    	this.title = title;
    	this.message = message;
    }

	public String getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}