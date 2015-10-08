package org.tourgune.mdp.airbnb.test;
/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */



import java.io.IOException;
import java.net.Socket;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;

/**
 * Elemental example for executing multiple GET requests sequentially.
 */
public class ElementalHttpGet {

	/**
	 * Adds to our requests the 'Accept' header field.
	 * @author Ander.Juaristi
	 *
	 */
	public static class RequestAccept implements HttpRequestInterceptor {
		public static final String ACCEPTED_TYPES = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
		public static final String ACCEPTED_LANGUAGES = "en,en-us;q=0.8,es-es;q=0.5,es;q=0.3";
		
		@Override
		public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
			Args.notNull(request, "HTTP request");
			
			if (request.getRequestLine().getMethod().equalsIgnoreCase("GET")) {
				if (!request.containsHeader("Accept"))
					request.addHeader("Accept", ACCEPTED_TYPES);
				if (!request.containsHeader("Accept-Language"))
					request.addHeader("Accept-Language", ACCEPTED_LANGUAGES);
				if (!request.containsHeader("Accept-Encoding"))
					request.addHeader("Accept-Encoding", "identity");
				if (!request.containsHeader("Content-Length"))
					request.addHeader("Content-Length", "0");
			}
		}
	}
	public static class RequestConnControl implements HttpRequestInterceptor {

		@Override
		public void process(HttpRequest req, HttpContext con) throws HttpException, IOException {
			Args.notNull(req, "HTTP request");
			
			if (req.getRequestLine().getMethod().equalsIgnoreCase("CONNECT"))
				return;
			
			if (!req.containsHeader(HTTP.CONN_DIRECTIVE))
				req.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
		}
	}
	
    public static void main(String[] args) throws Exception {
        HttpProcessor httpproc = HttpProcessorBuilder.create()
            .add(new RequestContent())	// no hace nada, ya que no estamos enviando ninguna entidad en nuestras peticiones
            .add(new RequestTargetHost())
            .add(new ElementalHttpGet.RequestAccept())
            .add(new org.apache.http.protocol.RequestConnControl())
//            .add(new ElementalHttpGet.RequestConnControl())
            .add(new RequestUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0"))
            .add(new RequestExpectContinue(true)).build();

        HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

        HttpCoreContext coreContext = HttpCoreContext.create();
        HttpHost host = new HttpHost("www.booking.com", 80);
        coreContext.setTargetHost(host);

        DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(8 * 1024);
        ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE;

        try {

        	String[] targets = {"/"};
//            String[] targets = {
//                    "/",
//                    "/servlets-examples/servlet/RequestInfoExample",
//                    "/somewhere%20in%20pampa"};

        	Socket socket = null;
        	
            for (int i = 0; i < 1; i++) {
            	// NOTA: conn.isOpen() siempre devolverá true una vez se haya invocado bind().
            	// Incluso aunque el socket que está por debajo se haya cerrado.
                if (!conn.isOpen()) {
                    socket = new Socket(host.getHostName(), host.getPort());
                    conn.bind(socket);
//                	socket = SSLSocketFactory.getDefault().createSocket(host.getHostName(), host.getPort());
//                	conn.bind(socket);
                }
                if(socket != null && !socket.isClosed()) {
	                BasicHttpRequest request = new BasicHttpRequest("GET", targets[0]);
	                System.out.println(">> Request URI: " + request.getRequestLine().getUri());
	
	                httpexecutor.preProcess(request, httpproc, coreContext);
	                // DEBUG
	                System.out.println(">> Request headers:");
	                for (Header header : request.getAllHeaders())
	                	System.out.println(header.toString());
	                // END DEBUG
	                HttpResponse response = httpexecutor.execute(request, conn, coreContext);
	                httpexecutor.postProcess(response, httpproc, coreContext);
	                // DEBUG
	                System.out.println("<< Response headers:");
	                for (Header header : response.getAllHeaders())
	                	System.out.println(header.toString());
	                // END DEBUG
	
	                System.out.println("<< Response: " + response.getStatusLine());
	                
	                HttpEntity entity = response.getEntity();
	                System.out.println(entity.toString());	// 'ChunkedInputStream' se encarga de tratar los transfer-codings de manera automática :D
	                
//	                InputStream is = entity.getContent();
//	                for (int myByte = is.read(); myByte != -1;)
//	                	System.out.print((char) myByte);
	                
	                System.out.println(EntityUtils.toString(entity));
	                System.out.println("==============");
	                if (!connStrategy.keepAlive(response, coreContext)) {
//	                	socket.close();
	                	// conn.close() también cierra el socket que está por debajo, así que no es necesario que lo cerremos nosotros
	                	// Llamar a conn.close() provocará que posteriormente una llamada a conn.isOpen() devuelva false.
	                    conn.close();
	                } else {
	                    System.out.println("Connection kept alive...");
	                }
	                
	                EntityUtils.consume(entity);	// IMPORTANTE: asegurarse de haber consumido la entidad antes de mandar otra petición
                }
            }
        } finally {
            conn.close();
            System.out.println("Connection closed.");
        }
    }

}
