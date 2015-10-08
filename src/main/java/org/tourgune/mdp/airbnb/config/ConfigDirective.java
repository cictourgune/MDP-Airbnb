package org.tourgune.mdp.airbnb.config;

import org.tourgune.mdp.airbnb.exception.DirectiveSyntaxException;
import org.tourgune.mdp.airbnb.utils.Constants;

public class ConfigDirective {

	private String section;
	private String name;
	private String relativeName;
	private String value;
	
	private static final SeparatorHolder sep = new SeparatorHolder();
	
	public ConfigDirective(String name, String value) throws DirectiveSyntaxException {
		this.name = mangleName(name);
		this.value = value;
		
		if (this.name.contains(sep.getSeparator())) {
			String[] parts = this.name.split(sep.getSeparator(true), 2);
			this.section = parts[0];
			this.relativeName = parts[1];
		} else
			throw new DirectiveSyntaxException("Could not tell section name");
	}
	
	public ConfigDirective(String section, String relativeName, String value) throws DirectiveSyntaxException {
		this.section = section;
		this.name = mangleName(section, relativeName);
		this.relativeName = relativeName;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof ConfigDirective) {
			ConfigDirective otherCd = (ConfigDirective) obj;
			isEqual = otherCd.getName().equals(this.getName());
		}
		return isEqual;
	}

	@Override
	public String toString() {
		return "[" + section + "] " + name + " = " + value;
	}

	public String getSection() {
		return section;
	}
	
	public String getName() {
		return name;
	}
	
	public String getRelativeName() {
		return relativeName;
	}
	
	public String getValue() {
		return value;
	}
	
	/**
	 * Algorithm:
	 * 	1. Strip leading dashes.
	 * 	2. Check for more leading dashes.
	 * 	3. Normalize dashes and underscores.
	 * 	4. Put everything in lowercase.
	 * 
	 * @param directiveName
	 * @return
	 * @throws DirectiveSyntaxException
	 */
	public static String mangleName(String directiveName) throws DirectiveSyntaxException {
		String mangled = directiveName.startsWith("--") ? directiveName.substring(2) : directiveName;
		
		if (mangled.startsWith("-"))
			throw new DirectiveSyntaxException("Directive names cannot start with a dash (-)");
		
		mangled = mangled.replace("-", "_");
		mangled = mangled.toLowerCase();
		
		return mangled;
	}
	
	public static String mangleName(String section, String directiveName) throws DirectiveSyntaxException {
		return mangleName(section + sep + mangleName(directiveName));
	}
}
