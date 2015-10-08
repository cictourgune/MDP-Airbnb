package org.tourgune.mdp.airbnb.core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tourgune.mdp.airbnb.utils.Args;

public class JSONRecordSerializer implements RecordSerializer {

	@Override
	public String serialize(Record record) {
		return serialize(record, (Charset) null);
	}

	/**
	 * Characters are coded as strings.
	 * Shorts and bytes are coded as integers (w/o quotes).
	 */
	@Override
	public String serialize(Record record, Charset charset) {
		Args.checkNotNull("record cannot be null", record);
		
		int fieldIndex = 0;
		JSONObject json = new JSONObject();
		JSONArray jsonData = new JSONArray();
		RecordTypes[] layout = record.getLayout();
		
		for (RecordTypes type : layout) {
			switch (type) {
			case BYTE:
				jsonData.put((int) record.getByte(fieldIndex));
				break;
			case CHAR:
				jsonData.put(new Character(record.getChar(fieldIndex)));
				break;
			case INTEGER:
				jsonData.put(record.getInt(fieldIndex));
				break;
			case SHORT:
				jsonData.put((int) record.getShort(fieldIndex));
				break;
			case STRING:
				jsonData.put(record.getString(fieldIndex));
				break;
			}
			fieldIndex++;
		}
		
		json.put("data", jsonData);
		
		return charset == null ? json.toString() : new String(json.toString().getBytes(), charset);
	}

	@Override
	public void serialize(Record record, OutputStream os) throws IOException {
		serialize(record, os, null);
	}

	@Override
	public void serialize(Record record, OutputStream os, Charset charset) throws IOException {
		Args.checkNotNull("OutputStream cannot be null", os);
		
		byte[] content = serialize(record, charset).getBytes();
		os.write(content);
	}

}
