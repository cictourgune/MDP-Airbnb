package org.tourgune.mdp.airbnb.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.tourgune.mdp.airbnb.config.ConfigFileParser;
import org.tourgune.mdp.airbnb.exception.ConfigSyntaxException;

public class ConfigFileParserTest {
	@Test
	public void testNextSource() {
		List<String> lines = newList(
				new String[]{
						"[MySection]",
						"my_attr = my_val",
						"my_attr2=my_val2"
				}
				);
		ConfigFileParser cfp = new ConfigFileParser(lines);
		String[] actual = null;
		String[][] expected = {
				{"my_attr", "my_val"},
				{"my_attr2", "my_val2"}
		};
		try {
			assertTrue(cfp.nextSource().equals("MySection"));
			assertTrue(cfp.nextSource() == null);
			
			actual = cfp.nextEntry();
			assertTrue(actual[0].equals(expected[0][0]) && actual[1].equals(expected[0][1]));
			
			actual = cfp.nextEntry();
			assertTrue(actual[0].equals(expected[1][0]) && actual[1].equals(expected[1][1]));
		} catch (ConfigSyntaxException e) {
			fail("A 'ConfigSyntaxException' was thrown");
		}
	}

	@Test
	public void testSpaces_2() {
		List<String> lines = newList(
				new String[]{
						"[My Section]",
						"my attr = my val",
						"my attr 2=my val 2"
				}
				);
		ConfigFileParser cfp = new ConfigFileParser(lines);
		String[] actual = null;
		String[][] expected = {
				{"my attr", "my val"},
				{"my attr 2", "my val 2"}
		};
		try {
			assertTrue(cfp.nextSource().equals("My Section"));
			
			actual = cfp.nextEntry();
			assertTrue(actual[0].equals(expected[0][0]) && actual[1].equals(expected[0][1]));
			
			actual = cfp.nextEntry();
			assertTrue(actual[0].equals(expected[1][0]) && actual[1].equals(expected[1][1]));
		} catch (ConfigSyntaxException e) {
			fail("A 'ConfigSyntaxException' was thrown");
		}
	}
	
	@Test
	public void testSpaces_3() {
		List<String> lines = newList(
				new String[]{
						" [ My Section ]  ",
						" my attr  =     my val",
						" my attr 2         =  my val 2"
				}
				);
		ConfigFileParser cfp = new ConfigFileParser(lines);
		String[] actual = null;
		String[][] expected = {
				{"my attr", "my val"},
				{"my attr 2", "my val 2"}
		};
		try {
			assertTrue(cfp.nextSource().equals("My Section"));
			
			actual = cfp.nextEntry();
			assertTrue(actual[0].equals(expected[0][0]) && actual[1].equals(expected[0][1]));
			
			actual = cfp.nextEntry();
			assertTrue(actual[0].equals(expected[1][0]) && actual[1].equals(expected[1][1]));
		} catch (ConfigSyntaxException e) {
			fail("A 'ConfigSyntaxException' was thrown");
		}
	}
	
	@Test
	public void testEmptyLines() {
		List<String> lines = newList(
				new String[]{
						"",
						" [ My Section ]",
						" my attr = my val",
						"",
						"",
						""
				}
				);
		ConfigFileParser cfp = new ConfigFileParser(lines);
		String[] actual = null, expected = {"my attr", "my val"};
		try {
			assertTrue(cfp.nextSource().equals("My Section"));
			
			actual = cfp.nextEntry();
			assertTrue(actual[0].equals(expected[0]) && actual[1].equals(expected[1]));
			
			assertTrue(cfp.nextEntry() == null);
			assertTrue(cfp.nextSource() == null);
			assertTrue(cfp.nextEntry() == null);
			assertTrue(cfp.nextEntry() == null);
			assertTrue(cfp.nextEntry() == null);
			assertTrue(cfp.nextEntry() == null);
			assertTrue(cfp.nextSource() == null);
		} catch (ConfigSyntaxException e) {
			fail("A 'ConfigSyntaxException' was thrown");
		}
	}
	
	@Test
	public void testComments() {
		List<String> lines = newList(
				new String[]{
						"# this is a comment",
						" [ My Section ]",
						"#tiac",
						"# tiac",
						" my attr = my val",
						"#this is a comment",
						"",
						""
				}
				);
		ConfigFileParser cfp = new ConfigFileParser(lines);
		String[] actual = null, expected = {"my attr", "my val"};
		try {
			assertTrue(cfp.nextSource().equals("My Section"));
			
			actual = cfp.nextEntry();
			assertTrue(actual[0].equals(expected[0]) && actual[1].equals(expected[1]));
			
			assertTrue(cfp.nextEntry() == null);
			assertTrue(cfp.nextSource() == null);
			assertTrue(cfp.nextEntry() == null);
			assertTrue(cfp.nextEntry() == null);
			assertTrue(cfp.nextEntry() == null);
			assertTrue(cfp.nextEntry() == null);
			assertTrue(cfp.nextSource() == null);
		} catch (ConfigSyntaxException e) {
			fail("A 'ConfigSyntaxException' was thrown");
		}
	}
	
	@Test
	public void testNullSourceEntry() {
		List<String> lines = newList(
				new String[]{
						" [My Section]",
						"my attr = my val",
						""
				}
				);
		ConfigFileParser cfp = new ConfigFileParser(lines);
		String[] expected = {"my attr", "my val"}, actual = null;
		try {
			assertTrue(cfp.nextEntry() == null);
			assertTrue(cfp.nextSource().equals("My Section"));
			assertTrue(cfp.nextSource() == null);
			
			actual = cfp.nextEntry();
			assertTrue(actual[0].equals(expected[0]) && (actual[1].equals(expected[1])));
			
			assertTrue(cfp.nextSource() == null);
			assertTrue(cfp.nextEntry() == null);
		} catch (ConfigSyntaxException e) {
			fail("A 'ConfigSyntaxException' was thrown");
		}
	}
	
	@Test
	public void testInvalidEntry() {
		List<String> lines = newList(
				new String[]{
						" [My Section]",
						"my attr = my val",
						"a"
				}
				);
		ConfigFileParser cfp = new ConfigFileParser(lines);
		String[] actual = null, expected = {"my attr", "my val"};
		try {
			assertTrue(cfp.nextSource().equals("My Section"));
			
			actual = cfp.nextEntry();
			assertTrue(actual[0].equals(expected[0]) && actual[1].equals(expected[1]));
		} catch (ConfigSyntaxException e) {
			fail("A 'ConfigSyntaxException' was thrown");
		}
		try {
			cfp.nextEntry();
			fail("A 'ConfigSyntaxException' should have been thrown");
		} catch (ConfigSyntaxException e) {}
	}

	private List<String> newList(String[] strings) {
		List<String> list = new ArrayList<String>();
		for (String string : strings)
			list.add(string);
		return list;
	}
}
