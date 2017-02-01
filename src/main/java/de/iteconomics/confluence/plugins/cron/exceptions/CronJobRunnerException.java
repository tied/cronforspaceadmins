package de.iteconomics.confluence.plugins.cron.exceptions;

public class CronJobRunnerException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 9193285844511576831L;

	public CronJobRunnerException() {}
	public CronJobRunnerException(String message) {
		super(message);
	}

}
