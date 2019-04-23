package com.exa.chars;

import java.nio.CharBuffer;

import com.exa.buffer.ReadingBuffer;
import com.exa.lexing.ParsingException;
import com.exa.utils.ManagedException;

public class EscapeCharMan {
	public static final EscapeCharMan NO_ESCAPE = new EscapeCharMan();
	
	public static final EscapeCharMan  STANDARD = new EscapeCharMan() {

		@Override
		public char translated(ReadingBuffer readingBuffer) {
			Character c = readingBuffer.currentChar();
			if(c == null) throw new IllegalStateException("No char in read in buffer");
			if(c == '\\') {
				Character nc;
				if((nc = readingBuffer.nextChar()) == null) throw new IllegalStateException("EOF met while expecting a char after escape character.");
				
				if(nc == 'n') return '\n';
				if(nc == 'r') return '\r';
				if(nc == 't') return '\t';
				if(nc == '\\') return '\\';
				
				throw new IllegalStateException("Unknown escape char '" +nc+ "'");
			}
			return c;
		}

		@Override
		public void normalized(CharBuffer charBuffer) {
			charBuffer.position(0);
			while(charBuffer.hasRemaining()) {
				int p = charBuffer.position();
				char c = charBuffer.get();
				if(c != '\\') continue;
				
				if(charBuffer.remaining() == 0) throw new IllegalStateException("The char buffer no contain remaining char while expecting a char after escape character.");
				c = charBuffer.get();
				if(c == 'n') {
					charBuffer.put(p, '\n');
					for(int i= p+1; i<charBuffer.limit()-1; ++i) {
						charBuffer.put(i, charBuffer.get(i+1));
					}
					charBuffer.position(p+1);
					charBuffer.limit(charBuffer.limit() - 1);
					continue;
				}
				if(c == 'r') {
					charBuffer.put(p, '\r');
					for(int i= p+1; i<charBuffer.limit()-1; ++i) {
						charBuffer.put(i, charBuffer.get(i+1));
					}
					charBuffer.position(p+1);
					charBuffer.limit(charBuffer.limit() - 1);
					continue;
				}
				
				if(c == 't') {
					charBuffer.put(p, '\t');
					for(int i= p+1; i<charBuffer.limit()-1; ++i) {
						charBuffer.put(i, charBuffer.get(i+1));
					}
					charBuffer.position(p+1);
					charBuffer.limit(charBuffer.limit() - 1);
					continue;
				}
				
				if(c == '\\') {
					charBuffer.put(p, '\\');
					for(int i= p+1; i<charBuffer.limit()-1; ++i) {
						charBuffer.put(i, charBuffer.get(i+1));
					}
					charBuffer.position(p+1);
					charBuffer.limit(charBuffer.limit() - 1);
					continue;
				}
				
				throw new IllegalStateException("Unknown escape char '" +c+ "'");
			}
			
			charBuffer.position(0);
		}
		
		@Override
		public void normalized(StringBuilder sb) {
			for(int i = 0; i<sb.length(); ++i) {
				char c = sb.charAt(i);
				if(c != '\\') continue;
				
				if(i == sb.length()-1) throw new IllegalStateException("The string no contain remaining char while expecting a char after escape character.");
				c = sb.charAt(i+1);
				if(c == 'n') {
					sb.replace(i, i+2, "\n");
					continue;
				}
				
				if(c == 'r') {
					sb.replace(i, i+2, "\r");
					continue;
				}
				
				if(c == 't') {
					sb.replace(i, i+2, "\t");
					continue;
				}
				
				if(c == '\\') {
					sb.replace(i, i+2, "\\");
					continue;
				}
				
				throw new IllegalStateException("Unknown escape char '" +c+ "'");
				
			}
		}

		@Override
		public String escaped(char c) {
			switch(c) {
				case '\n': return "\\n";
				case '\r': return "\\r";
				case '\t': return "\\t";
				case '\\': return "\\\\";
				default: return null;
			}
		}

		@Override
		public boolean escapable(char c) {
			return c == 'n' || c == 'r' || c == 't' || c == '\\';
		}

		@Override
		public Character translated(String ec) {
			if(ec.equals("\\n")) return '\n';
			
			if(ec.equals("\\r")) return '\r';
			
			if(ec.equals("\\t")) return '\t';
			
			if(ec.equals("\\\\")) return '\\';
			
			return super.translated(ec);
		}
		
		
		
		
	};
	

	public char translated(ReadingBuffer readingBuffer) throws ParsingException { 
		return readingBuffer.currentChar(); 
	}
	
	public void normalized(CharBuffer charBuffer) {
		charBuffer.position(0);
	}
	
	public void normalized(StringBuilder sb) {}
	
	public String escaped(char c) { return null; }
	
	public boolean escapable(char c) { return false; }
	
	public Character translated(String ec) {
		if(ec.length() == 1) return ec.charAt(0);
		
		return null;
	}

}
