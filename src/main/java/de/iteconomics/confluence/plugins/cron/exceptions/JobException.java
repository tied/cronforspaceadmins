package de.iteconomics.confluence.plugins.cron.exceptions;

public class JobException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 4239759204974642388L;

	public JobException() {}

	public JobException(String message) {
		super(message);
	}

}
