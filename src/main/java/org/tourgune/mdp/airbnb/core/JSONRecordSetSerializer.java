package org.tourgune.mdp.airbnb.core;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tourgune.mdp.airbnb.utils.Args;

public class JSONRecordSetSerializer implements RecordSetSerializer {

	@Override
	public String serialize(RecordSet recordSet) {
		return serialize(recordSet, (Charset) null);
	}

	@Override
	public String serialize(RecordSet recordSet, Charset charset) {
		Args.checkNotNull("RecordSet cannot be null", recordSet);
		
		JSONObject rs = new JSONObject();
		JSONArray records = new JSONArray();
		
		for (Iterator<String> keyIt = recordSet.getInfoKeySet().iterator(); keyIt.hasNext();) {
			String curKey = keyIt.next();
			rs.put(curKey, recordSet.getInfo(curKey));
		}
		
		for (Record record : recordSet.getRecords())
			records.put(new JSONObject(record.toString(new JSONRecordSerializer())));
		rs.put("records", records);
		
		return charset == null ? rs.toString() : new String(rs.toString().getBytes(), charset);
	}

	@Override
	public void serialize(RecordSet recordSet, OutputStream os) {
		// TODO Auto-generated method stub

	}

	@Override
	public void serialize(RecordSet recordSet, OutputStream os, Charset charset) {
		// TODO Auto-generated method stub

	}

}
