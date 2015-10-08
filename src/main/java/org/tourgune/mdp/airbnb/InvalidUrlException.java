package org.tourgune.mdp.airbnb;

public class InvalidUrlException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6248638924455698945L;

	public InvalidUrlException() {
		super();
	}

	public InvalidUrlException(String message) {
		super(message);
	}
}
