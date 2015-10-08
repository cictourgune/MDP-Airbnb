package org.tourgune.mdp.airbnb;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultHttpClientIODispatch;
import org.apache.http.impl.nio.DefaultNHttpClientConnectionFactory;
import org.apache.http.impl.nio.SSLNHttpClientConnectionFactory;
import org.apache.http.impl.nio.pool.BasicNIOConnFactory;
import org.apache.http.impl.nio.pool.BasicNIOConnPool;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.tourgune.mdp.airbnb.client.HttpClient;
import org.tourgune.mdp.airbnb.config.Config;
import org.tourgune.mdp.airbnb.config.ConfigDirective;
import org.tourgune.mdp.airbnb.core.Context;
import org.tourgune.mdp.airbnb.core.Context.CriticalSections;
import org.tourgune.mdp.airbnb.core.Director;
import org.tourgune.mdp.airbnb.core.RecordSet;
import org.tourgune.mdp.airbnb.database.AirbnbDatabase;
import org.tourgune.mdp.airbnb.exception.ConfigException;
import org.tourgune.mdp.airbnb.exception.ConfigSyntaxException;
import org.tourgune.mdp.airbnb.exception.DirectiveSyntaxException;
import org.tourgune.mdp.airbnb.plugin.Dispatcher;
import org.tourgune.mdp.airbnb.utils.Args;

public class Main {
	
	public class MyRunnable implements Runnable {
		// connection pool for the HTTP clients
		protected BasicNIOConnPool connPool;
		protected Dispatcher dispatcher;
		
		public MyRunnable(BasicNIOConnPool connPool, Dispatcher dispatcher) {
			Args.checkNotNull(connPool);
			this.connPool = connPool;
			this.dispatcher = dispatcher;
		}
		
		@Override
		public void run() {
			Context context = Context.getContext();
			RecordSet curRs = null;
			
			HttpClient httpClient = new HttpClient(connPool);
			DatabaseSource dbs = new DatabaseSource();
			AirbnbDatabase db = new AirbnbDatabase();
			
//			loadPlugins(dispatcher);
			dbs.attachDatabase(db);
			
//			dispatcher.dispatch(Dispatcher.Event.INIT, null);

			try {
				
				do {
					httpClient.work();
					
					curRs = context.get(CriticalSections.POSTFETCH);
					if (curRs != null) {
						dispatcher.dispatch(Dispatcher.Event.FETCH, curRs);
						context.store(CriticalSections.POSTPARSE, curRs);
						curRs = null;
					}
					
					curRs = context.get(CriticalSections.POSTPARSE);
					if (curRs != null) {
						dispatcher.dispatch(Dispatcher.Event.PARSE, curRs);
						curRs.clean();
						curRs = null;
					}
//					dbs.work();
				} while (context.registeredGeographies() > 0);
			} finally {
				httpClient.finish();
			}
		}
	}
	
	public class HttpClientTestRunnable extends MyRunnable {
		public HttpClientTestRunnable(BasicNIOConnPool connPool, Dispatcher dispatcher) {
			super(connPool, dispatcher);
		}
		
		@Override
		public void run() {
			Context context = Context.getContext();
			HttpClient httpClient = new HttpClient(connPool);
			
			httpClient.work();
			httpClient.work();
			
			httpClient.finish();
		}
	}
	
	public void loadPlugins(Dispatcher dispatcher) {
		Config c = Config.getInstance();
		Map<String, String> params = c.getParams("Core");
		
		// DEBUG
		System.out.println("Plugins to load:");
		// END DEBUG
		for (String plugin : params.get("load_plugins").split(",")) {
			// DEBUG
			System.out.println("\t" + plugin.trim());
			// END DEBUG
			dispatcher.loadPlugin(plugin.trim());
		}
	}
	
	public void run(BasicNIOConnPool connPool) {
	    Map<String, String> params = Config.getInstance().getParams("Core");
	    int numThreads = Integer.parseInt(params.get("worker_threads"));
	    Thread[] t = new Thread[numThreads];
	
	    Dispatcher dispatcher = new Dispatcher();
	    loadPlugins(dispatcher);
	
	    // trigger init event
	    dispatcher.dispatch(Dispatcher.Event.INIT, null);
	
	    for (int i = 0; i < numThreads; i++)
	            t[i] = new Thread(new MyRunnable(connPool, dispatcher));
	
	    for (int i = 0; i < numThreads; i++)
	            t[i].start();
	
	    for (int i = 0; i < numThreads; i++)
	            try {
	                    t[i].join();
	            } catch (InterruptedException e) {
	                    e.printStackTrace();
	            }
	
	    // trigger end event
	    dispatcher.dispatch(Dispatcher.Event.END, null);
	}
	
	public ConnectingIOReactor initReactor() throws IOReactorException {
		Map<String, String> params = Config.getInstance().getParams("Core");
		int ioReactorDispatchThreads = Integer.parseInt(params.get("reactor_threads"));
		IOReactorConfig ioConfig = IOReactorConfig.custom()
				.setIoThreadCount(ioReactorDispatchThreads)
				.build();
		
		// Create client-side HTTP protocol handler
        HttpAsyncRequestExecutor protocolHandler = new HttpAsyncRequestExecutor();
        
        // Create client-side I/O event dispatch with default connection configuration
        final IOEventDispatch ioEventDispatch = new DefaultHttpClientIODispatch(protocolHandler, ConnectionConfig.DEFAULT);
        
        // Create client-side I/O reactor
        final ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioConfig);
        
        // Run the I/O reactor in a separate thread
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    // Ready to go!
                    ioReactor.execute(ioEventDispatch);
                } catch (InterruptedIOException ex) {
                    System.err.println("Interrupted");
                } catch (IOException e) {
                    System.err.println("I/O error: " + e.getMessage());
                }
            }

        });
        t.setName("I/O Reactor thread");
        // Start the client thread
        t.start();
        
        return ioReactor;
	}
	
	private static void processCommandLine(Config conf, String[] args) {
		String name = null, value = null;
		ConfigDirective cd = null;
		
		for (String arg : args) {
			try {
				if (arg.startsWith("--") && arg.length() >= 3) {
					if (arg.contains("=")) {
						String[] parts = arg.split("=", 2);
						name = parts[0].substring(2).trim();
						value = parts[1];
					} else {
						name = arg.substring(2).trim();
						value = "true";
					}
				} else if (arg.startsWith("-") && arg.length() >= 2) {
					if (arg.length() == 2) {
						name = arg.substring(1);
						value = "true";
					} else {
						name = arg.substring(1, 2);
						value = arg.substring(3).trim();
					}
					
					name = conf.getShortOptionMapping(name);
				} else {
					// skip add option
					throw new DirectiveSyntaxException();
				}
				
				cd = new ConfigDirective(name, value);
				conf.addDirective(cd, true);
			} catch (DirectiveSyntaxException e) {
				System.out.println("[WARNING] Unrecognized argument '" + arg + "'");
			} catch (ConfigException e) {
				System.out.println("[WARNING] Skipping configuration option '" + cd.toString() + "'");
			}
		}
	}
	
	public static void main(String[] args) {
		long tsInicio = 0, tsFin = 0;
		Main main = new Main();
		Director director = new Director();
		
		try {
			// load default config file
			// TODO debería haber un listado de prioridades:
			//	1. /etc/airbnb.conf
			//  2. ./airbnb.conf
			Config conf = Config.load(new File("./airbnb.conf"));
			processCommandLine(conf, args);
			
			// initialize the director, and populate the PREFETCH context
//			director.init();
			
			// initialize the I/O reactor
			ConnectingIOReactor ioReactor = main.initReactor();
			// create a connection factory for HTTP and HTTPS
			BasicNIOConnFactory connFactory = new BasicNIOConnFactory(
					new DefaultNHttpClientConnectionFactory(ConnectionConfig.DEFAULT),
					new SSLNHttpClientConnectionFactory(ConnectionConfig.DEFAULT));
			// create a connection pool, and bind it to the I/O reactor
			// WARNING: connect timeout set to zero (0)
			BasicNIOConnPool connPool = new BasicNIOConnPool(ioReactor, connFactory, 0);
			
			// limit total number of connections
			Map<String, String> params = Config.getInstance().getParams("Core");
			int maxConnsPerRoute = Integer.parseInt(params.get("max_connections_per_route"));
			int maxConnsTotal = Integer.parseInt(params.get("max_connections_total"));
			connPool.setDefaultMaxPerRoute(maxConnsPerRoute);
			connPool.setMaxTotal(maxConnsTotal);
			
			// we're ready to start: launch the worker threads and time the process
            tsInicio = new Date().getTime();
            main.run(connPool);
            tsFin = new Date().getTime();

            // shutdown I/O reactor
            ioReactor.shutdown();

            // display total time and end
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(new Date(tsFin - tsInicio));
            System.out.println("Total time: " + (calendar.get(Calendar.HOUR) - 1) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND));
		} catch (ConfigSyntaxException e) {
			// el fichero de configuración tiene un error de sintaxis, así que salimos
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (ConfigException e) {
			e.printStackTrace();
		}
	}

}
