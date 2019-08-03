package com.exa.lexing;

import com.exa.buffer.CharReader;

public class CommentWord extends WordSeparator {
	public static interface EndOfWord {
		boolean ok(StringBuilder sb);
	}
	
	private EndOfWord eow;
	
	public CommentWord(String keyword, LexingRules lexer, EndOfWord eow) {
		super(keyword, lexer);
		
		this.eow = eow;
	}
	
	@Override
	public boolean isBlank() {	return true;  }

	@Override
	public CharProperty<Boolean> nextUntilEnd(CharReader script, StringBuilder bufferContent) throws ParsingException {
		Character ch = script.nextChar();
		
		if(ch != null) bufferContent.append(ch);
		if(ch == null || eow.ok(bufferContent)) return new CharProperty<Boolean>(ch, Boolean.FALSE);
		
		return new CharProperty<>(ch, Boolean.TRUE);
	}
	
	
	
	
}
