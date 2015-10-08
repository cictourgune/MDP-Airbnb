package org.tourgune.mdp.airbnb.client;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.tourgune.mdp.airbnb.config.HttpRequestInfo;
import org.tourgune.mdp.airbnb.utils.Constants;
import org.tourgune.mdp.airbnb.utils.Utils;

public class CompletedFutureCallback implements FutureCallback<HttpResponse> {
	private HttpRequestInfo httpRequestInfo;
	private HttpHost targetHost;
	private String geographyId;
	private String checkinDate, checkoutDate, lengthOfStay, guests;
	private AtomicBoolean completed;
	
	private int lastResponseCode;
	private String lastHtml;
	private String lastContentType;
	private String lastLanguage;
	
	public CompletedFutureCallback(HttpHost targetHost, HttpRequestInfo httpRequestInfo, String geographyId, String checkinDate, String checkoutDate, String lengthOfStay, String guests) {
		this.targetHost = targetHost;
		this.httpRequestInfo = httpRequestInfo;
		this.geographyId = geographyId;
		this.checkinDate = checkinDate;
		this.checkoutDate = checkoutDate;
		this.lengthOfStay = lengthOfStay;
		this.guests = guests;
		this.completed = new AtomicBoolean(false);
		this.lastResponseCode = 0;
		this.lastHtml = null;
		this.lastContentType = null;
		this.lastLanguage = null;
	}
	
	@Override
	public void completed(HttpResponse result) {
		HttpEntity entity = result.getEntity();
		// DEBUG
//		System.out.println(targetHost + " completed --> " + result.getStatusLine());
//		System.out.println("\tContent-Encoding: " + entity.getContentEncoding());
//		System.out.println("\tContent-Type: " + entity.getContentType());
		// END DEBUG
		try {
			String contentType = entity.getContentType() == null
					? Constants.HTTP_DEFAULT_CONTENT_TYPE
							: entity.getContentType().getValue().split(";", 2)[0];
			String lang = result.containsHeader(Constants.HTTP_CONTENT_LANG_HEADER)
					? result.getHeaders(Constants.HTTP_CONTENT_LANG_HEADER)[0].getValue()
							: Constants.HTTP_DEFAULT_CONTENT_LANG;
			String encoding = result.containsHeader(Constants.HTTP_CONTENT_ENCODING_HEADER)
					? result.getHeaders(Constants.HTTP_CONTENT_ENCODING_HEADER)[0].getValue()
							: Constants.HTTP_DEFAULT_CONTENT_ENCODING;
			String html = "";
			if (encoding.equalsIgnoreCase(Constants.HTTP_DEFAULT_CONTENT_ENCODING))
				html = EntityUtils.toString(entity);
			else
				html = Utils.decompress(EntityUtils.toByteArray(entity), encoding);
			
			EntityUtils.consume(entity);
			// DEBUG
//			System.out.println(html.substring(0, 20));	// printa los primeros 20 chars
			// END DEBUG
			this.lastHtml = html;
			this.lastResponseCode = result.getStatusLine().getStatusCode();
			this.lastContentType = contentType;
			this.lastLanguage = lang;
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		completed.set(true);
	}
	
	@Override
	public void failed(Exception ex) {
		System.out.println(targetHost + " failed: " + ex.getMessage());
		completed.set(true);
	}
	
	@Override
	public void cancelled() {
		System.out.println(targetHost + " cancelled.");
		completed.set(true);
	}

	public HttpRequestInfo getHttpRequestInfo() {
		return httpRequestInfo;
	}

	public HttpHost getTargetHost() {
		return targetHost;
	}
	
	public String getGeographyId() {
		return geographyId;
	}

	public void setGeographyId(String geographyId) {
		this.geographyId = geographyId;
	}

	public String getCheckinDate() {
		return checkinDate;
	}

	public String getCheckoutDate() {
		return checkoutDate;
	}

	public String getLengthOfStay() {
		return lengthOfStay;
	}
	
	public String getNumGuests() {
		return guests;
	}

	public String getHtml() {
		return lastHtml;
	}
	
	public int getResponseCode() {
		return lastResponseCode;
	}
	
	public String getContentType() {
		return lastContentType;
	}
	
	public String getLanguage() {
		return lastLanguage;
	}

	public boolean isCompleted() {
		return completed.get();
	}
}
