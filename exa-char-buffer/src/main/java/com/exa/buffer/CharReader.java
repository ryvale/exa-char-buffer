package com.exa.buffer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.exa.chars.EscapeCharMan;
import com.exa.lexing.ParsingException;
import com.exa.utils.ManagedException;

public class CharReader implements Cloneable {
	public static abstract class ListeningBufferFactory {
		public abstract ListeningBuffer create(CharReader charReader);
	}
	
	public static final ListeningBufferFactory LBF_CHAR_READER_POINTER = new ListeningBufferFactory() {
		
		@Override
		public ListeningBuffer create(CharReader charReader) {
			return new LBCharReaderPointer(charReader);
		}
		
	};
	
	class SharedBufferMan {
		ListeningBuffer buffer = null;
		int refCount = 0;
		
		ListeningBuffer get() {
			if(++refCount == 1) buffer = lbf.create(CharReader.this);
			return buffer;
		}
		
		void release() {
			if(--refCount == 0) buffer = null;
		}
		
		void charRead(char c) {
			if(buffer == null) return;
			
			buffer.charRead(c);
		}
		
		void rewind(int n) throws ParsingException {
			if(buffer == null) return;
			
			buffer.rewind(n);
		}
	}
	
	public class ClientBuffer {
		protected ListeningBuffer listeningBuffer;
		protected int start;
		protected Integer end = null;
		
		private ClientBuffer() {
			listeningBuffer = sharedBufferMan.get();
			start = listeningBuffer.position();
		}
		
		public ClientBuffer release() {
			sharedBufferMan.release();
			
			end = listeningBuffer.position();
			
			return this;
		}
		
		public int normalizedEnd() {
			int res = (this.end == null ? listeningBuffer.position() : this.end);
			if(res> listeningBuffer.position()) res = listeningBuffer.position();
			return res;
		}
		
		public ClientBuffer rewind() throws ManagedException {
			//listeningBuffer.rewind(normalizedEnd() - start);
			listeningBuffer.back(toString());
			return this;
		}

		@Override
		public String toString() {
			return listeningBuffer.substring(start, normalizedEnd());
		}
		
		public int start() { return start; }
		public void start(int start) { this.start = start; }
		
		public Integer end() { return end; }
		
		public void end(int end) { this.end = end; }
		
		public int length() { return normalizedEnd() - start; }
		public char charAt(int index) { return listeningBuffer.charAt(start + index); }
		
		public String substring(int start, int end) {
			return listeningBuffer.substring(this.start + start, this.start + end);
		}
		
		public String substring(int start) {
			return listeningBuffer.substring(this.start + start, normalizedEnd());
		}
		
		public ClientBuffer markPosition() {
			start = listeningBuffer.position();
			return this;
		}
	}
	
	protected final ReadingBuffer readingBuffer;
	//protected final StringBuilder analysisBuffer;
	protected final List<CharReader> clones;
	
	protected SharedBufferMan sharedBufferMan =  new SharedBufferMan();
	protected EscapeCharMan escapeCharMan;
	protected ListeningBufferFactory lbf;

	public CharReader(ReadingBuffer readingBuffer, EscapeCharMan escapeCharMan, List<CharReader> clones/*, String analysisBuffer*, int position*/, ListeningBufferFactory lbf) {
		this.readingBuffer = readingBuffer;
		this.clones = clones;
		this.escapeCharMan = escapeCharMan;
		this.lbf = lbf;
	}
	
	public CharReader(String str, EscapeCharMan escapeCharMan) {
		this(new RBRAM(str), escapeCharMan, new ArrayList<CharReader>(), LBF_CHAR_READER_POINTER);
	}
	
	public CharReader(RandomAccessFile file, Charset charset, boolean autoDetectCharset, EscapeCharMan escapeCharMan) throws IOException {
		this(new RBMappedFile(file, charset, autoDetectCharset), escapeCharMan, new ArrayList<CharReader>()/*, "", 0*/, LBF_CHAR_READER_POINTER);
	}
	
	public CharReader(String str) {
		this(new RBRAM(str), EscapeCharMan.NO_ESCAPE, new ArrayList<CharReader>(), LBF_CHAR_READER_POINTER);
	}
	
	public Character nextChar() throws ParsingException {
		Character currentChar;
		
		/*if(analysisBuffer.length()>0) {
			currentChar = analysisBuffer.charAt(0);
			analysisBuffer.deleteCharAt(0);
		}
		else {*/
			currentChar = readingBuffer.nextChar();
			if(currentChar == null) return null;
			currentChar = escapeCharMan.translated(readingBuffer);
		//}
		addInDataBuffer(currentChar);
		//addInCloneAnalysisBuffer(currentChar);
		//position++;
		return currentChar;
	}
	
	private void addInDataBuffer(Character currentChar) {
		sharedBufferMan.charRead(currentChar);
	}

	/*public void addInAnalysisBuffer(Character ch) {
		analysisBuffer.insert(0, ch);
		position -= 1;
		sharedBufferMan.rewind(1);
	}
	
	public void addInAnalysisBuffer(String ch) {
		analysisBuffer.insert(0, ch);
		position -= ch.length();
		sharedBufferMan.rewind(ch.length());
	}
	*/
	
	public CharReader back(String ch) throws ParsingException {
		int p1 = readingBuffer.position();
		readingBuffer.back(ch, escapeCharMan);
		int p2 = readingBuffer.position();
		
		sharedBufferMan.rewind(p1-p2);
		return this; 
	}

	/*private void addInCloneAnalysisBuffer(Character ch) {
		for(CharReader cr : clones) {
			if(cr == this) continue;
			
			cr.addInAnalysisBuffer(ch);
		}
	}*/
	
	public int position() { return readingBuffer.position(); }
	
	public ReadingBuffer readingBuffer() { return readingBuffer; }

	@Override
	public CharReader clone()  {
		return new CharReader(readingBuffer, escapeCharMan, clones/*, analysisBuffer.toString(/, position*/, lbf);
	}
	
	
	public ClientBuffer listen() { return new ClientBuffer(); }
	
	//start and end in reading buffer coordinate
	public String rbSubstring(int start, int end) {
		return readingBuffer.substring(start, end, escapeCharMan);
	}
	
	public char charAt(int index) { return readingBuffer.charAt(index, escapeCharMan); }
	
	public void close() { }
	
	public Character nextWhileCharIn(String authorizedChars) throws ManagedException {
		Character currentChar;
		while((currentChar = nextChar()) != null) {
			if(authorizedChars.indexOf(currentChar.toString())>=0) continue;
			
			back(currentChar.toString());
			
			return currentChar;
		}
		return null;
	}
	
	public String nextForwardChar(int nb) throws ManagedException {
		StringBuilder res = new StringBuilder();
		
		for(int i=0; i < nb; i++) {
			Character currentChar = nextChar();
			if(currentChar == null) break;
			res.append(currentChar);
		}
		
		back(res.toString());
		return res.toString();
	}

	
	/*private void updateLineProperties(char lastChar) {
		if(lastChar=='\r' && currentChar=='\n') return;
		
		else if(currentChar=='\n' || currentChar=='\r') {
			lineNumber++;
			colPosInLine = 0;
		}
		else colPosInLine++;
	}*/
	
	public void nextToChar(char c) throws ParsingException {
		Character currentChar;
		while((currentChar = nextChar()) != null) {
			if(currentChar == c) return;
		}
	}

}
