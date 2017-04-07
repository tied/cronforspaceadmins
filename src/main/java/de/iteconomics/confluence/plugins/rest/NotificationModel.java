package de.iteconomics.confluence.plugins.rest;

public class NotificationModel {

    private String recipient;
    private String title;
    private String message;

    public NotificationModel() {
    }

    public NotificationModel(String recipient, String title, String message) {
    	this.recipient = recipient;
    	this.title = title;
    	this.message = message;
    }

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
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