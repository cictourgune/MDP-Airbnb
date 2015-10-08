package org.tourgune.mdp.airbnb.utils;

public class Args {

	public static final void checkPositive(int n) {
		checkPositive((long) n);
	}

	public static final void checkPositiveOrZero(int n) {
		checkPositiveOrZero((long) n);
	}
	
	public static final void checkPositive(String msg, int n) {
		checkPositive(msg, (long) n);
	}
	
	public static final void checkPositiveOrZero(String msg, int n) {
		checkPositiveOrZero(msg, (long) n);
	}

	public static final void checkPositive(long n) {
		checkPositive(null, n);
	}
	
	public static final void checkPositiveOrZero(long n) {
		checkPositiveOrZero(null, n);
	}
	
	public static final void checkPositive(String msg, long n) {
		if (n <= 0) {
			if (msg == null) throw new IllegalArgumentException();
			else throw new IllegalArgumentException(msg);
		}
	}
	
	public static final void checkPositiveOrZero(String msg, long n) {
		if (n < 0) {
			if (msg == null) throw new IllegalArgumentException();
			else throw new IllegalArgumentException(msg);
		}
	}
	
	public static final void checkNotNull(Object o) {
		checkNotNull(null, o);
	}
	
	public static final void checkNotNull(String msg, Object o) {
		if (o == null) {
			if (msg == null) throw new NullPointerException();
			else throw new NullPointerException(msg);
		}
	}
}
