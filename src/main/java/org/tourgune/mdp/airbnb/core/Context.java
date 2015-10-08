package org.tourgune.mdp.airbnb.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Context {

	public static enum CriticalSections {
		PREFETCH,
		POSTFETCH,
		POSTPARSE
	};
	
//	public static final ContextDNS DNS = ContextDNS.getInstance();
	
	protected static Context instance = null;
	protected Map<String, String> contextKeys;
	protected Deque<UrlHtmlPair> contextSites;
	protected Deque<String> emptyContextKeys;
	
	protected Deque<RecordSet> pendingQueue, htmlQueue, parsedQueue;
	protected Lock pendingLock, htmlLock, parsedLock;
	protected Condition pendingQueueEmpty, htmlQueueEmpty, parsedQueueEmpty;
	
	protected AtomicInteger geographies;
	
	public static class UrlHtmlPair {
		public String url;
		public String html;
	}
	
	protected Context() {
		contextKeys = new HashMap<String, String>();
		contextSites = new ArrayDeque<UrlHtmlPair>();
		emptyContextKeys = new ArrayDeque<String>();
		
		pendingQueue = new ArrayDeque<RecordSet>();
		pendingLock = new ReentrantLock();
		pendingQueueEmpty = pendingLock.newCondition();
		
		htmlQueue = new ArrayDeque<RecordSet>();
		htmlLock = new ReentrantLock();
		htmlQueueEmpty = htmlLock.newCondition();
		
		parsedQueue = new ArrayDeque<RecordSet>();
		parsedLock = new ReentrantLock();
		parsedQueueEmpty = parsedLock.newCondition();
		
		geographies = new AtomicInteger(0);
	}
	
	public static Context getContext() {
		if (instance == null) {
			synchronized(Context.class) {
				instance = new Context();
			}
		}
		return instance;
	}
	
	/*
	 * TODO Cuando se introduzca algo en la zona 2 (POSTPARSE), eliminar automáticamente
	 * 		todos los Records de la zona 1 (POSTFETCH).
	 */
	public void store(CriticalSections cs, RecordSet rs) {
		Deque<RecordSet> myQueue = null;
		Lock myLock = null;
		Condition myCondition = null;
		
		switch (cs) {
		case PREFETCH:
			myLock = pendingLock;
			myQueue = pendingQueue;
			myCondition = pendingQueueEmpty;
			break;
		case POSTFETCH:
			myLock = htmlLock;
			myQueue = htmlQueue;
			myCondition = htmlQueueEmpty;
			break;
		case POSTPARSE:
			myLock = parsedLock;
			myQueue = parsedQueue;
			myCondition = parsedQueueEmpty;
			
			// elimina todos los Records con sourceId == 1 para liberar memoria
			// TODO Esto no se debería hacer así: puede haber varios sourceIds que escriban en POSTFETCH.
			//		Haría falta un emplazamiento central que maneje los sourceIds.
//			rs.removeRecords(1);
			
			break;
		}
		
		// zona crítica
		try {
			myLock.lock();
			myQueue.addLast(rs);
		} finally {
			myLock.unlock();
		}
		// fin zona crítica
	}
	
	public RecordSet get(CriticalSections cs) {
		RecordSet rs = null;
		Deque<RecordSet> myQueue = null;
		Lock myLock = null;
		Condition myCondition = null;
		
		switch (cs) {
		case PREFETCH:
			myLock = pendingLock;
			myQueue = pendingQueue;
			myCondition = pendingQueueEmpty;
			break;
		case POSTFETCH:
			myLock = htmlLock;
			myQueue = htmlQueue;
			myCondition = htmlQueueEmpty;
			break;
		case POSTPARSE:
			myLock = parsedLock;
			myQueue = parsedQueue;
			myCondition = parsedQueueEmpty;
			break;
		}
		
		// zona crítica
		try {
			myLock.lock();
			rs = myQueue.pollFirst();
		} finally {
			myLock.unlock();
		}
		// fin zona crítica
		
		return rs;
	}
	
	public int size(CriticalSections cs) {
		int size = 0;
		Deque<RecordSet> myDeque = null;
		Lock myLock = null;
		
		switch(cs) {
		case PREFETCH:
			myLock = pendingLock;
			myDeque = pendingQueue;
			break;
		case POSTFETCH:
			myLock = htmlLock;
			myDeque = htmlQueue;
			break;
		case POSTPARSE:
			myLock = parsedLock;
			myDeque = parsedQueue;
			break;
		}
		
		// zona crítica
		try {
			myLock.lock();
			size = myDeque.size();
		} finally {
			myLock.unlock();
		}
		// fin zona crítica
		
		return size;
	}
	
	public void registerGeography(int idGeography) {
		geographies.incrementAndGet();
	}
	
	public void unregisterGeography(int idGeography) {
		geographies.decrementAndGet();
	}
	
	public int registeredGeographies() {
		return geographies.get();
	}
}