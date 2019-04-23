package com.exa.buffer;

import com.exa.lexing.ParsingException;
import com.exa.utils.ManagedException;

public class LBCharReaderPointer extends ListeningBuffer {
	protected CharReader charReader;
	protected int start;
	protected int end;
	
	protected LBCharReaderPointer(CharReader charReader, int start, int end) {
		super();
		this.charReader = charReader;
		this.start = start;
		this.end = end;
	}
	
	public LBCharReaderPointer(CharReader charReader) {
		this(charReader, charReader.readingBuffer.position(), charReader.readingBuffer.position());
	}
	
	
	@Override
	public void charRead(char c) { end = charReader.readingBuffer.position(); }

	@Override
	public int position() {	return end - start;	}

	@Override
	public ListeningBuffer rewind(int n) throws ParsingException {
		//charReader.back(toString());
		end = charReader.readingBuffer.position();
		
		return this;
	}

	@Override
	public String substring(int start, int end) {
		return charReader.rbSubstring(this.start + start, this.start + end);
	}

	@Override
	public char charAt(int index) {
		return charReader.charAt(this.start+index);
	}

	@Override
	public String toString() {
		return charReader.rbSubstring(this.start, end);
	}

	@Override
	public ListeningBuffer back(String str) throws ManagedException {
		charReader.back(str);
		return this;
	}
	
	

}
