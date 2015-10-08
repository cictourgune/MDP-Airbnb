package org.tourgune.mdp.airbnb.core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public interface RecordSerializer {

	String serialize(Record record);
	String serialize(Record record, Charset charset);
	
	void serialize(Record record, OutputStream os) throws IOException;
	void serialize(Record record, OutputStream os, Charset charset) throws IOException;
}
