package org.tourgune.mdp.airbnb.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.tourgune.mdp.airbnb.config.HttpRequestInfo;

public class HttpRequestInfoTest {
	public HttpRequestInfoTest() {
	}

	@Test
	public void getWholePath_appendQueryStrStr() {
		HttpRequestInfo hri = new HttpRequestInfo("http", "www.burgersoftware.es", 80, "/index.php");
		
		assertTrue("getWholePath() should return '/index.php'", hri.getWholePath().equals("/index.php"));
		
		hri.appendQuery("k", "v");
		hri.appendQuery("v", "k");
		// HashMap no garantiza que el orden se mantendrá, y la verdad es que no nos importa
		// por eso, hay que dar por válidas todas las secuencias posibles
		assertTrue("getWholePath() should return '/index.php?k=v&v=k' or '/index.php?v=k&k=v'",
				hri.getWholePath().equals("/index.php?k=v&v=k") || hri.getWholePath().equals("/index.php?v=k&k=v"));
		
		hri.appendQuery("k", "alt");
		assertTrue("getWholePath() should return '/index.php?k=alt&v=k' or '/index.php?v=k&k=alt'",
				hri.getWholePath().equals("/index.php?k=alt&v=k") || hri.getWholePath().equals("/index.php?v=k&k=alt"));
	}

	@Test
	public void getWholePath_appendQuery() {
		HttpRequestInfo hri = new HttpRequestInfo("http", "www.burgersoftware.es", 80, "/index.php");
		
		assertTrue("getWholePath() should return '/index.php'", hri.getWholePath().equals("/index.php"));
		
		hri.appendQuery("k=v");
		hri.appendQuery("v=k");
		assertTrue("getWholePath() should return '/index.php?k=v&v=k' or '/index.php?v=k&k=v'",
				hri.getWholePath().equals("/index.php?k=v&v=k") || hri.getWholePath().equals("/index.php?v=k&k=v"));
		
		hri.appendQuery("k=alt");
		assertTrue("getWholePath() should return '/index.php?k=alt&v=k' or '/index.php?v=k&k=alt'",
				hri.getWholePath().equals("/index.php?k=alt&v=k") || hri.getWholePath().equals("/index.php?v=k&k=alt"));
	}

	@Test
	public void getWholePath_appendQueryStrStr_appendQuery() {
		HttpRequestInfo hri = new HttpRequestInfo("http", "www.burgersoftware.es", 80, "/index.php");
		
		assertTrue("getWholePath() should return '/index.php'", hri.getWholePath().equals("/index.php"));
		
		hri.appendQuery("k", "v");
		hri.appendQuery("v=k");
		assertTrue("getWholePath() should return '/index.php?k=v&v=k' or '/index.php?v=k&k=v'",
				hri.getWholePath().equals("/index.php?k=v&v=k") || hri.getWholePath().equals("/index.php?v=k&k=v"));
		
		hri.appendQuery("k=alt");
		assertTrue("getWholePath() should return '/index.php?k=alt&v=k' or '/index.php?v=k&k=alt'",
				hri.getWholePath().equals("/index.php?k=alt&v=k") || hri.getWholePath().equals("/index.php?v=k&k=alt"));
	}
	
	@Test
	public void getWholePath_appendQueryStrStr_appendQuery_invalid() {
		HttpRequestInfo hri = new HttpRequestInfo("http", "www.burgersoftware.es", 80, "/index.php");
		
		hri.appendQuery("kv");
		assertTrue("getWholePath() should return '/index.php'", hri.getWholePath().equals("/index.php"));
		
		hri.appendQuery("k", "");
		assertTrue("getWholePath() should return '/index.php', but returned '" + hri.getWholePath() + "'",
				hri.getWholePath().equals("/index.php"));
		
		hri.appendQuery("", "v");
		assertTrue("getWholePath() should return '/index.php'", hri.getWholePath().equals("/index.php"));
		
		try {
			hri.appendQuery(null, "v");
			fail("A 'NullPointerException' should have been thrown");
		} catch (NullPointerException e) {}
		try {
			hri.appendQuery("k", null);
			fail("A 'NullPointerException' should have been thrown");
		} catch (NullPointerException e) {}
		try {
			hri.appendQuery(null, null);
			fail("A 'NullPointerException' should have been thrown");
		} catch (NullPointerException e) {}
	}
	
	@Test
	public void getWholePath_setQuery() {
		HttpRequestInfo hri = new HttpRequestInfo("http", "www.burgersoftware.es", 80, "/index.php");
		
		hri.setQuery("");
		assertTrue("getWholePath() should return '/index.php' or '/index.php'", hri.getWholePath().equals("/index.php"));
		
		hri.setQuery("k=v&v=k");
		assertTrue("getWholePath() should return '/index.php?k=v&v=k' or '/index.php?v=k&k=v'",
				hri.getWholePath().equals("/index.php?k=v&v=k") || hri.getWholePath().equals("/index.php?v=k&k=v"));
		
		hri.setQuery("k=alt&v=k");
		assertTrue("getWholePath() should return '/index.php?k=alt&v=k' or '/index.php?v=k&k=alt'",
				hri.getWholePath().equals("/index.php?k=alt&v=k") || hri.getWholePath().equals("/index.php?v=k&k=alt"));
		
		hri.setQuery("");
		assertTrue("getWholePath() should return '/index.php?k=alt&v=k' or '/index.php?v=k&k=alt'",
				hri.getWholePath().equals("/index.php?k=alt&v=k") || hri.getWholePath().equals("/index.php?v=k&k=alt"));
		
		try {
			hri.setQuery(null);
			fail("A 'NullPointerException' should have been thrown");
		} catch (NullPointerException e) {}
	}

	@Test
	public void testToString() {
		HttpRequestInfo hri = new HttpRequestInfo("http", "www.burgersoftware.es", 80, "/index.php");
		
		assertTrue("toString() should return 'http://www.burgersoftware.es/index.php'",
				hri.toString().equals("http://www.burgersoftware.es/index.php"));
		
		hri.appendQuery("k", "v");
		hri.appendQuery("v=k");
		assertTrue("toString() should return 'http://www.burgersoftware.es/index.php?k=v&v=k' or 'http://www.burgersoftware.es/index.php?v=k&k=v'",
				hri.toString().equals("http://www.burgersoftware.es/index.php?k=v&v=k") ||
				hri.toString().equals("http://www.burgersoftware.es/index.php?v=k&k=v"));
	}
	
	@Test
	public void testToString_nonStandardPort() {
		HttpRequestInfo hri = new HttpRequestInfo("http", "www.burgersoftware.es", 443, "/index.php");
		
		assertTrue("toString() should return 'http://www.burgersoftware.es:443/index.php'",
				hri.toString().equals("http://www.burgersoftware.es:443/index.php"));
		
		hri.appendQuery("k", "v");
		hri.appendQuery("v=k");
		assertTrue("toString() should return 'http://www.burgersoftware.es:443/index.php?k=v&v=k' or 'http://www.burgersoftware.es:443/index.php?v=k&k=v'",
				hri.toString().equals("http://www.burgersoftware.es:443/index.php?k=v&v=k") ||
				hri.toString().equals("http://www.burgersoftware.es:443/index.php?v=k&k=v"));
	}

}
