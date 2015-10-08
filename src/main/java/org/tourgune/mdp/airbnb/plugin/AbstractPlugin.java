package org.tourgune.mdp.airbnb.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.tourgune.mdp.airbnb.config.HttpRequestInfo;
import org.tourgune.mdp.airbnb.core.Context;
import org.tourgune.mdp.airbnb.core.Context.CriticalSections;
import org.tourgune.mdp.airbnb.core.Record;
import org.tourgune.mdp.airbnb.core.RecordSet;

public abstract class AbstractPlugin {
	

	protected List<String> acceptedHosts, acceptedPaths, acceptedMimes, acceptedLanguages;
	protected Set<RecordSet> uniqueTest;
	
	public AbstractPlugin() {
		this.uniqueTest = new HashSet<RecordSet>();
		
		this.acceptedHosts = new ArrayList<String>();
		this.acceptedPaths = new ArrayList<String>();
		this.acceptedMimes = new ArrayList<String>();
		this.acceptedLanguages = new ArrayList<String>();
	}
	
	/**
	 * Default implementation. Subclasses are expected to override this method.
	 * 
	 * @param rs
	 */
	public void onParse(RecordSet rs) {
		return;
	}
	
	/**
	 * Default implementation. Subclasses are expected to override this method.
	 */
	public void onPluginInit(List<RecordSet> recordSets) {
		return;
	}
	
	/**
	 * Default implementation. Subclasses are expected to override this method.
	 */
	public void onPluginEnd() {
		return;
	}
	
	/**
	 * Default implementation. Subclasses are expected to override this method.
	 * 
	 * @param hri
	 * @param content
	 * @param geographyId
	 * @return Always <code>null</code>
	 */
	public Record[] onFetch(HttpRequestInfo hri, String content, String geographyId) {
		return null;
	}
	
	public void init() {
		List<RecordSet> recordSets = new LinkedList<RecordSet>();
		
		onPluginInit(recordSets);
		
		if (!recordSets.isEmpty()) {
			for (RecordSet recordSet : recordSets) {
				if (isUnique(recordSet)) {
					// registrar las regiones que se scrappearán
					Context.getContext().store(CriticalSections.PREFETCH, recordSet);
					
					// TODO cambiar el modo en el que se registran las regiones (contador)
					Context.getContext().registerGeography(Integer.parseInt(recordSet.getInfo("geoid")));
				}
			}
		}
	}
	
	public void end() {
		onPluginEnd();
		uniqueTest.clear();
	}
	
	// TODO 'HttpRequestInfo' might be a subclass of 'Record' as well
	public Record[] parse(HttpRequestInfo hri, String html, String geographyId) {
		Record[] records = null;
		
		if (isHostAccepted(hri.getHostname()) && isPathAccepted(hri.getPath()) && isMimeAccepted(hri.getMime()) && isLangAccepted(hri.getLang()))
			records = onFetch(hri, html, geographyId);
		
		return records;
	}
	
	public void store(RecordSet rs) {
		if (isHostAccepted(rs.getInfo("hostname")) && isPathAccepted(rs.getInfo("path")))
			onParse(rs);
	}

	public void registerMime(String mimetype) {
		if (!mimetype.isEmpty()) {
			if (mimetype.equals("*"))
				acceptedMimes.clear();
			acceptedMimes.add(mimetype);
		} else
			acceptedMimes.clear();
	}
	
	public void registerMimes(String[] mimetypes) {
		for (String mimetype : mimetypes)
			registerMime(mimetype);
	}
	
	public void registerLang(String language) {
		if (!language.isEmpty()) {
			if (language.equals("*"))
				acceptedLanguages.clear();
			acceptedLanguages.add(language);
		} else
			acceptedLanguages.clear();
	}
	
	public void registerLangs(String[] languages) {
		for (String language : languages)
			registerLang(language);
	}
	
	public void registerHost(String hostname) {
		if (!hostname.isEmpty()) {
			if (hostname.equals("*"))
				acceptedHosts.clear();
			acceptedHosts.add(hostname.toLowerCase());
		} else
			acceptedHosts.clear();	// TODO emitir un warning si pasa esto
	}
	
	public void registerHosts(String[] hostnames) {
		// TODO Enviar un warning si se ha registrado '*' junto con otros nombres que no son '*'
		for (String hostname : hostnames)
			registerHost(hostname);
	}
	
	public void registerPath(String path) {
		if (!path.isEmpty()) {
			if (path.equals("*"))
				acceptedPaths.clear();
			acceptedPaths.add(path.toLowerCase());
		} else
			acceptedPaths.clear();
	}
	
	public void registerPaths(String[] paths) {
		for (String path : paths)
			registerPath(path);
	}
	
	protected boolean isLangAccepted(String language) {
		boolean accepted = false, compareGlobal = true;
		String acceptedLang = null, lang = language.toLowerCase();
		
		// TODO Enviar un warning si esto se cumple
		if (acceptedLanguages.isEmpty())
			return false;
		
		if (acceptedLanguages.get(0).equals("*"))
			return true;
		
		for (Iterator<String> it = acceptedLanguages.iterator(); it.hasNext() && !accepted;) {
			acceptedLang = it.next().toLowerCase();
			
			if (acceptedLang.endsWith("-")) {
				acceptedLang = acceptedLang.substring(0, acceptedLang.length() - 1);
				compareGlobal = false;
			}
			
			if (compareGlobal)
				accepted = acceptedLang.equals(lang);
			else
				accepted = acceptedLang.equals(lang.contains("-") ? lang.split("-")[0] : lang);
		}
		
		return accepted;
	}

	protected boolean isMimeAccepted(String mime) {
		boolean accepted = false;
		
		// TODO Enviar un warning si esto se cumple
		if (acceptedMimes.isEmpty())
			return false;
		
		if (acceptedMimes.get(0).equals("*"))
			return true;
		
		for (Iterator<String> it = acceptedMimes.iterator(); it.hasNext() && !accepted;)
			accepted = it.next().toLowerCase().equals(mime.toLowerCase());
		
		return accepted;
	}

	protected boolean isPathAccepted(String path) {
		boolean accepted = false, compareGlobal = true;
		String acceptedPath = null;
		String[] acceptedPathParts = null, pathParts = null;
		int charIndex = 0, apIndex = 0, pIndex = 0;
		
		// TODO Enviar un warning si esto se cumple
		if (acceptedPaths.isEmpty())
			return false;
		
		// TODO Al registrar un path no se tiene en cuenta el asterisco
		if (acceptedPaths.get(0).equals("*"))
			return true;
		
		for (Iterator<String> it = acceptedPaths.iterator(); it.hasNext() && !accepted;) {
			acceptedPath = it.next();
			
			if (acceptedPath.lastIndexOf((int) '/') == acceptedPath.length() - 1) {
				compareGlobal = false;
				acceptedPath = acceptedPath.substring(0, acceptedPath.length() - 1);
			}
			
			acceptedPathParts = acceptedPath.split("\\/");
			pathParts = path.toLowerCase().split("\\/");
			apIndex = 0;
			pIndex = 0;
			
			while (apIndex < acceptedPathParts.length && pIndex < pathParts.length) {
				accepted = acceptedPathParts[apIndex++].equals(pathParts[pIndex++]);
				if (!accepted)
					break;
			}
			
			if (accepted)
				if (acceptedPathParts.length != pathParts.length && compareGlobal)
					accepted = false;
		}
		
		return accepted;
	}

	protected boolean isHostAccepted(String hostname) {
		boolean accepted = false, compareGlobal = true;
		String acceptedHostname = null;
		String[] acceptedHostnameParts = null, hostnameParts = null;
		int ahIndex = 0, hIndex = 0;
		
		// TODO Enviar un warning si esto se cumple
		if (acceptedHosts.isEmpty())
			return false;
		
		if (acceptedHosts.get(0).equals("*"))
			return true;
		
		for (Iterator<String> it = acceptedHosts.iterator(); it.hasNext() && !accepted;) {
			acceptedHostname = it.next();
			
			while (acceptedHostname.charAt(0) == '.') {
				compareGlobal = false;
				acceptedHostname = acceptedHostname.substring(1);
			}
			
			acceptedHostnameParts = acceptedHostname.split("\\.");
			hostnameParts = hostname.toLowerCase().split("\\.");
			ahIndex = acceptedHostnameParts.length - 1;
			hIndex = hostnameParts.length - 1;
			
			while (ahIndex >= 0 && hIndex >= 0) {
				accepted = acceptedHostnameParts[ahIndex--].equals(hostnameParts[hIndex--]);
				if (!accepted) break;
			}
			
			if (accepted)
				if (hIndex != ahIndex && compareGlobal)
					accepted = false;
		}
		
		return accepted;
	}
	
	private boolean isUnique(RecordSet rs) {
		return uniqueTest.add(rs);
	}
}
