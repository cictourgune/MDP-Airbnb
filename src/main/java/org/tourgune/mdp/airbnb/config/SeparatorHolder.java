package org.tourgune.mdp.airbnb.config;

import org.tourgune.mdp.airbnb.utils.Constants;

public class SeparatorHolder {

	private String sep;
	
	public SeparatorHolder() {
		sep = Constants.CONFIG_DIRECTIVE_SEPARATOR;
	}
	
	public String getSeparator() {
		return getSeparator(false);
	}
	
	public String getSeparator(boolean escapeRegexpChars) {
		return (escapeRegexpChars ? escape(sep) : sep);
	}
	
	private String escape(String s) {
		// Cuidado con la barra invertida: debe ir siempre la primera.
		char[] regexpReservedChars = {'\\', '[', ']', '(', ')', '.'};
		for (char reservedChar : regexpReservedChars)
			s = s.replace("" + reservedChar, "\\" + reservedChar);
		return s;
	}
}
