package org.tourgune.mdp.airbnb.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.tourgune.mdp.airbnb.core.Context;
import org.tourgune.mdp.airbnb.core.Context.CriticalSections;
import org.tourgune.mdp.airbnb.core.Record;
import org.tourgune.mdp.airbnb.core.RecordSet;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;

public class ContextExpectedUsageTest {

	public static AtomicInteger counter = new AtomicInteger(0);
	@Rule public ConcurrentRule rule = new ConcurrentRule();
	
	@Test
	@Before
	public void initialization() {
		Context ctx = Context.getContext();
		RecordSet rs1 = new RecordSet(), rs2 = new RecordSet();
		Record r1 = new Record(), r2 = new Record();
		
		r1.addString("First pending record");
		r2.addString("Second pending record");
		
		rs1.addRecord(r1);
		rs2.addRecord(r2);
		counter.addAndGet(2);
		
		ctx.store(CriticalSections.PREFETCH, rs1);
		ctx.store(CriticalSections.PREFETCH, rs2);
	}
	
	@Test
	@Concurrent(count = 5)
	public void process() {
		while(counter.get() > 0)
			processPendingQueue();
	}
	
	public void processPendingQueue() {
		Context ctx = Context.getContext();
		RecordSet rs = null;
		
		rs = ctx.get(CriticalSections.PREFETCH);
		counter.decrementAndGet();
		if (rs != null) {
			for (Record record : rs.getRecords()) {
				System.out.println(Thread.currentThread().getName() + " - Processed record: " + record.getString(0));
			}
		}
	}
}
