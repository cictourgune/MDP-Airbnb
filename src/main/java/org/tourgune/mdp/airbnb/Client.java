package org.tourgune.mdp.airbnb;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Este cliente está pensado para ser lineal y monohilo.
 * Las URLs que se van añadiendo se van visitando una tras otra.
 * Esta interfaz no es thread-safe.
 * 
 * @todo Modificar interfaz para hacer que sea concurrente, y thread-safe.
 * @author Ander.Juaristi
 *
 */
public interface Client extends Closeable {

	public void addUrl(URL url) throws InvalidUrlException;
	public void addUrl(String url) throws InvalidUrlException;
	public void addAllUrls(List<URL> urls) throws InvalidUrlException;
	
	public void start() throws IOException;
	
}
