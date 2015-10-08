package org.tourgune.mdp.airbnb.tests;

import junit.framework.TestCase;

import org.tourgune.mdp.airbnb.core.JSONRecordSerializer;
import org.tourgune.mdp.airbnb.core.JSONRecordSetSerializer;
import org.tourgune.mdp.airbnb.core.Record;
import org.tourgune.mdp.airbnb.core.RecordSet;
import org.tourgune.mdp.airbnb.core.RecordTypes;

public class RecordExpectedUsageTest extends TestCase {

	private static final String testString = "randomString";
	private static final int testInt = 123456;
	private static final short testShort = 32767;
	private static final char testChar = 'a';
	private static final byte testByte = 127;

	public void testGetString() {
		Record r = new Record();
		r.addString(testString);
		String str = r.getString(0);
		assertEquals(str, testString);
	}

	public void testGetInt() {
		Record r = new Record();
		r.addInt(testInt);
		int i = r.getInt(0);
		assertEquals(i, testInt);
	}

	public void testGetShort() {
		Record r = new Record();
		r.addShort(testShort);
		short s = r.getShort(0);
		assertEquals(s, testShort);
	}

	public void testGetChar() {
		Record r = new Record();
		r.addChar(testChar);
		char c = r.getChar(0);
		assertEquals(c, testChar);
	}

	public void testGetByte() {
		Record r = new Record();
		r.addByte(testByte);
		byte b = r.getByte(0);
		assertEquals(b, testByte);
	}

	public void testGetLayout() {
		RecordTypes[] expectedLayout = {
				RecordTypes.CHAR,
				RecordTypes.STRING,
				RecordTypes.INTEGER,
				RecordTypes.STRING,
				RecordTypes.SHORT},
				layout = null;
		Record r = new Record();
		
		r.addChar(testChar);
		r.addString(testString);
		r.addInt(testInt);
		r.addString(testString + " 2");
		r.addShort(testShort);
		
		layout = r.getLayout();
		if (layout.length != expectedLayout.length)
			fail("Layout test (Before read) - Lengths of expected layout and returned layout do not match");
		for (int i = 0; i < layout.length; i++)
			if (layout[i] != expectedLayout[i])
				fail("Layout test (Before read) - Index " + i + " of expected layout and returned layout do not match");
		
		r.getChar(0);
		r.getString(1);
		r.getInt(2);
		r.getString(3);
		r.getShort(4);
		
		layout = r.getLayout();
		if (layout.length != expectedLayout.length)
			fail("Layout test (After read) - Lengths of expected layout and returned layout do not match");
		for (int i = 0; i < layout.length; i++)
			if (layout[i] != expectedLayout[i])
				fail("Layout test (After read) - Index " + i + " of expected layout and returned layout do not match");
	}

	public void testRecordsEqual() {
		Record records[] = {new Record(), new Record()};
		
		for (Record record : records) {
			record.addByte(testByte);
			record.addString(testString);
			record.addInt(testInt);
			record.addShort(testShort);
			record.addChar(testChar);
		}
		
		assertTrue(records[0].equals(records[1]));
	}
	
	public void testRecordsNotEqual() {
		Record records[] = {new Record(), new Record()};
		
		records[0].addByte(testByte);
		records[0].addString(testString);
		records[0].addInt(testInt);
		records[0].addShort(testShort);
		records[0].addChar(testChar);
		
		records[1].addChar(testChar);
		records[1].addString(testString);
		records[1].addShort(testShort);
		records[1].addInt(testInt);
		records[1].addByte(testByte);
		
		assertFalse(records[0].equals(records[1]));
	}
	
	public void testRecordsNotEqualDiffSourceId() {
		Record records[] = {new Record(), new Record()};
		
		for (Record record : records) {
			record.addByte(testByte);
			record.addString(testString);
			record.addInt(testInt);
			record.addShort(testShort);
			record.addChar(testChar);
		}
		
		assertFalse(records[0].equals(records[1]));
	}
	
	public void testRecordSerialization() {
		Record r = new Record();
		
		r.addByte((byte) 1);
		r.addChar('a');
		r.addShort((short) 40);
		r.addString("MyString");
		r.addInt(5000);
		
		String expected = "{\"sourceId\":1,\"data\":[1,\"a\",40,\"MyString\",5000]}";
		String[] str = {new JSONRecordSerializer().serialize(r),
				r.toString(new JSONRecordSerializer())};
		
		assertTrue("Both serializations should be equal", str[0].equals(str[1]));
		assertTrue("Actual output did not match expected output", str[0].equals(expected));
	}
	
	public void testRecordSetSerialization() {
		RecordSet rs = new RecordSet();
		Record r1 = new Record(), r2 = new Record();
		
		r1.addByte((byte) 1);
		r1.addChar('a');
		r1.addShort((short) 40);
		r1.addString("MyString");
		r1.addInt(5000);
		
		r2.addString("MySecondString");
		r2.addChar('b');
		r2.addInt(4);
		
		rs.putInfoKey("key1", "val1");
		rs.putInfoKey("key2", "val2");
		rs.addRecord(r1);
		rs.addRecord(r2);
		
		String expected = "{\"key1\":\"val1\",\"key2\":\"val2\",\"records\":[{\"sourceId\":1,\"data\":[1,\"a\",40,\"MyString\",5000]},{\"sourceId\":2,\"data\":[\"MySecondString\",\"b\",4]}]}";
		String[] str = {new JSONRecordSetSerializer().serialize(rs),
				rs.toString(new JSONRecordSetSerializer())};
		
		assertTrue("Both serializations should be equal", str[0].equals(str[1]));
		assertTrue("Actual output did not match expected output", str[0].equals(expected));
	}

}
