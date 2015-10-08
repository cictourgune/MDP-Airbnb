package org.tourgune.mdp.airbnb.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.tourgune.mdp.airbnb.config.HttpRequestInfo;
import org.tourgune.mdp.airbnb.core.Record;
import org.tourgune.mdp.airbnb.plugin.AbstractPlugin;

public class AbstractParserTest {

	private class AbstractParserWrapper extends AbstractPlugin {
		public boolean isHostAccepted(String hostname) {
			return super.isHostAccepted(hostname);
		}
		public boolean isLangAccepted(String language) {
			return super.isLangAccepted(language);
		}
		public boolean isMimeAccepted(String mime) {
			return super.isMimeAccepted(mime);
		}
		public boolean isPathAccepted(String path) {
			return super.isPathAccepted(path);
		}

		@Override
		public Record[] onFetch(HttpRequestInfo hri, String html, String geographyId) {
			fail("This is not a test!");
			return null;
		}
	}
	
	public AbstractParserTest() {
	}
	
	@Test public void hostsRelative() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerHost(".airbnb.es");
		apw.registerHost(".booking.com");
		
		assertTrue("Host 'www.airbnb.es' should be accepted", apw.isHostAccepted("www.airbnb.es"));
		assertTrue("Host 'airbnb.es' should be accepted", apw.isHostAccepted("airbnb.es"));
		assertTrue("Host 'ftp.airbnb.es' should be accepted", apw.isHostAccepted("ftp.airbnb.es"));
		assertTrue("Host 'ftp.sp.airbnb.es' should be accepted", apw.isHostAccepted("ftp.sp.airbnb.es"));
		assertTrue("Host 'www.AirBnB.es' should be accepted", apw.isHostAccepted("www.AirBnB.es"));
		assertTrue("Host 'AirBnB.es' should be accepted", apw.isHostAccepted("AirBnB.es"));
		assertTrue("Host 'airbnb.ES' should be accepted", apw.isHostAccepted("airbnb.ES"));
		
		assertTrue("Host 'www.booking.com' should be accepted", apw.isHostAccepted("www.booking.com"));
		assertTrue("Host 'booking.com' should be accepted", apw.isHostAccepted("booking.com"));
		assertTrue("Host 'ftp.booking.com' should be accepted", apw.isHostAccepted("ftp.booking.com"));
		assertTrue("Host 'ftp.nl.booking.com' should be accepted", apw.isHostAccepted("ftp.nl.booking.com"));
		assertTrue("Host 'www.Booking.COM' should be accepted", apw.isHostAccepted("www.Booking.COM"));
		assertTrue("Host 'Booking.cOm' should be accepted", apw.isHostAccepted("Booking.cOm"));
		
		assertFalse("Host '3vil.com' should not be accepted", apw.isHostAccepted("3vil.com"));
		assertFalse("Host 'www.booking.3vil.com' should not be accepted", apw.isHostAccepted("www.booking.3vil.com"));
		assertFalse("Host 'www.airbnb.3vil.es' should not be accepted", apw.isHostAccepted("www.airbnb.3vil.es"));
		assertFalse("Host 'www.b00king.com' should not be accepted", apw.isHostAccepted("www.b00king.com"));
	}
	@Test public void hostsAbsolute() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerHost("airbnb.es");
		apw.registerHost("www.booking.com");
		
		assertTrue("Host 'airbnb.es' should be accepted", apw.isHostAccepted("airbnb.es"));
		assertTrue("Host 'www.booking.com' should be accepted", apw.isHostAccepted("www.booking.com"));
		assertTrue("Host 'AirBnB.es' should be accepted", apw.isHostAccepted("AirBnB.es"));
		assertTrue("Host 'www.Booking.COM' should be accepted", apw.isHostAccepted("www.Booking.COM"));
		
		assertFalse("Host 'www.airbnb.es' should not be accepted", apw.isHostAccepted("www.airbnb.es"));
		assertFalse("Host 'booking.com' should not be accepted", apw.isHostAccepted("booking.com"));
	}
	@Test public void hostsAll() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerHost("*");
		
		assertTrue(apw.isHostAccepted("3vil.com"));
		assertTrue(apw.isHostAccepted("booking.com"));
		assertTrue(apw.isHostAccepted("www.booking.com"));
		assertTrue(apw.isHostAccepted("airbnb.es"));
		assertTrue(apw.isHostAccepted("www.airbnb.es"));
	}
	@Test public void hostsNone() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		for (int i = 0; i < 2; i++) {
			assertFalse("Host '3vil.com' should not be accepted (round " + (i+1) + ")", apw.isHostAccepted("3vil.com"));
			assertFalse("Host 'booking.com' should not be accepted (round " + (i+1) + ")", apw.isHostAccepted("booking.com"));
			assertFalse("Host 'www.booking.com' should not be accepted (round " + (i+1) + ")", apw.isHostAccepted("www.booking.com"));
			assertFalse("Host 'airbnb.es' should not be accepted (round " + (i+1) + ")", apw.isHostAccepted("airbnb.es"));
			assertFalse("Host 'www.airbnb.es' should not be accepted (round " + (i+1) + ")", apw.isHostAccepted("www.airbnb.es"));
			apw.registerHost("");
		}
	}
	
	@Test public void pathsRelative() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerPath("/room/");
		apw.registerPath("/s/");
		
		assertTrue(apw.isPathAccepted("/room"));
		assertTrue(apw.isPathAccepted("/room/"));
		assertTrue(apw.isPathAccepted("/s"));
		assertTrue(apw.isPathAccepted("/s/"));
		assertTrue(apw.isPathAccepted("/room/123456/"));
		assertTrue(apw.isPathAccepted("/room/123456"));
		assertTrue(apw.isPathAccepted("/room/123456/aaa/"));
		assertTrue(apw.isPathAccepted("/room/123456/aaa"));
		assertTrue(apw.isPathAccepted("/s/San Sebastian -- Spain"));
		assertTrue(apw.isPathAccepted("/s/San Sebastian -- Spain/"));
		assertTrue(apw.isPathAccepted("/s/San Sebastian -- Spain/aaa"));
		assertTrue(apw.isPathAccepted("/s/San Sebastian -- Spain/aaa/"));
		assertTrue(apw.isPathAccepted("/s/San Sebastian%20--%20Spain?search_by_map=false"));
		
		assertTrue(apw.isPathAccepted("/RooM"));
		assertTrue(apw.isPathAccepted("/RooM/"));
		assertTrue(apw.isPathAccepted("/RooM/aAa"));
		assertTrue(apw.isPathAccepted("/RooM/aAa/"));
		
		assertFalse(apw.isPathAccepted("/mypath"));
		assertFalse(apw.isPathAccepted("/mypath/"));
		assertFalse(apw.isPathAccepted("/mypath/aaa"));
		assertFalse(apw.isPathAccepted("/mypath/aaa/"));
	}
	@Test public void pathsAbsolute() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerPath("/room");
		apw.registerPath("/s");
		
		assertTrue(apw.isPathAccepted("/room"));
		assertTrue(apw.isPathAccepted("/s"));
		assertTrue(apw.isPathAccepted("/RooM"));
		assertTrue(apw.isPathAccepted("/S"));
		
		assertTrue(apw.isPathAccepted("/s/"));
		assertTrue(apw.isPathAccepted("/room/"));
		
		assertFalse(apw.isPathAccepted("/room/123456/"));
		assertFalse(apw.isPathAccepted("/room/123456"));
		assertFalse(apw.isPathAccepted("/room/123456/aaa/"));
		assertFalse(apw.isPathAccepted("/room/123456/aaa"));
		assertFalse(apw.isPathAccepted("/s/San Sebastian -- Spain"));
		assertFalse(apw.isPathAccepted("/s/San Sebastian -- Spain/"));
		assertFalse(apw.isPathAccepted("/s/San Sebastian -- Spain/aaa"));
		assertFalse(apw.isPathAccepted("/s/San Sebastian -- Spain/aaa/"));
		assertFalse(apw.isPathAccepted("/s/San Sebastian%20--%20Spain?search_by_map=false"));
		
		assertFalse(apw.isPathAccepted("/mypath"));
		assertFalse(apw.isPathAccepted("/mypath/"));
		assertFalse(apw.isPathAccepted("/mypath/aaa"));
		assertFalse(apw.isPathAccepted("/mypath/aaa/"));
	}
	@Test public void pathsAll() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerPath("*");
		
		assertTrue(apw.isPathAccepted("/room"));
		assertTrue(apw.isPathAccepted("/s/"));
		assertTrue(apw.isPathAccepted("/mypath"));
		assertTrue(apw.isPathAccepted("/mypath/aaa"));
	}
	@Test public void pathsNone() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		for (int i = 0; i < 2; i++) {
			assertFalse(apw.isPathAccepted("/room"));
			assertFalse(apw.isPathAccepted("/room/"));
			assertFalse(apw.isPathAccepted("/s"));
			assertFalse(apw.isPathAccepted("/s/"));
			assertFalse(apw.isPathAccepted("/s/aaa"));
			assertFalse(apw.isPathAccepted("/s/aaa/"));
			apw.registerPath("");
		}
	}
	
	@Test public void langsRelative() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerLang("es-");
		
		assertTrue(apw.isLangAccepted("es"));
		assertTrue(apw.isLangAccepted("ES"));
		assertTrue(apw.isLangAccepted("es-ES"));
		assertTrue(apw.isLangAccepted("es-es"));
		assertTrue(apw.isLangAccepted("es-AR"));
		assertTrue(apw.isLangAccepted("es-ar"));
		
		assertFalse(apw.isLangAccepted("en"));
		assertFalse(apw.isLangAccepted("EN"));
		assertFalse(apw.isLangAccepted("en-EN"));
		assertFalse(apw.isLangAccepted("en-en"));
	}
	@Test public void langsAbsolute1() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerLang("es");
		
		assertTrue(apw.isLangAccepted("es"));
		assertTrue(apw.isLangAccepted("ES"));
		
		assertFalse(apw.isLangAccepted("es-ES"));
		assertFalse(apw.isLangAccepted("es-AR"));
	}
	@Test public void langsAbsolute2() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerLang("es-es");
		
		assertTrue(apw.isLangAccepted("es-es"));
		assertTrue(apw.isLangAccepted("es-ES"));
		
		assertFalse(apw.isLangAccepted("es"));
		assertFalse(apw.isLangAccepted("ES"));
	}
	@Test public void langsAbsolute1Upper() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerLang("ES");
		
		assertTrue(apw.isLangAccepted("es"));
		assertTrue(apw.isLangAccepted("ES"));
		
		assertFalse(apw.isLangAccepted("es-ES"));
		assertFalse(apw.isLangAccepted("es-AR"));
	}
	@Test public void langsAbsolute2Upper() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerLang("es-ES");
		
		assertTrue(apw.isLangAccepted("es-es"));
		assertTrue(apw.isLangAccepted("es-ES"));
		
		assertFalse(apw.isLangAccepted("es"));
		assertFalse(apw.isLangAccepted("ES"));
	}
	
	@Test public void mimesAbsolute() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerMime("application/json");
		apw.registerMime("text/HTML");
		
		assertTrue(apw.isMimeAccepted("Application/JSON"));
		assertTrue(apw.isMimeAccepted("text/html"));
		assertFalse(apw.isMimeAccepted("application/octet-stream"));
	}
	
	@Test public void mimesAll() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		apw.registerMime("*");
		
		assertTrue(apw.isMimeAccepted("application/octet-stream"));
		assertTrue(apw.isMimeAccepted("text/html"));
		assertTrue(apw.isMimeAccepted("APPLICATION/JSON"));
	}
	
	@Test public void mimesNone() {
		AbstractParserWrapper apw = new AbstractParserWrapper();
		
		for (int i = 0; i < 2; i++) {
			assertFalse(apw.isMimeAccepted("application/octet-stream"));
			assertFalse(apw.isMimeAccepted("text/html"));
			assertFalse(apw.isMimeAccepted("APPLICATION/JSON"));
			apw.registerMime("");
		}
	}
}
