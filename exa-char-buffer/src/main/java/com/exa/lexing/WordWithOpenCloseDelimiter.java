package com.exa.lexing;

import com.exa.buffer.CharReader;
import com.exa.utils.ManagedException;

public class WordWithOpenCloseDelimiter extends WordSeparator {

	protected Character closeDelimiter;
	
	public WordWithOpenCloseDelimiter(LexingRules lexer, Character openDelimiter, Character closeDelimiter) {
		super(openDelimiter.toString(), lexer);
		
		this.closeDelimiter = closeDelimiter;
	}
	
	public Character getCloseDelimiter() {
		return closeDelimiter;
	}

	@Override
	public void nextToEndOfWord(CharReader charReader) throws ParsingException {
		charReader.nextToChar(closeDelimiter);
	}
	
	@Override
	public void nextToEndOfExpression(CharReader charReader) throws ManagedException {
		nextToEndOfWord(charReader);
	}

	@Override
	public boolean isFirstCharManager() {
		return true;
	}

	@Override
	public CharProperty<Boolean> nextUntilEnd(CharReader script) throws ParsingException {
		Character ch = script.nextChar();
		if(ch == null || ch == closeDelimiter) return new CharProperty<Boolean>(ch, Boolean.FALSE);
		
		return new CharProperty<>(ch, Boolean.TRUE);
	}
	
	
	
}
