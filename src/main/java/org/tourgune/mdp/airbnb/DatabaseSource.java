package org.tourgune.mdp.airbnb;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.tourgune.mdp.airbnb.core.Context;
import org.tourgune.mdp.airbnb.core.Context.CriticalSections;
import org.tourgune.mdp.airbnb.core.RecordSet;
import org.tourgune.mdp.airbnb.database.AbstractDatabase;

public class DatabaseSource {

	private Context ctx;
	private List<AbstractDatabase> dbs;
	
	public DatabaseSource() {
		ctx = Context.getContext();
		dbs = new LinkedList<AbstractDatabase>();
	}
	
	public void attachDatabase(AbstractDatabase db) {
		dbs.add(db);
	}
	
	public void work() {
		RecordSet rs = null;
		
		rs = ctx.get(CriticalSections.POSTPARSE);
		if (rs != null) {
			System.out.println("[" + Thread.currentThread().getName() + "] Processing \"" + rs.toString().substring(0, 20) + "\"...");
			for (Iterator<AbstractDatabase> it = dbs.iterator(); it.hasNext();)
				it.next().insertRecords(rs);
		}
	}
}
