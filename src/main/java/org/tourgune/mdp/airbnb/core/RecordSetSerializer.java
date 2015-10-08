package org.tourgune.mdp.airbnb.core;

import java.io.OutputStream;
import java.nio.charset.Charset;

public interface RecordSetSerializer {

	String serialize(RecordSet recordSet);
	String serialize(RecordSet recordSet, Charset charset);
	
	void serialize(RecordSet recordSet, OutputStream os);
	void serialize(RecordSet recordSet, OutputStream os, Charset charset);
}
