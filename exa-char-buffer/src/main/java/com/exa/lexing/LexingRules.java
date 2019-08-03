package com.exa.lexing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.exa.buffer.CharReader;
import com.exa.buffer.CharReader.ClientBuffer;
import com.exa.utils.ManagedException;

public class LexingRules {
	protected String blankCharacters;
	
	public final static ActiveWord AW_FAKE_WORD_SEPARATOR = new WordSeparator(null, null);
	protected HashMap<String, ActiveWord> activeWords;
	
	protected String lastWrd = null;
	
	protected StringBuilder sbBlank = new StringBuilder();

	public LexingRules(String blankCharacters, HashMap<String, ActiveWord> activeWords) {
		this.blankCharacters = blankCharacters;
		this.activeWords = activeWords;
	}
	
	public LexingRules(String blankCharacters) {
		this.blankCharacters = blankCharacters;
		this.activeWords = new HashMap<String, ActiveWord>();
	}
		
	public HashMap<String, ActiveWord> getActiveWords() { return activeWords; }
	
	public void addWordSeparator(WordSeparator aw) {
		StringBuilder wrd = new StringBuilder();
		String awName = aw.getKeyword();
		
		for(int i=0; i<awName.length()-1; i++) {
			wrd.append(awName.charAt(i));
			
			ActiveWord iaw = activeWords.get(wrd.toString());
			if(iaw == null) activeWords.put(wrd.toString(), AW_FAKE_WORD_SEPARATOR);
		}
		
		activeWords.put(awName, aw);
	}
	
	public void addWordSeparator(String w) {
		addWordSeparator(new WordSeparator(w, this));
	}
	
	public void addWordSeparator(String ... words) {
		for(String w : words) addWordSeparator(new WordSeparator(w, this));
	}
	
	public void addActiveWord(String key, ActiveWord aw) {
		activeWords.put(key, aw);
	}
	
	public void addActiveWord(ActiveWord aw) {
		activeWords.put(aw.getKeyword(), aw);
	}
	
	public ActiveWord getActiveWord(String awName) {
		ActiveWord aw = activeWords.get(awName);
		
		if(aw == null)
			if(blankCharacters.contains(awName)) return ActiveWord.BLANK_WORD;
		
		return aw;
	}
		
	public Character nextNonBlankChar(CharReader charReader) throws ParsingException {
		Character currentChar;
		sbBlank.setLength(0);
		
		while((currentChar = charReader.nextChar()) != null) {
			ActiveWord aw = getActiveWord(currentChar.toString());
			
			if(aw == null) return currentChar;
			if(aw.isBlank()) {
				sbBlank.append(currentChar);
				continue;
			}
			
			if(aw == AW_FAKE_WORD_SEPARATOR) {
				StringBuilder wrd = new StringBuilder(currentChar.toString());
				
				boolean continueSeeking = false;
				
				while((currentChar = charReader.nextChar()) != null) {
					wrd.append(currentChar);
					
					aw = getActiveWord(wrd.toString());
					
					if(aw == null) {
						charReader.back(wrd.substring(1));
						
						return wrd.charAt(0);
					}
					
					if(aw == AW_FAKE_WORD_SEPARATOR) continue;
					
					if(aw.isBlank()) {
						CharProperty<Boolean> chPr;
						do {
							chPr = aw.nextUntilEnd(charReader, wrd);
							if(chPr.getCharacter() == null) throw new ParsingException(String.format("Unexpected end of file after '%s'", aw.getKeyword()));
							
						} while(chPr.getProperty());
						sbBlank.append(wrd);
						continueSeeking = true;
						break;
					}
				}
				if(continueSeeking) continue;
				
				return wrd.charAt(0);
			}
			return currentChar;
		}
		
		return null;
	}
	
	public Character beforeNextNonBlankChar(CharReader charReader) throws ManagedException {
		Character currentChar;
		
		while((currentChar = charReader.nextChar()) != null) {
			ActiveWord aw = getActiveWord(currentChar.toString());
			
			if(aw == null) {
				charReader.back(currentChar.toString());
				return currentChar;
			}
			if(aw.isBlank()) continue;
			
			charReader.back(currentChar.toString());
			
			return currentChar;
		}
		
		return null;
	}
	
	private String nextWordStartingWithNonWS(CharReader script) throws ParsingException {
		StringBuilder wrd = new StringBuilder();
		
		Character currentChar;
		while((currentChar = script.nextChar()) != null) {
			ActiveWord aw = getActiveWord(currentChar.toString());
			
			if(aw == null) 
				wrd.append(currentChar);
			else if(aw.isWordSeparator()) {
				if(aw != AW_FAKE_WORD_SEPARATOR) {
					script.back(currentChar.toString());
					break;
				}
				
				StringBuilder wsSB = new StringBuilder(); wsSB.append(currentChar);
				while((currentChar = script.nextChar()) != null) {
					wsSB.append(currentChar);
					aw = getActiveWord(wsSB.toString());
					
					if(aw == null) {
						wrd.append(wsSB.toString());
						break;
					}
					
					if(aw.isWordSeparator()) {
						if(aw == AW_FAKE_WORD_SEPARATOR) continue;
						
						script.back(wsSB.toString());
						return wrd.toString();
					}
					
					wrd.append(wsSB.toString());
					break;
				}
			}
		}
		return wrd.toString();
	}
	
	private String nextWordStartingWithWS(CharReader script, ActiveWord ws/*, String currentString*/) throws ParsingException {
		String currentString = ws.getKeyword();
		StringBuilder wsSB = new StringBuilder(currentString); //wsSB.append(currentString);
		
		StringBuilder wrd = new StringBuilder(currentString); //wrd.append(currentString);
		
		boolean processExit = false;
		Character currentChar;
		while((currentChar = script.nextChar()) != null) {
			wsSB.append(currentChar);
			
			ActiveWord aw = getActiveWord(wsSB.toString());
			if(aw == null) {
				script.back(wsSB.substring(wrd.length()));
				
				if(ws.isBlank()) {
					StringBuilder sb = new StringBuilder(wsSB.substring(0, wsSB.length()-1));
					CharProperty<Boolean> chPr;
					do {
						chPr = ws.nextUntilEnd(script, sb);
						if(chPr.getCharacter() == null) throw new ParsingException(String.format("Unexpected end of file after '%s'", ws.getKeyword()));
						
						//sb.append(chPr.getCharacter());
					} while(chPr.getProperty());
					
					return nextString(script);
				}
				
				processExit = true;
				break;
			}
			if(aw.isWordSeparator()) {
				if(aw == AW_FAKE_WORD_SEPARATOR) {
					
					continue;
				}
				
				wrd.setLength(0);
				wrd.append(wsSB);
			}
			script.back(wsSB.substring(wrd.length()));
			processExit = true;
			break;
		}
		if(processExit) return wrd.toString();
		
		script.back(wsSB.substring(wrd.length()));
				
		return wrd.toString();
	}
	
	private String nextWordStartingWithFakeWS(CharReader script, Character currentChar) throws ParsingException {
		StringBuilder wrd = new StringBuilder(currentChar.toString()); //wrd.append(currentChar);
		
		while((currentChar = script.nextChar()) != null) {
			wrd.append(currentChar);
			
			ActiveWord aw = getActiveWord(wrd.toString());
			
			if(aw == null) 
				return wrd.toString() + nextWordStartingWithNonWS(script);
			
			if(aw.isWordSeparator()) {
				if(aw == AW_FAKE_WORD_SEPARATOR) continue;
				
				return nextWordStartingWithWS(script, aw);
			}
			
			return wrd.toString() + nextWordStartingWithNonWS(script);
		}
		
		return wrd.toString();
	}
	
	public String nextString(CharReader script) throws ParsingException {
		StringBuilder wrd = new StringBuilder();
		
		Character currentChar = nextNonBlankChar(script);
		
		if(currentChar == null) return lastWrd = null;
		
		ActiveWord aw = getActiveWord(currentChar.toString());
		
		if(aw == null) return lastWrd = currentChar + nextWordStartingWithNonWS(script);
		
		if(aw.isFirstCharManager()) {
			StringBuilder sb = new StringBuilder(currentChar.toString());
			
			CharProperty<Boolean> chPr;
			do {
				chPr = aw.nextUntilEnd(script, sb);
				if(chPr.getCharacter() == null) break;
				
				//sb.append(chPr.getCharacter());
			} while(chPr.getProperty());
			
			return lastWrd = sb.toString();
		}
		
		if(aw.isWordSeparator()) {
			if(aw == AW_FAKE_WORD_SEPARATOR) return lastWrd = nextWordStartingWithFakeWS(script, currentChar);
			return lastWrd = nextWordStartingWithWS(script, aw /*currentChar.toString()*/);
		}
		
		return lastWrd = wrd.toString();
	}
	
	public Integer nextInteger(CharReader script) throws ManagedException {
		String str = nextString(script);
		if(str == null) return null;
		
		try {
			return Integer.valueOf(str);
		}
		catch(NumberFormatException e) {
			return null;
		}
		catch(Exception e) {
			throw new ManagedException(e);
		}
	}
	
	public Long nextLong(CharReader script) throws ManagedException {
		String str = nextString(script);
		if(str == null) return null;
		
		try {
			return Long.valueOf(str);
		}
		catch(NumberFormatException e) {
			return null;
		}
		catch(Exception e) {
			throw new ManagedException(e);
		}
	}
	
	public String nextNonNullString(CharReader charReader) throws ParsingException {
		String res;
		try {
			res = nextString(charReader);
			if(res == null) throw new ParsingException("Missing word");
			
			return res;
		} catch (ManagedException e) {
			throw new ParsingException(e);
		}
	}
	
	public void expectedWord(CharReader charReader, String wrd) throws ParsingException {
		if(wrd.equals(nextNonNullString(charReader))) return;
		
		throw new ParsingException(wrd+" expected.");
	}
	
	public String expectedWords(CharReader charReader, String ... wrds) throws ParsingException {
		Set<String> wrdSet = new HashSet<String>(wrds.length);
		for(String w : wrds) {
			wrdSet.add(w);
		}
		
		return expectedWords(charReader, wrdSet);
	}
	
	public String expectedWords(CharReader charReader, Set<String> wrds) throws ParsingException {
		String wrd = nextNonNullString(charReader);
		
		if(wrds.contains(wrd)) return wrd;
		throw new ParsingException("Unexpected '"+wrd+"'");
	}
		
	public String findRequired(CharReader charReader, String ... wrds) throws ParsingException {
		do {
			String curWrd = nextNonNullString(charReader);
			
			for(String w : wrds)
				if(curWrd.equals(w)) return w;
			
			ActiveWord aw = getActiveWord(curWrd);
			if(aw == null) continue;
			try {aw.nextToEndOfExpression(charReader);} catch(Exception e) { throw new ParsingException(e); }
		} while(true);
	}

	public boolean findRequired(CharReader charReader, String wrd) throws ParsingException {
		do {
			String curWrd = nextNonNullString(charReader);
			
			if(curWrd.equals(wrd)) return true;
			
			ActiveWord aw = getActiveWord(curWrd);
			if(aw == null) continue;
			try {aw.nextToEndOfExpression(charReader);} catch(Exception e) { throw new ParsingException(e); }
		} while(true);
	}

	public boolean find(CharReader charReader, String ... wrds) throws ManagedException {
		do {
			String curWrd = nextString(charReader);
			if(curWrd == null) return false;
			
			for(String w : wrds)
				if(curWrd.equals(w)) return true;
			
			ActiveWord aw = getActiveWord(curWrd);
			if(aw == null) continue;
			
			aw.nextToEndOfExpression(charReader);
		} while(true);
	}
	
	public Character nextForwardChar(CharReader charReader) throws ManagedException {
		Character currentChar = charReader.nextChar();
		if(currentChar == null) return null;
		
		charReader.back(currentChar.toString());
		
		return currentChar;
	}
		
	public Character nextForwardNonBlankChar(CharReader charReader) throws ManagedException {
		Character currentChar = nextNonBlankChar(charReader);
		if(currentChar == null) return null;
		
		charReader.back(currentChar.toString());
		
		return currentChar;
	}
	
	public Character nextForwardRequiredNonBlankChar(CharReader charReader) throws ManagedException {
		
		Character currentChar = nextForwardNonBlankChar(charReader);
		if(currentChar == null) throw new ParsingException("Non blank char expected before end of file");
		
		return currentChar;
	}
	
	public String readBlank() { return sbBlank.toString(); }
		
	public String trimLeft(String str) {
		StringBuilder sb = new StringBuilder(str);
		int i = 0;
		
		for(i = 0; i<sb.length(); i++) {
			Character c = sb.charAt(i);
			if(blankCharacters.contains(c.toString())) continue;
			
			break;
		}
		if(i == 0) return sb.toString();
		
		sb.delete(0, i);
		return sb.toString();
	}

	public String trimLeft(ClientBuffer db) {
		int i = 0;
		int nb = db.length();
		for(i = 0; i<nb; i++) {
			Character c = db.charAt(i);
			if(blankCharacters.contains(c.toString())) continue;
			
			break;
		}
		if(i == 0) return db.toString();
		
		return db.substring(i);		
	}
	
	public boolean isBlank(Character ch) {
		return blankCharacters.contains(ch.toString());
	}
	
}
