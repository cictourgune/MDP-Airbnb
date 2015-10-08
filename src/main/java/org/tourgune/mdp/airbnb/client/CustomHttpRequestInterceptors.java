package org.tourgune.mdp.airbnb.client;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.tourgune.mdp.airbnb.utils.Constants;

public class CustomHttpRequestInterceptors {
	public static class RequestAcceptedTypes implements HttpRequestInterceptor {
		private String acceptedTypes;
		private String acceptedLangs;
		
		public RequestAcceptedTypes(String acceptedTypes, String acceptedLangs) {
			this.acceptedTypes = acceptedTypes;
			this.acceptedLangs = acceptedLangs;
		}
		
		@Override
		public void process(org.apache.http.HttpRequest request, HttpContext context) throws HttpException, IOException {
			if (!request.containsHeader(Constants.HTTP_ACCEPT_HEADER))
				request.addHeader(Constants.HTTP_ACCEPT_HEADER, acceptedTypes);
			if (!request.containsHeader(Constants.HTTP_ACCEPT_LANG_HEADER))
				request.addHeader(Constants.HTTP_ACCEPT_LANG_HEADER, acceptedLangs);
		}
	}
	
	public static class RequestAcceptedCodings implements HttpRequestInterceptor {
		private String acceptedCodings;
		
		public RequestAcceptedCodings(String acceptedCodings) {
			this.acceptedCodings = acceptedCodings;
		}
		
		@Override
		public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
			if (!request.containsHeader(Constants.HTTP_ACCEPT_ENCODING_HEADER))
				request.addHeader(Constants.HTTP_ACCEPT_ENCODING_HEADER, acceptedCodings);
		}
	}
}
