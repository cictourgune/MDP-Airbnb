package org.tourgune.mdp.airbnb.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class Utils {

	public static String[] toStringArray(int[] ary) {
		Args.checkNotNull("ary cannot be null", ary);
		
		String[] dst = new String[ary.length];
		
		for (int index = 0; index < ary.length; index++)
			dst[index] = String.valueOf(ary[index]);
		
		return dst;
	}
	
	public static int[] toIntArray(String[] ary) {
		return toIntArray(ary, false);
	}
	
	public static int[] toIntArray(String[] ary, boolean skipErrors) {
		Args.checkNotNull("ary cannot be null", ary);
		
		int[] result = new int[ary.length];
		List<Integer> dst = new ArrayList<Integer>(ary.length);
		
		for (int index = 0; index < ary.length; index++) {
			try {
				dst.set(index, Integer.parseInt(ary[index]));
			} catch (NumberFormatException e) {
				if (skipErrors)
					dst.remove(index);
				else
					throw e;
			}
		}
		
		for (int index = 0; index < dst.size(); index++)
			result[index] = dst.get(index).intValue();
		
		return result;
	}
	
	public static List<Integer> toIntList(List<String> list) {
		List<Integer> dst = new ArrayList<Integer>(list.size());
		
		for (Iterator<String> it = list.iterator(); it.hasNext();)
			dst.add(new Integer(it.next()));
		
		return dst;
	}
	
	public static List<String> toStringList(List<Integer> list) {
		List<String> dst = new ArrayList<String>(list.size());
		
		for (Iterator<Integer> it = list.iterator(); it.hasNext();)
			dst.add(it.next().toString());
		
		return dst;
	}
	
	public static String decompress(byte[] input, String codingScheme) {
		String result = "";
		
		try {
			InputStream is = null;
			ByteArrayInputStream bis = new ByteArrayInputStream(input);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte b = 0;
			
			switch (codingScheme.toLowerCase()) {
			case "gzip":
				is = new GZIPInputStream(bis);
				break;
			case "deflate":
				is = new InflaterInputStream(bis);
				break;
			default:
				is = bis;
				break;
			}
			
			while ((b = (byte) is.read()) != -1)
				os.write(b);
			result = os.toString();
			
			bis.close();
			is.close();
			os.close();
		} catch (IOException e) {
			result = "";
		}
		
		return result;
	}
}
