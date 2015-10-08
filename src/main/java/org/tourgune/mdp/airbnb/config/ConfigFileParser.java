package org.tourgune.mdp.airbnb.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import org.tourgune.mdp.airbnb.exception.ConfigSyntaxException;
import org.tourgune.mdp.airbnb.utils.Args;

/**
 * @todo REFACTOR --> nextSource() and nextEntry().
 * @author Ander.Juaristi
 *
 */
public class ConfigFileParser {

	private List<String> lines;
	private int pos;
	
	public static enum EntryTypes {
		SOURCE,
		ENTRY,
		COMMENT,
		EMPTYLINE,
		UNKNOWN
	};
	
	/**
	 * Opens a file, reads it line by line and stores all the content in a buffer.
	 * Finally, closes the file.
	 * All the subsequent parsing operations are performed over the contents in the buffer,
	 * which means that if the file changes after this class has been instantiated, the changes
	 * will not be visible to the parser.
	 * 
	 * @param file The configuration file to read. It must follow a proper configuration syntax, or a ConfigSyntaxException will be thrown by subsequent method calls.
	 * @throws IOException If the file could not be read for some reason.
	 */
	public ConfigFileParser(File file) throws IOException {
		Args.checkNotNull(file);
		this.lines = Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));
		pos = 0;
	}
	
	public ConfigFileParser(List<String> lines) {
		Args.checkNotNull(lines);
		this.lines = lines;
		pos = 0;
	}
	
	/**
	 * Reads the next line and formats it as a configuration source. These have the following format:
	 * 		<code>[MyConfigSource]</code>
	 * 
	 * @return The next configuration source found, or <code>null</code> if EOF was reached.
	 * @throws ConfigSyntaxException If the next line did not match the format of a configuration source.
	 */
	public String nextSource() throws ConfigSyntaxException {
		String curLine = null;
		boolean foundMatch = false;
		int lastCharIndex = 0;
		
		while (!foundMatch) {
			if (pos < lines.size()) {
				curLine = lines.get(pos++).trim();
				switch (entryType(curLine)) {
				case SOURCE:
					lastCharIndex = curLine.length() - 1;
					if ((curLine.charAt(0) == '[') && (curLine.charAt(lastCharIndex) == ']'))
						curLine = curLine.substring(1, lastCharIndex).trim();
					else
						curLine = null;
					foundMatch = true;
					break;
				case ENTRY:
					pos--;
					curLine = null;
					foundMatch = true;
					break;
				case EMPTYLINE:
				case COMMENT:
					break;
				case UNKNOWN:
				default:
					throw new ConfigSyntaxException("Invalid syntax: '" + curLine + "'");
				}
			} else {
				curLine = null;
				foundMatch = true;
			}
		}
		
		return curLine;
	}
	
	/**
	 * Reads the next line and formats it as a configuration entry or parameter. These have the following format:
	 * 		<code>key = value</code>
	 * Linear whitespace around the equal sign is not meaningful, as well as leading and trailing whitespace.
	 * 
	 * @return An array with the layout <code>[key, value]</code>, or <code>null</code> if EOF was reached.
	 * @throws ConfigSyntaxException If the next line did not match the format of a configuration parameter.
	 */
	public String[] nextEntry() throws ConfigSyntaxException {
		String curLine = null, parts[] = null;
		boolean foundMatch = false;
		
		while (!foundMatch) {
			if (pos < lines.size()) {
				curLine = lines.get(pos++).trim();
				switch (entryType(curLine)) {
				case SOURCE:
					pos--;
					foundMatch = true;
					break;
				case ENTRY:
					parts = curLine.split("=", 2);
					if (parts.length == 2) {
						parts[0] = parts[0].trim();
						parts[1] = parts[1].trim();
					} else
						parts = null;
					foundMatch = true;
					break;
				case COMMENT:
				case EMPTYLINE:
					break;
				case UNKNOWN:
				default:
					throw new ConfigSyntaxException("Invalid syntax: '" + curLine + "'");
				}
			} else {
				parts = null;
				foundMatch = true;
			}
		}
		return parts;
	}
	
	/**
	 * 
	 * @param line must be trimmed
	 * @return
	 */
	private EntryTypes entryType(String line) {
		if (line.isEmpty())
			return EntryTypes.EMPTYLINE;
		if (line.trim().charAt(0) == '#')
			return EntryTypes.COMMENT;
		if (line.charAt(0) == '[' && line.lastIndexOf((int) ']') == line.length() - 1)
			return EntryTypes.SOURCE;
		if (line.matches("^(\\w|\\s)+=(\\w|\\s|\\p{Punct})+$"))
			return EntryTypes.ENTRY;
		return EntryTypes.UNKNOWN;
	}
}
