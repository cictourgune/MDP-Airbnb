package org.tourgune.mdp.airbnb.logger.proxy;

import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggerProxy {

	private LogManager logManager;
	private Logger rootLogger;
	
	public LoggerProxy() {
		logManager = LogManager.getLogManager();
		rootLogger = java.util.logging.Logger.getLogger("");
//		logManager.addLogger("airbnb-logger");
	}
}
