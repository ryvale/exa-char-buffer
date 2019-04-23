package com.exa.buffer;

import com.exa.chars.EscapeCharMan;
import com.exa.lexing.ParsingException;

public class RBRAM extends ReadingBuffer {
	protected String string;
	protected int start, end, position;
	protected Character currentChar;

	public RBRAM(String string, int start, int end) {
		if(start<0) throw new IllegalArgumentException();
		if(end>string.length()) throw new IllegalArgumentException();
		if(start>end) throw new IllegalArgumentException();
		
		this.string = string;
		this.start = start;
		this.end = end;
		this.position = 0;
	}
	
	public RBRAM(String string) { this(string, 0, string.length());}

	@Override
	public Character nextChar() {
		if(position+1>end) return null;
		
		return currentChar = string.charAt(position++);
	}

	@Override
	public String substring(int start, int end, EscapeCharMan em) {
		StringBuilder sb = new StringBuilder(string.substring(this.start + start, this.start + end));
		em.normalized(sb);
		return sb.toString();
	}

	@Override
	public int position() { return position - start; }

	@Override
	public void position(int newPosition) {
		this.position = newPosition + start;
	}

	@Override
	public ReadingBuffer clone() {
		return new RBRAM(string, start, end);
	}

	@Override
	public Character currentChar() { return currentChar; }

	@Override
	public char charAt(int index, EscapeCharMan em) {
		return string.charAt(start + index);
	}

	@Override
	public void reset() { this.position = 0; }

	@Override
	public boolean back(String str, EscapeCharMan em) throws ParsingException {
		int p1 = str.length() - 1; int p2 = position-1;
		StringBuilder sbStr = new StringBuilder(str);
		
		while(p1>=0 && p2>=0) {
			char c1 = sbStr.charAt(p1);
			char c2 = string.charAt(p2);
			
			if(c1 == c2) { 
				--p1; --p2; 
				continue; 
			}
			
			String ec = em.escaped(c1);
			
			if(ec == null) return false;
			
			sbStr.replace(p1, p1+1, ec);
			if(sbStr.charAt(p1+ec.length()-1) != c2) return false;
			p1 += ec.length()-2;
			--p2;
		}
		position -= sbStr.length();
		return true;
	}

	@Override
	public RBRAM asRAMReadingBuffer() {	return this; }

	@Override
	public String string(EscapeCharMan ecm) {
		return substring(start, end, ecm);
	}

	
	
	
}
