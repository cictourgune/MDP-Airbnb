package org.tourgune.mdp.airbnb.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.tourgune.mdp.airbnb.utils.Args;

public class HttpRequestInfo {
	private String scheme, hostname, path, fragment;
	private Map<String, String> queryMap;
	private int port;
	private String mimetype;
	private String language;
	private String checkin, checkout;
	private String lengthOfStay;
	private String guests;
	
	public HttpRequestInfo(String scheme, String hostname, int port, String path) {
		this.scheme = scheme;
		this.hostname = hostname;
		this.port = port;
		this.path = null;
		this.fragment = null;
		this.queryMap = new HashMap<String, String>();
		this.mimetype = null;
		this.language = null;
		this.checkin = null;
		this.checkout = null;
		this.lengthOfStay = null;
		
		updateParams(path);
	}

	public void setMime(String mimetype) {
		this.mimetype = mimetype;
	}
	public String getMime() {
		return mimetype;
	}
	
	public void setWholePath(String path) {
		updateParams(path);
	}
	public String getWholePath() {
		StringBuilder sb = new StringBuilder(path);
		
		if (!queryMap.isEmpty())
			sb.append(implodeQueryMap());
		
		if (fragment != null)
			sb.append("#" + fragment);
		
		return sb.toString();
	}
	
	public void setLang(String language) {
		this.language = language;
	}
	public String getLang() {
		return language;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getScheme() {
		return scheme;
	}

	public String getHostname() {
		return hostname;
	}

	public void setFragment(String fragment) {
		this.fragment = fragment;
	}
	public String getFragment() {
		return fragment;
	}

	public int getPort() {
		return port;
	}
	
	public String getCheckinDate() {
		return checkin;
	}

	public void setCheckinDate(String checkin) {
		this.checkin = checkin;
	}

	public String getCheckoutDate() {
		return checkout;
	}

	public void setCheckoutDate(String checkout) {
		this.checkout = checkout;
	}

	public String getLengthOfStay() {
		return lengthOfStay;
	}

	public void setLengthOfStay(String lengthOfStay) {
		this.lengthOfStay = lengthOfStay;
	}

	public String getGuests() {
		return guests;
	}

	public void setGuests(String guests) {
		this.guests = guests;
	}

	public void setQuery(String query) {
		Args.checkNotNull(query);
		computeQueryMap(query);
	}
	public HttpRequestInfo appendQuery(String key, String value) {
		Args.checkNotNull(key);
		Args.checkNotNull(value);
		queryMap.put(key, value);
		return this;
	}
	public HttpRequestInfo appendQuery(String query) {
		String kv[] = null;
		Args.checkNotNull(query);
		for (String params : query.split("&")) {
			if (params.indexOf((int) '=') >= 0) {
				kv = params.split("=");
				appendQuery(kv[0], kv[1]);
			}
		}
		return this;
	}
	public Map<String, String> getQuery() {
		return queryMap;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(scheme + "://" + hostname);
		
		// si el puerto no es el predeterminado, hay que explicitarlo
		if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443))
			sb.append(":" + port);
		
		sb.append(getWholePath());
		
		return sb.toString();
	}
	
	private void updateParams(String path) {
		splitWholePath(path);
	}
	
	private void splitWholePath(String path) {
		String query = null;
		String[] parts = null;
		
		if ((path.indexOf((int) '?') != -1) && (path.indexOf((int) '#') != -1)) {
			parts = path.split("[\\?#]");
			this.path = relativePath(parts[0]);
			query = parts[1];
			this.fragment = parts[2];
		} else if (path.indexOf((int) '?') != -1) {
			parts = path.split("\\?");
			this.path = relativePath(parts[0]);
			query = parts[1];
			this.fragment = null;
		} else if (path.indexOf((int) '#') != -1) {
			parts = path.split("#");
			this.path = relativePath(parts[0]);
			query = null;
			this.fragment = parts[1];
		} else {
			this.path = relativePath(path);
			query = null;
			this.fragment = null;
		}
		
		computeQueryMap(query);		// update the query map
	}
	
	private String relativePath(String wholePath) {
		return wholePath.substring(wholePath.indexOf((int) '/'));
	}
	
	private void computeQueryMap(String query) {
		String[] queryParams = null, kv = null;
		if (query != null) {
			queryParams = query.split("[&;]");
			for (String param : queryParams) {
				if (param.indexOf((int) '=') >= 0) {
					kv = param.split("=");
					queryMap.put(kv[0], kv[1]);
				}
			}
		}
	}
	
	private String implodeQueryMap() {
		String key = null, value = null;
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> keyIt = queryMap.keySet().iterator(); keyIt.hasNext();) {
			key = keyIt.next();
			value = queryMap.get(key);
			if (value != null && !value.isEmpty() && !key.isEmpty()) {
				sb.append(key + "=" + value);
				if (keyIt.hasNext())
					sb.append("&");
			}
		}
		if (sb.length() > 0)
			sb.insert(0, '?');
		return sb.toString();
	}
}
