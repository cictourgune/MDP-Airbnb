package org.tourgune.mdp.airbnb.logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.logging.LogManager;

public class LoggerConfigurer {
	public LoggerConfigurer() throws SecurityException, IOException {
		System.out.println("Configuring root logger...");
		
		final StringReader sr = new StringReader(buildConfigString());
		final LogManager lm = LogManager.getLogManager();
		
		lm.readConfiguration(new InputStream() {
			@Override
			public int read() throws IOException {
				return sr.read();
			}
		});
	}
	
	private String buildConfigString() {
		StringBuilder sb = new StringBuilder();
		
//		sb.append("handlers = java.util.logging.ConsoleHandler\n");
		sb.append("java.util.logging.ConsoleHandler.level=FINEST");
		sb.append("airbnb-logger.useParentHandlers=false");
		
		return sb.toString();
	}
}
