package org.tourgune.mdp.airbnb.logger;

import java.util.logging.Level;

public class Logger 
{
	private static Logger instance = null;
	private java.util.logging.Logger rootLogger;
	
    private Logger() {
    	System.setProperty("java.util.logging.config.class",
    			"org.tourgune.mdp.airbnb.logger.LoggerConfigurer");
    	rootLogger = java.util.logging.Logger.getLogger("airbnb-logger");
    	rootLogger.setLevel(Level.FINEST);
    	rootLogger.addHandler(new CustomConsoleHandler());
    	System.setProperty("java.util.logging.config.class", "");
    }
    
    public static synchronized Logger get() {
    	if (instance == null)
    		instance = new Logger();
		return instance;
    }
    
    public void info(String message) {
    	rootLogger.log(Level.INFO, message);
    }
    
    public void debug(String message) {
    	rootLogger.log(Level.FINEST, message);
    }
    
    public void warning(String message) {
    	rootLogger.log(Level.WARNING, message);
    }
    
    public void error(String message) {
    	rootLogger.log(Level.SEVERE, message);
    }
}
