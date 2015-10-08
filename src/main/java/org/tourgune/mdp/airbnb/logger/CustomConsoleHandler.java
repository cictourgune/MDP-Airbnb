package org.tourgune.mdp.airbnb.logger;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class CustomConsoleHandler extends ConsoleHandler {

	public CustomConsoleHandler() {
		super();
		this.setLevel(Level.FINEST);
		this.setOutputStream(System.out);
	}
	@Override
	public void publish(LogRecord record) {
		if (super.isLoggable(record)) {
			if (record.getLevel() == Level.INFO)
				info(record);
			else if (record.getLevel() == Level.WARNING)
				warning(record);
			else if (record.getLevel() == Level.SEVERE)
				error(record);
			else
				debug(record);
		}
		
	}

	protected void info(LogRecord record) {
		String msg = this.getFormatter().formatMessage(record);
		System.out.println(msg);
	}
	
	protected void debug(LogRecord record) {
		String msg = this.getFormatter().formatMessage(record);
		System.out.println("[DEBUG] " + msg);
	}
	
	protected void warning(LogRecord record) {
		String msg = this.getFormatter().formatMessage(record);
		System.out.println("[WARNING] " + msg);
	}
	
	protected void error(LogRecord record) {
		String msg = this.getFormatter().formatMessage(record);
		System.out.println("[ERROR] " + msg);
	}

}
