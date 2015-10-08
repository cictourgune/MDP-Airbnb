package org.tourgune.mdp.airbnb.core;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tourgune.mdp.airbnb.utils.Args;

public class Record {

	private final List<RecordTypes> layout;
	private final Map<Integer, Integer> stringLengths;
	private final List<Integer> fieldOffsets;
	private ByteBuffer buffer;
	
	public Record() {
		layout = new ArrayList<RecordTypes>();
		stringLengths = new HashMap<Integer, Integer>();
		fieldOffsets = new ArrayList<Integer>();
		buffer = ByteBuffer.allocate(40);	// 10 ints
	}
	
	/**
	 * @throws IndexOutOfBoundsException If the supplied field number is greater than the highest field number.
	 * @throws IllegalArgumentException  If the supplied field number is negative, or the field is not a string field.
	 * @param fn
	 * @return
	 */
	public String getString(int fn) {
		Args.checkPositiveOrZero("Field number cannot be negative", fn);
		int offset = fieldOffsets.get(fn);
		
		Args.checkNotNull("Field " + fn + " is not a string field", stringLengths.get(fn));
		byte[] strBytes = new byte[stringLengths.get(fn)];
		
		for (int i = 0; i < strBytes.length; i++)
			strBytes[i] = buffer.get(offset++);
		return new String(strBytes);
	}
	
	/**
	 * @throws IndexOutOfBoundsException If the supplied field number is greater than the highest field number.
	 * @param fn
	 * @return
	 */
	public int getInt(int fn) {
		Args.checkPositiveOrZero("Field number cannot be negative", fn);
		int offset = fieldOffsets.get(fn);
		return buffer.getInt(offset);
	}
	
	public float getFloat(int fn) {
		Args.checkPositiveOrZero("Field number cannot be negative", fn);
		int offset = fieldOffsets.get(fn);
		return buffer.getFloat(offset);
	}
	
	public double getDouble(int fn) {
		Args.checkPositiveOrZero("Field number cannot be negative", fn);
		int offset = fieldOffsets.get(fn);
		return buffer.getDouble(offset);
	}
	
	public short getShort(int fn) {
		Args.checkPositiveOrZero("Field number cannot be negative", fn);
		int offset = fieldOffsets.get(fn);
		return buffer.getShort(offset);
	}
	
	public char getChar(int fn) {
		Args.checkPositiveOrZero("Field number cannot be negative", fn);
		int offset = fieldOffsets.get(fn);
		return buffer.getChar(offset);
	}
	
	public byte getByte(int fn) {
		Args.checkPositiveOrZero("Field number cannot be negative", fn);
		int offset = fieldOffsets.get(fn);
		return buffer.get(offset);
	}
	
	public Record addString(String s) {
		return addString(s, null);
	}
	
	public Record addString(String s, Charset ch) {
		byte[] strBytes = null;
		int offset = 0;
		
		Args.checkNotNull("String 's' must not be null", s);
		
		strBytes = s.getBytes(ch == null ? Charset.defaultCharset() : ch);
		reallocIfNeeded(strBytes.length);
		offset = buffer.position();
		layout.add(RecordTypes.STRING);
		fieldOffsets.add(offset);
		stringLengths.put(layout.size() - 1, strBytes.length);
		buffer.put(strBytes);
		
		return this;
	}
	
	public Record addInt(int n) {
		int offset = 0;
		
		reallocIfNeeded(4);
		offset = buffer.position();
		layout.add(RecordTypes.INTEGER);
		fieldOffsets.add(offset);
		buffer.putInt(n);
		
		return this;
	}
	
	public Record addFloat(float n) {
		int offset = 0;
		
		reallocIfNeeded(4);
		offset = buffer.position();
		layout.add(RecordTypes.FLOAT);
		fieldOffsets.add(offset);
		buffer.putFloat(n);
		
		return this;
	}
	
	public Record addDouble(double n) {
		int offset = 0;
		
		reallocIfNeeded(8);
		offset = buffer.position();
		layout.add(RecordTypes.DOUBLE);
		fieldOffsets.add(offset);
		buffer.putDouble(n);
		
		return this;
	}

	public Record addBytes(byte[] b) {
		int offset = 0;
		
		Args.checkNotNull("Byte array 'b' must not be null", b);
		
		reallocIfNeeded(b.length);
		for (byte curByte : b) {
			offset = buffer.position();
			layout.add(RecordTypes.BYTE);
			fieldOffsets.add(offset);
			buffer.put(b);
		}
		
		return this;
	}
	
	public Record addByte(byte b) {
		reallocIfNeeded(1);
		
		int offset = buffer.position();
		
		layout.add(RecordTypes.BYTE);
		fieldOffsets.add(offset);
		buffer.put(b);
		
		return this;
	}
	
	public Record addChar(char c) {
		reallocIfNeeded(2);
		
		int offset = buffer.position();
		
		layout.add(RecordTypes.CHAR);
		fieldOffsets.add(offset);
		buffer.putChar(c);
		
		return this;
	}
	
	public Record addShort(short s) {
		reallocIfNeeded(2);
		
		int offset = buffer.position();
		
		layout.add(RecordTypes.SHORT);
		fieldOffsets.add(offset);
		buffer.putShort(s);
		
		return this;
	}
	
	public RecordTypes[] getLayout() {
		return this.layout.toArray(new RecordTypes[0]);
	}
	
	public ByteBuffer getBuffer() {
		return buffer;
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		Record anotherRecord = null;
		byte[] byteArray = this.buffer.array(),
				otherByteArray = null;
		int index = 0;
		
		if (obj instanceof Record) {
			anotherRecord = (Record) obj;
			otherByteArray = anotherRecord.getBuffer().array();
			try {
				do {
					if (byteArray[index] == otherByteArray[index]) {
						isEqual = true;
						index++;
					} else
						isEqual = false;
				} while(isEqual && index < otherByteArray.length);
			} catch (ArrayIndexOutOfBoundsException e) {
				// this most probably means "byteArray" and "otherByteArray" don't have the same length
				isEqual = false;
			}
		}
		
		return isEqual;
	}

	private void reallocIfNeeded(int nBytes) {
		if (buffer.remaining() < nBytes) {
			// we're out of space, so realloc the buffer
			byte[] curBytes = buffer.array();
			int oldCapacity = 0;
			do {
				oldCapacity = buffer.capacity();
				// TODO will throw IllegalArgumentException if capacity wraps
				buffer = ByteBuffer.allocate(oldCapacity << 1);
			} while(buffer.capacity() - oldCapacity < nBytes);
			buffer.put(curBytes);
		}
	}

	@Override
	public String toString() {
		return new JSONRecordSerializer().serialize(this);
	}
	
	public String toString(JSONRecordSerializer serializer) {
		return serializer.serialize(this);
	}
}
