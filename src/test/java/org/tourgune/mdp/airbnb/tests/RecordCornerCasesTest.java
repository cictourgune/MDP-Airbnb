package org.tourgune.mdp.airbnb.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.tourgune.mdp.airbnb.core.Record;

public class RecordCornerCasesTest {

	private static final String testString = "randomString";
	private static final int testInt = 123456;
	private static final short testShort = 32767;
	private static final char testChar = 'a';
	private static final byte testByte = 127;
	
	@Test
	public void diffOrderReadWrite() {
		Record r = new Record();
		
		r.addByte(testByte);
		r.addChar(testChar);
		r.addShort(testShort);
		r.addInt(testInt);
		r.addString(testString);
		
		assertTrue("Corrupted short field (field no. 2)", r.getShort(2) == testShort);
		assertTrue("Corrupted byte field (field no. 0)", r.getByte(0) == testByte);
		assertTrue("Corrupted string field (field no. 4)", r.getString(4).equals(testString));
		assertTrue("Corrupted char field (field no. 1)", r.getChar(1) == testChar);
		assertTrue("Corrupted integer field (field no. 3)", r.getInt(3) == testInt);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNegativeSourceId() {
		Record r = new Record();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testZeroSourceId() {
		Record r = new Record();
	}
	
	@Test
	public void testNegativeFields() {
		Record r = new Record();
		
		try{
			try{
				r.getByte(-1);
				fail("getByte() should have thrown IllegalArgumentException");
			}catch(IllegalArgumentException e){}
			
			try {
				r.getChar(-1);
				fail("getChar() should have thrown IllegalArgumentException");
			}catch(IllegalArgumentException e){}
			
			try{
				r.getShort(-1);
				fail("getShort() should have thrown IllegalArgumentException");
			}catch(IllegalArgumentException e){}
			
			try{
				r.getInt(-1);
				fail("getInt() should have thrown IllegalArgumentException");
			}catch(IllegalArgumentException e){}
			
			try{
				r.getString(-1);
				fail("getString() should have thrown IllegalArgumentException");
			}catch(IllegalArgumentException e){}
		}catch(Exception e){
			fail("An IllegalArgumentException should have been thrown");
		}
	}
	
	@Test
	public void testRecordEqualsNull() {
		Record r = new Record();
		assertFalse(r.equals(null));
	}
	
	@Test(expected = NullPointerException.class)
	public void testNullString() {
		Record r = new Record();
		r.addString(null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testNullByteArray() {
		Record r = new Record();
		r.addBytes(null);
	}
}
