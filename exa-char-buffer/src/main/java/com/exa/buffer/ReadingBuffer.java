package com.exa.buffer;

import java.io.Closeable;
import java.io.IOException;

import com.exa.chars.EscapeCharMan;
import com.exa.lexing.ParsingException;

public abstract class ReadingBuffer implements Cloneable, Closeable {
	public abstract Character nextChar();
	public abstract boolean back(String str, EscapeCharMan em) throws ParsingException;
	
	public abstract Character currentChar();
	
	public abstract char charAt(int index, EscapeCharMan em);
	
	public abstract String substring(int start, int end, EscapeCharMan escapeCharMan);
	
	public abstract void reset();
	
	public String substring(int start, int end) {
		return substring(start, end, EscapeCharMan.NO_ESCAPE);
	}
	
	public abstract String string(EscapeCharMan ecm) throws IOException;
	
	public String string() throws IOException {
		return string(EscapeCharMan.NO_ESCAPE);
	}
	
	public abstract int position();
	public abstract void position(int newPosition);

	@Override
	public abstract ReadingBuffer clone();
	
	public void close() throws IOException {}
	
	public RBRAM asRAMReadingBuffer() { return null; }
	
	public RBMappedFile asMappedFileReadingBuffer() { return null; }

	//public abstract void back(String ch);
	
	
}
