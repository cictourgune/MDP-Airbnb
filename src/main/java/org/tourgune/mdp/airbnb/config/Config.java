package org.tourgune.mdp.airbnb.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.tourgune.mdp.airbnb.exception.ConfigException;
import org.tourgune.mdp.airbnb.exception.DirectiveSyntaxException;
import org.tourgune.mdp.airbnb.exception.DuplicatedConfigDirectiveException;
import org.tourgune.mdp.airbnb.utils.Args;

public class Config {
	
	public List<ConfigDirective> directiveList;
	
	private static Config instance = null;
	
	private final SeparatorHolder sep;
	
	private enum ShortOpts {
		DEBUG ("d", "core.debug"),
		VERBOSE ("v", "core.verbose");
		
		private final String shortName;
		private final String longName;
		
		private ShortOpts(String shortName, String longName) {
			this.shortName = shortName;
			this.longName = longName;
		}
		
		public String getShortName() {
			return shortName;
		}
		
		public String getLongName() {
			return longName;
		}
	}
	
	protected Config() {
		directiveList = new ArrayList<ConfigDirective>();
		sep = new SeparatorHolder();
	}
	
	public void addDirective(ConfigDirective cd, boolean replace) throws ConfigException {
		int index = -1;
		
		if (replace) {
			index = directiveList.indexOf(cd);
			if (index >= 0)
				directiveList.set(index, cd);
		} else
			ensureUnique(cd);
		
		if (index < 0)
			directiveList.add(cd);
	}
	
	public void addDirective(ConfigDirective cd) throws ConfigException {
		addDirective(cd, false);
	}
	
	public void addDirective(String name, String value, boolean replace) throws ConfigException {
		ConfigDirective cd = new ConfigDirective(name, value);
		addDirective(cd, replace);
	}
	
	public void addDirective(String name, String value) throws ConfigException {
		addDirective(name, value, false);
	}
	
	public void addDirective(String section, String name, String value, boolean replace) throws ConfigException {
		ConfigDirective cd = new ConfigDirective(section, name, value);
		addDirective(cd, replace);
	}
	
	public void addDirective(String section, String name, String value) throws ConfigException {
		addDirective(section, name, value, false);
	}
	
	public synchronized void addFile(File file) throws IOException, ConfigException {
		Args.checkNotNull(file);
		
		ConfigFileParser parser = new ConfigFileParser(file);
		String curSource = null, curEntry[] = null;
		
		do {
			curSource = parser.nextSource();
			if (curSource != null) {
				do {
					curEntry = parser.nextEntry();
					if (curEntry != null) {
						try {
							addDirective(curSource + sep.getSeparator() + curEntry[0], curEntry[1]);
						} catch (DuplicatedConfigDirectiveException e) {
							System.out.println("[WARNING] '" + curEntry[0] + "' will be ignored.");
						}
					}
				} while(curEntry != null);
			}
		} while(curSource != null);
	}
	
	public String getParam(String paramNameMangled) {
		try {
			for (ConfigDirective cd : directiveList)
				if (cd.getName().equals(ConfigDirective.mangleName(paramNameMangled)))
					return cd.getValue();
		} catch (DirectiveSyntaxException e) {
			// return null
		}
		
		return null;
	}
	
	public String getParam(String section, String paramNameUnmangled) {
		return getParam(section + sep.getSeparator() + paramNameUnmangled);
	}
	
	public Map<String, String> getParams(String section) {
		Map<String, String> values = new HashMap<String, String>();
		for (ConfigDirective cd : directiveList) {
			if (cd.getSection().equals(section.toLowerCase()))
				values.put(cd.getRelativeName(), cd.getValue());
		}
		return values;
	}
	
	public List<String> getParamValues(String section, String paramName, String[] defaultValues) {
		return getParamValues(section + sep.getSeparator() + paramName, defaultValues);
	}
	
	public List<String> getParamValues(String paramName, String[] defaultValues) {
		List<String> params = new ArrayList<String>();
		
		String paramValue = getParam(paramName);
		if (paramValue != null) {
			for (String param : paramValue.split(","))
				params.add(param.trim());
		}
		
		if (params.isEmpty() && defaultValues != null) {
			for (String value : defaultValues)
				params.add(value);
		}
		
		return params;
	}
	
	public void clear() {
		directiveList.clear();
	}
	
	public String getShortOptionMapping(String shortName) {
		for (ShortOpts opt : ShortOpts.values()) {
			if (opt.getShortName().equals(shortName))
				return opt.getLongName();
		}
		return null;
	}
	
	private void ensureUnique(ConfigDirective cd) throws DuplicatedConfigDirectiveException {
		if (directiveList.contains(cd))
			throw new DuplicatedConfigDirectiveException();
	}
	
	public static synchronized Config load(File configFile) throws IOException, ConfigException {
		getInstance().addFile(configFile);
		return instance;
	}
	
	public static synchronized Config getInstance() {
		if (instance == null)
			instance = new Config();
		return instance;
	}
}
