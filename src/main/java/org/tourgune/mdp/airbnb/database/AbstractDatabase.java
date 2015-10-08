package org.tourgune.mdp.airbnb.database;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.tourgune.mdp.airbnb.core.Record;
import org.tourgune.mdp.airbnb.core.RecordSet;

public abstract class AbstractDatabase {

	private List<Integer> acceptedParserIds;
	
	public AbstractDatabase() {
		acceptedParserIds = new LinkedList<Integer>();
	}
	
	public void registerId(int id) {
		acceptedParserIds.add(id);
	}
	
	/*
	 * Key mappings:
	 * 	TITLE --> 1
	 * 	ROOM_TYPE --> 2
	 * 	ROOM_PRICE --> 3
	 */
	public void insertRecords(RecordSet recordSet) {
		int curParserId = 0;
		for (Iterator<Integer> pidIt = acceptedParserIds.iterator(); pidIt.hasNext();) {
			curParserId = pidIt.next().intValue();
			for (Record record : recordSet.getRecords())
				insertRecord(record);
		}
	}

	protected abstract void insertRecord(Record record);
}
