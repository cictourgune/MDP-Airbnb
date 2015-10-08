package org.tourgune.mdp.airbnb.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.tourgune.mdp.airbnb.core.Context;
import org.tourgune.mdp.airbnb.core.Context.CriticalSections;
import org.tourgune.mdp.airbnb.core.Record;
import org.tourgune.mdp.airbnb.core.RecordSet;

public class ContextConcurrentTest {
	
	private static AtomicInteger counter = new AtomicInteger(0);
	private static AtomicBoolean firstTime = new AtomicBoolean(true);
	private static int numThreads = 8;
	
	@Test
	public void testConcurrent() {
		int timeout = 5;	// seconds
		String message = "Context read/writes";
		List<Thread> threadList = new ArrayList<Thread>();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				RecordSet rs = new RecordSet();
				Record r = new Record();
				
				r.addString("myVal");
				r.addShort((short) 10);
				rs.addRecord(r);
				
				Context.getContext().store(CriticalSections.POSTFETCH, rs);
				
				for (;;) {
					rs = Context.getContext().get(CriticalSections.POSTFETCH);
					for (Record record : rs.getRecords()) {
						if (!record.getString(0).equals("myVal"))
							throw new RuntimeException("Record first string was: " + record.getString(0));
						if (record.getShort(1) != 10)
							throw new RuntimeException("Record second short was: " + record.getShort(1));
					}
				}
			}
		};
		
		for (int i = 0; i < numThreads; i++)
			threadList.add(new Thread(r));

		try {
			assertConcurrent(message, threadList, timeout);
		} catch (InterruptedException e) {
			fail("Thread was interrupted");
			e.printStackTrace();
		}
	}
	
	public void testConcurrentReadersAndWriters() {
		int timeout = 5;
		String message = "Context read/writes in different threads";
		List<Thread> threadList = new ArrayList<Thread>();
		Runnable writer = new Runnable() {
			@Override
			public void run() {
				RecordSet rs = new RecordSet();
				Record r = new Record();
				
				r.addString("myVal");
				r.addShort((short) 10);
				rs.addRecord(r);
				
				Context.getContext().store(CriticalSections.POSTFETCH, rs);
				counter.incrementAndGet();
			}
		};
		Runnable reader = new Runnable() {
			@Override
			public void run() {
				int processed = 0;
				RecordSet rs = null;
				for (;;) {
					rs = Context.getContext().get(CriticalSections.POSTFETCH);
					firstTime.set(false);
					counter.decrementAndGet();
					if (rs ==  null)
						break;
					for (Record record : rs.getRecords()) {
						processed++;
						if (record.getShort(1) != 10)
							throw new RuntimeException("Record second short was: " + record.getShort(1));
						if (!record.getString(0).equals("myVal"))
							throw new RuntimeException("Record first string was: " + record.getString(0));
					}
				}
				System.out.println(Thread.currentThread().getId() + " - Processed records: " + processed);
			}
		};
		
		for (int i = 0; i < (numThreads / 2); i++)
			threadList.add(new Thread(writer));
		for (int i = (numThreads / 2); i < numThreads; i++)
			threadList.add(new Thread(reader));
		
		try {
			assertConcurrent(message, threadList, timeout);
		} catch (InterruptedException e) {
			fail("Thread was interrupted");
			e.printStackTrace();
		}
		
		assertTrue("Counter: " + counter.get() + "; First time: " + firstTime.get(), counter.get() == 0);
	}
	
	public void assertConcurrent(final String message, final List<? extends Runnable> runnables, final int maxTimeoutSeconds) throws InterruptedException {
        final int numThreads = runnables.size();
        final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        try {
          final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
          final CountDownLatch afterInitBlocker = new CountDownLatch(1);
          final CountDownLatch allDone = new CountDownLatch(numThreads);
          for (final Runnable submittedTestRunnable : runnables) {
            threadPool.submit(new Runnable() {
              public void run() {
                allExecutorThreadsReady.countDown();
                try {
                  afterInitBlocker.await();
                  submittedTestRunnable.run();
                } catch (final Throwable e) {
                  exceptions.add(e);
                } finally {
                  allDone.countDown();
                }
              }
            });
          }
          // wait until all threads are ready
          assertTrue("Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent", allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
          // start all test runners
          afterInitBlocker.countDown();
          assertTrue(message +" timeout! More than " + maxTimeoutSeconds + " seconds", allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
        } finally {
          threadPool.shutdownNow();
        }
        assertTrue(message + "failed with exception(s)" + exceptions, exceptions.isEmpty());
      }
}
