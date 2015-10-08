package org.tourgune.mdp.airbnb.exception;

public class NoMoreElementsException extends Exception {
	private static final long serialVersionUID = 1L;
	public NoMoreElementsException() {
		super();
	}
	public NoMoreElementsException(String msg) {
		super(msg);
	}
}
