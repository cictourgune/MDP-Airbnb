package org.tourgune.mdp.airbnb.exception;

public class DatabaseException extends Exception {
	public DatabaseException() {
		super();
	}
	public DatabaseException(String message) {
		super(message);
	}
	public DatabaseException(Throwable cause) {
		super(cause);
	}
}
