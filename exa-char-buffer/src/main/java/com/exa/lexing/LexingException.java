package com.exa.lexing;

import com.exa.utils.ManagedException;

public class LexingException extends ManagedException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String parsingString = null;
	private boolean realParsingException = false;
	
	public LexingException(String message, boolean realParsingException) {
		super(message);
		this.realParsingException = realParsingException;
	}

	public LexingException(String message, String parsingString) {
		super(message);
		this.parsingString = parsingString;
	}

	public LexingException(Throwable cause, String parsingString) {
		super(cause);
	}

	public String getParsingString() { return parsingString; }

	public boolean isRealParsingException() {
		return realParsingException;
	}

}
