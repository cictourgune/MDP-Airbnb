package org.tourgune.mdp.airbnb.config;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

import org.tourgune.mdp.airbnb.exception.ConfigException;
import org.tourgune.mdp.airbnb.exception.DirectiveSyntaxException;
import org.tourgune.mdp.airbnb.exception.DuplicatedConfigDirectiveException;

public class ConfigTest {
	
	public Config loadConfig() throws IOException, ConfigException {
		String f = "test-airbnb.conf";
		return Config.load(new File(f));
	}
	
	@After
	public void clearConfig() {
		Config.getInstance().clear();
	}
	
	@Test
	public void testDirective() throws IOException, ConfigException {
		Config c = loadConfig();
		
		String bar = c.getParam("test_foo");
		assertTrue("Incorrect getParam(String) for [Test]", bar.equals("bar"));
		
		bar = c.getParam("test", "foo");
		assertTrue("Incorrect getParam(String, String) for [Test]", bar.equals("bar"));
		
		bar = c.getParam("core_foo");
		assertTrue("Incorrect getParam(String) for [Core]", bar.equals("bar"));
		
		bar = c.getParam("core", "foo");
		assertTrue("Incorrect getParam(String, String) for [Core]", bar.equals("bar"));
	}
	
	@Test
	public void testDirectiveReplace() throws IOException, ConfigException {
		Config c = loadConfig();
		
		try {
			c.addDirective("test_foo", "new bar");
		} catch (DirectiveSyntaxException e) {
			fail("A DirectiveSyntaxException was thrown");
		} catch (DuplicatedConfigDirectiveException e) {
			// this is a success
		} catch (ConfigException e) {
			fail("A DuplicatedConfigDirectiveException should be thrown, but another ConfigException was thrown instead");
		}
		
		try {
			c.addDirective("test_foo", "new bar", true);
			String bar = c.getParam("test", "foo");
			assertTrue("Param 'test_foo' should be 'new bar'", bar.equals("new bar"));
		} catch (ConfigException e) {
			fail("foo should have been replaced with the new value");
		}
	}
	
	@Test
	public void testDirectiveReplaceWithCommandLine() throws IOException, ConfigException {
		Config c = loadConfig();
		
		String foo = c.getParam("core", "foo");
		assertTrue(foo.equals("bar"));
		
		c.addDirective("--core-foo", "new bar", true);
		foo = c.getParam("core", "foo");
		assertTrue("--core-foo should be 'new bar'", foo.equals("new bar"));
	}
	
	@Test
	public void testDirectiveReplaceWithCommandLineImplicit() throws IOException, ConfigException {
		Config c = loadConfig();
		
		String foo = c.getParam("core", "foo");
		assertTrue(foo.equals("bar"));
		
		try {
		c.addDirective("--core-foo", "new bar");
		foo = c.getParam("core", "foo");
		assertTrue("--core-foo should be 'new bar'", foo.equals("new bar"));
		} catch (DuplicatedConfigDirectiveException e) {
			fail("--core-foo should have been replaced (a DuplicatedConfigDirectiveException has been thrown)");
		} catch (ConfigException e) {
			fail("An ambiguous ConfigException has been thrown");
		}
	}
	
	@Test
	public void testDirectiveReplaceWhenNotExist() throws IOException, ConfigException {
		Config c = loadConfig();
		
		String myParam = c.getParam("test_myparam");
		assertTrue("'test_myparam' should not exist in [Test]", myParam == null);
		
		c.addDirective("test_myparam", "myvalue", true);
		myParam = c.getParam("test", "myparam");
		assertTrue("[Test] myparam should be 'myvalue'", myParam.equals("myvalue"));
		
		c.addDirective("test", "myparam", "my new value", true);
		myParam = c.getParam("test", "myparam");
		assertTrue("[Test] myparam should be 'my new value'", myParam.equals("my new value"));
	}
	
	@Test
	public void testDirectiveMultiple() throws IOException, ConfigException {
		Config c = loadConfig();
		
		String testSection = null;
		String coreSection = null;
		
		for (int i = 0; i < 2; i++) {
			/*
			 * Hacemos la prueba dos veces: primero en minúscula y luego en mayúscula.
			 */
			switch (i) {
			case 0:
				testSection = "test";
				coreSection = "core";
				break;
			case 1:
				testSection = "Test";
				coreSection = "Core";
				break;
			}
			
			Map<String, String> testParams = c.getParams(testSection);
			Map<String, String> coreParams = c.getParams(coreSection);
			
			if (testParams.containsKey("foo")) {
				if (!testParams.get("foo").equals("bar"))
					fail("test_foo should equal 'bar'");
			} else
				fail("Key test_foo does not exist");
			
			if (coreParams.containsKey("foo")) {
				if (!coreParams.get("foo").equals("bar"))
					fail("core_foo should equal 'bar'");
			} else
				fail("Key core_foo does not exist");
			
			if (coreParams.containsKey("bar")) {
				if (!coreParams.get("bar").equals("foobar"))
					fail("core_bar should equal 'foobar'");
			}  else
				fail("Key core_bar does not exist");
			
			assertFalse("Key 'bar' is not in section [Test]", testParams.containsKey("bar"));
		}
	}

	@Test
	public void testDirectiveMultipleValues() throws IOException, ConfigException {
		Config c = loadConfig();
		
		List<String> values = c.getParamValues("test_test", null);
		assertTrue("values should have 3 elements", values.size() == 3);
		assertTrue("first element doesn't match", values.get(0).equals("1"));
		assertTrue("second element doesn't match", values.get(1).equals("2"));
		assertTrue("third element doesn't match", values.get(2).equals("3"));
	}
	
	@Test
	public void testDirectiveMultipleValuesEmpty() throws IOException, ConfigException {
		Config c = loadConfig();
		
		List<String> values = c.getParamValues("foo_bar", null);
		assertTrue("values should be empty", values.isEmpty() && values.size() == 0);
	}
	
	@Test
	public void testDirectiveMultipleValuesDefault() throws IOException, ConfigException {
		Config c = loadConfig();
		
		String[] defaultValues = {"a"};
		List<String> values = c.getParamValues("foo_bar", defaultValues);
		assertTrue("values should have 1 element", values.size() == 1);
		assertTrue("first element doesn't match", values.get(0).equals("a"));
	}
}
