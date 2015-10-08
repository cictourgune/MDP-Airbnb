package org.tourgune.mdp.airbnb.test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.http.HttpException;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.nio.DefaultNHttpClientConnection;
import org.apache.http.impl.nio.reactor.IOSessionImpl;
import org.apache.http.impl.nio.reactor.SessionClosedCallback;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.nio.reactor.IOSession;


public class TestNbHttp {

	private static class MySessionClosedCallback implements SessionClosedCallback {
		@Override
		public void sessionClosed(IOSession session) {
			System.err.println("[MySessionClosedCallback] Session was closed.");
		}
	}
	
	public static void main(String[] args) {
		try {
			MySessionClosedCallback scc = new TestNbHttp.MySessionClosedCallback();
			BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("GET", "/");
			BasicHttpEntity entity = new BasicHttpEntity();
			
			System.out.println("Request entity: " + (request.getEntity() == null ? "null" : request.getEntity().toString()));
			
			InetSocketAddress addr = new InetSocketAddress("www.burgersoftware.es", 80);
			SocketChannel socket = SocketChannel.open(addr);
			socket.configureBlocking(false);
			SelectionKey sk = socket.register(Selector.open(), socket.validOps(), null);
			
			IOSessionImpl ioSess = new IOSessionImpl(sk, scc);
			DefaultNHttpClientConnection nbConn =
					new DefaultNHttpClientConnection(ioSess, 8*1024);
			
			nbConn.submitRequest(request);
			
			while(!nbConn.isRequestSubmitted());
			nbConn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
