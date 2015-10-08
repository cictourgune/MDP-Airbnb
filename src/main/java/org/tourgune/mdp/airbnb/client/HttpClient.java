package org.tourgune.mdp.airbnb.client;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.impl.nio.pool.BasicNIOConnPool;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequester;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.tourgune.mdp.airbnb.client.CustomHttpRequestInterceptors.RequestAcceptedCodings;
import org.tourgune.mdp.airbnb.client.CustomHttpRequestInterceptors.RequestAcceptedTypes;
import org.tourgune.mdp.airbnb.config.Config;
import org.tourgune.mdp.airbnb.config.HttpRequestInfo;
import org.tourgune.mdp.airbnb.core.Context;
import org.tourgune.mdp.airbnb.core.Context.CriticalSections;
import org.tourgune.mdp.airbnb.core.Record;
import org.tourgune.mdp.airbnb.core.RecordSet;
import org.tourgune.mdp.airbnb.utils.Constants;

public class HttpClient {
	
	private List<CompletedFutureCallback> pendingRequests;
	
	private HttpProcessorBuilder httpProcBuilder;
	private BasicNIOConnPool connPool;
	
	public HttpClient(BasicNIOConnPool connPool) {
		Config c = Config.getInstance();
		Map<String, String> params = c.getParams("Client");
		
		String userAgent = params.get("user_agent");
		String acceptedTypes = params.get("accepted_types");
		String acceptedLanguages = params.get("accepted_languages");
		String acceptedCodings = params.get("accepted_codings");
		
		pendingRequests = new LinkedList<CompletedFutureCallback>();
		httpProcBuilder = HttpProcessorBuilder.create()
                // Use standard client-side protocol interceptors
                .add(new RequestContent())
                .add(new RequestTargetHost())
                .add(new RequestConnControl())
                .add(new RequestUserAgent(userAgent))
                /* for each acceptedType in acceptedTypes */
                // TODO acceptedTypes y acceptedLanguages deberían cogerse desde las configuraciones de los plugins
                .add(new RequestAcceptedTypes(acceptedTypes, acceptedLanguages))
                /* end */
                .add(new RequestAcceptedCodings(acceptedCodings))
                .add(new RequestExpectContinue(true));
		this.connPool = connPool;
	}
	
	public boolean isWorking() {
		return !pendingRequests.isEmpty();
	}
	
	public void finish() {
		pendingRequests.clear();
	}
	
	/*
	 * TODO Añadir soporte para redirecciones 3xx
	 */
	public void work() {
		for (Iterator<CompletedFutureCallback> it = pendingRequests.iterator(); it.hasNext();) {
			CompletedFutureCallback cur = it.next();
			if (cur.isCompleted()) {
				switch (cur.getResponseCode()) {
				case 200:
					HttpRequestInfo httpReqInfo = cur.getHttpRequestInfo();
					RecordSet rs = new RecordSet();
					rs.putInfoKey(Constants.FIELD_SCHEME, httpReqInfo.getScheme());
					rs.putInfoKey(Constants.FIELD_HOSTNAME, httpReqInfo.getHostname());
					rs.putInfoKey(Constants.FIELD_PORT, "" + httpReqInfo.getPort());
					rs.putInfoKey(Constants.FIELD_PATH, httpReqInfo.getWholePath());
					rs.putInfoKey(Constants.FIELD_GEOID, cur.getGeographyId());
					rs.putInfoKey(Constants.FIELD_CHECKIN, cur.getCheckinDate());
					rs.putInfoKey(Constants.FIELD_CHECKOUT, cur.getCheckoutDate());
					rs.putInfoKey(Constants.FIELD_LENGTH_OF_STAY, cur.getLengthOfStay());
					rs.putInfoKey(Constants.FIELD_NUM_GUESTS, cur.getNumGuests());
					Record r = new Record();
					r.addString(httpReqInfo.toString())
						.addString(cur.getContentType())	// mime type
						.addString(cur.getLanguage())
						.addString(cur.getHtml());
					rs.addRecord(r);
					Context.getContext().store(CriticalSections.POSTFETCH, rs);
					// DEBUG
					System.out.println("[" + Thread.currentThread().getName() + "] " + cur.getHttpRequestInfo().toString() + " completed --> " + cur.getResponseCode());
					// END DEBUG
					break;
				default:
					// DEBUG
					System.out.println(cur.getHttpRequestInfo().toString() + " returned --> " + cur.getResponseCode());
					// END DEBUG
					break;
				}
				it.remove();
			}
		}
		
		RecordSet rs = Context.getContext().get(CriticalSections.PREFETCH);
		
		if (rs != null) {
			HttpRequestInfo httpReqInfo = new HttpRequestInfo(
					rs.getInfo(Constants.FIELD_SCHEME),
					rs.getInfo(Constants.FIELD_HOSTNAME),
					Integer.parseInt(rs.getInfo(Constants.FIELD_PORT)),
					rs.getInfo(Constants.FIELD_PATH));
			HttpHost host = new HttpHost(
					httpReqInfo.getHostname(),
					httpReqInfo.getPort(),
					httpReqInfo.getScheme());
			
			CompletedFutureCallback callback = new CompletedFutureCallback(host, httpReqInfo,
					rs.getInfo(Constants.FIELD_GEOID),
					rs.getInfo(Constants.FIELD_CHECKIN),
					rs.getInfo(Constants.FIELD_CHECKOUT),
					rs.getInfo(Constants.FIELD_LENGTH_OF_STAY),
					rs.getInfo(Constants.FIELD_NUM_GUESTS));
			pendingRequests.add(callback);
			
			newRequest(callback);
		}
	}
	
	private void newRequest(CompletedFutureCallback callback) {
		BasicHttpRequest httpRequest = new BasicHttpRequest(Constants.HTTP_REQUEST_VERB, callback.getHttpRequestInfo().getWholePath());
		HttpCoreContext context = HttpCoreContext.create();
		HttpAsyncRequester requester = new HttpAsyncRequester(httpProcBuilder.build());
		
		requester.execute(
				new BasicAsyncRequestProducer(callback.getTargetHost(), httpRequest),
				new BasicAsyncResponseConsumer(),
				connPool,
				context,
				callback);
	}
}
