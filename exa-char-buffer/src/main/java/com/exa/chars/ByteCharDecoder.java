package com.exa.chars;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ByteCharDecoder {
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static Charset charsetOf(ByteBuffer bb, Charset defaultCharSet) {
		Charset res = defaultCharSet;
		byte[] bom2Bytes = new byte[2];
		
		bb.get(bom2Bytes);
		
		String bomPart0 = bytesToHex(bom2Bytes);
		if("FEFF".equals(bomPart0)) return StandardCharsets.UTF_16BE; //Charset.forName("UTF-16");
		
		bb.get(bom2Bytes);
		
		String bomPart1 = bytesToHex(bom2Bytes);
		
		if("EFBB".equals(bomPart0) && "BF".equals(bomPart1.substring(0, 2))) {
			bb.position(bb.position() - 1);
			return StandardCharsets.UTF_8; // Charset.forName("UTF-8");
		}
		
		if("FFFE".equals(bomPart0)) {
			if("0000".equals(bomPart1)) return Charset.forName("UTF-32");
			bb.position(bb.position() - 2);
			return StandardCharsets.UTF_16LE;
		}
		
		if("0000".equals(bomPart0) && "FEFF".equals(bomPart0)) return Charset.forName("UTF-32");
		
		bb.position(bb.position() - 4);
		return res;
	}
	
	public static Charset charsetOf(ByteBuffer bb) { return charsetOf(bb, Charset.forName("UTF-8")); }

	
	public static byte[] bom(Charset charset) {
		String csName = charset.name();
		
		if(StandardCharsets.UTF_8.equals(csName) || charset.aliases().contains(StandardCharsets.UTF_16BE.name()))
			return hexStringToByteArray("EFBBBF");
		
		if(StandardCharsets.UTF_16BE.equals(csName) || charset.aliases().contains(StandardCharsets.UTF_16BE.name()))
			return hexStringToByteArray("FEFF");
		
		if(StandardCharsets.UTF_16LE.equals(csName) || charset.aliases().contains(StandardCharsets.UTF_16LE.name()))
			return  hexStringToByteArray("FFFE");
		
		if("UTF-32BE".equals(csName) || charset.aliases().contains("UTF-32BE"))
			return hexStringToByteArray("0000FEFF");
		
		if("UTF-32LE".equals(csName) || charset.aliases().contains("UTF-32LE"))
			return hexStringToByteArray("FFFE0000");
		
		return null;
	}
}
