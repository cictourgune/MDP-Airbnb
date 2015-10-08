package org.tourgune.mdp.airbnb.logger;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LoggerTest {

	@Test
	public void testLog() {
		Logger logger = Logger.get();
		logger.info("This is an INFO message!");
		logger.warning("This is a WARNING message!");
		logger.error("This is an ERROR message!");
		logger.debug("This is a DEBUG message!");
		assertTrue(true);
	}

}
