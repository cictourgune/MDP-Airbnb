package org.tourgune.mdp.airbnb.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecordSet implements Iterable<Record> {

	private List<Record> records;

	private Map<String, String> info;
	
	public RecordSet() {
		records = new ArrayList<Record>();
		info = new HashMap<String, String>();
	}
	
	public RecordSet(Map<String, String> info) {
		this.info = info;
		records = new ArrayList<Record>();
	}
	
	public RecordSet(List<Record> records) {
		this.records = records;
		info = new HashMap<String, String>();
	}
	
	public RecordSet putInfoKey(String k, String v) {
		info.put(k, v);
		return this;
	}
	
	public String getInfo(String key) {
		return info.get(key);
	}
	
	public Set<String> getInfoKeySet() {
		return info.keySet();
	}
	
	public Record[] getRecords() {
		return this.records.toArray(new Record[0]);
	}
	
	public RecordSet addRecord(Record r) {
		records.add(r);
		return this;
	}
	
	/**
	 * Removes a record from this <code>RecordSet</code>.
	 * In order to determine which <code>Record</code> has to be removed, the <code>equals()</code> method
	 * will be used to compare the supplied <code>Record</code> with each element in this <code>RecordSet</code>.
	 * 
	 * @param r The <code>Record</code> to be removed.
	 * @return The removed record or <code>null</code> if no such record existed.
	 */
	public Record removeRecord(Record r) {
		return records.remove(r) ? r : null;
	}
	
	/**
	 * Removes a <code>Record</code> from this <code>RecordSet</code>.
	 * 
	 * @param pos The index of the <code>Record</code> to be removed.
	 * @return The removed <code>Record</code>.
	 * @throws IndexOutOfBoundsException If the index is out of range <code>(index < 0 || index >= size())</code>.
	 */
	public Record removeRecord(int pos) {
		return records.remove(pos);
	}
	
	public Record getRecord(int pos) {
		return records.get(pos);
	}
	
	public int size() {
		return records.size();
	}
	
	@Override
	public String toString() {
		return new JSONRecordSetSerializer().serialize(this);
	}
	
	public String toString(RecordSetSerializer serializer) {
		return serializer.serialize(this);
	}
	
	@Override
	public Iterator<Record> iterator() {
		return records.iterator();
	}

	/**
	 * Removes all the <code>Record</code>s in this <code>RecordSet</code>.
	 */
	public void clean() {
		records.clear();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RecordSet) {
			RecordSet rs = (RecordSet) obj;
			if (size() == rs.size()) {
				for (int recordIndex = 0; recordIndex < size(); recordIndex++)
					if (!getRecord(recordIndex).equals(rs.getRecord(recordIndex)))
						return false;
				return true;
			}
			return false;
		}
		
		return false;
	}
}
