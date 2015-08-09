package com.github.zouzhberk.cli;

public class CliException extends RuntimeException {

	public CliException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CliException(String message, Throwable cause) {
		super(message, cause);
	}

	public CliException(String message) {
		super(message);
	}

	public CliException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
