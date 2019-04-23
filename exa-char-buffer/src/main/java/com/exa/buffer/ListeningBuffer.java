package com.exa.buffer;

import com.exa.lexing.ParsingException;
import com.exa.utils.ManagedException;

public abstract class ListeningBuffer {

	public abstract void charRead(char c);
	
	public abstract int position();
	
	public abstract ListeningBuffer rewind(int n) throws ParsingException;
	
	public abstract ListeningBuffer back(String str) throws ManagedException;
	
	public abstract String substring(int start, int end);
	
	public abstract char charAt(int index);
}
