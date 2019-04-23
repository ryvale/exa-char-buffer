package com.exa.buffer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import com.exa.chars.ByteCharDecoder;
import com.exa.chars.EscapeCharMan;
import com.exa.lexing.ParsingException;

public class RBMappedFile extends ReadingBuffer {
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	public static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;
	
	protected ByteBuffer mbb;
	private CharsetDecoder charsetDecoder;
	private int offset;
	protected CharBuffer buffer;
	
	public RBMappedFile(RandomAccessFile file, long start, int size, Charset charSet, int bufferSize, boolean autoDetectCharset) throws IOException {
		if(start<0) throw new IllegalArgumentException();
		if(size<0) throw new IllegalArgumentException();
		if(start + size>file.length()) throw new IllegalArgumentException();
		
		long sz = (size + 4 > file.length()) ? file.length() : size + 4;
		
		this.mbb = file.getChannel().map(MapMode.READ_ONLY, start, sz);
		if(autoDetectCharset) 
			charSet = ByteCharDecoder.charsetOf(mbb, charSet);
		
		offset = mbb.position();
		mbb.mark();
		
		size = (int)(file.length() - offset-1);
		
		mbb.limit(size);
		
		charsetDecoder = charSet.newDecoder();
		
		buffer = CharBuffer.allocate(bufferSize);
		buffer.limit(0);
	}
	
	public RBMappedFile(RandomAccessFile file, long start, Charset charSet, int bufferSize, boolean autoDetectCharset) throws IOException {
		if(start<0) throw new IllegalArgumentException();
		int size = (int)(file.length() - start);
		
		//this.file = file;
		long sz = (size + 4 > file.length()) ? file.length() : size + 4;
		
		this.mbb = file.getChannel().map(MapMode.READ_ONLY, start, sz);
		if(autoDetectCharset) 
			charSet = ByteCharDecoder.charsetOf(mbb, charSet);
		
		offset = mbb.position();
		mbb.mark();
		
		size = (int)(file.length() - offset-1);
		
		//mbb.limit(size);
		
		charsetDecoder = charSet.newDecoder();
		
		buffer = CharBuffer.allocate(bufferSize);
		buffer.limit(0);
	}
	
	/*public RBMappedFile(RandomAccessFile file, Charset charSet, boolean autoDetectCharset) throws IOException {
		
	}*/
	
	public RBMappedFile(String fileName, long start, int size, Charset charSet, int bufferSize, boolean autoDetectCharset) throws IOException {
		this(new RandomAccessFile(fileName, "rw"), start, size, charSet, bufferSize, autoDetectCharset);
	}
	
	public RBMappedFile(String fileName, long start, Charset charSet, int bufferSize, boolean autoDetectCharset) throws IOException {
		this(new RandomAccessFile(fileName, "rw"), start, charSet, bufferSize, autoDetectCharset);
	}
	
	public RBMappedFile(String fileName, Charset charSet, boolean autoDetectCharset) throws IOException {
		this(new RandomAccessFile(fileName, "rw"), 0, charSet, DEFAULT_BUFFER_SIZE, autoDetectCharset);
	}
	
	public RBMappedFile(RandomAccessFile file, Charset charSet, boolean autoDetectCharset) throws IOException {
		this(file, 0, charSet, DEFAULT_BUFFER_SIZE, autoDetectCharset);
	}
	
	public RBMappedFile(RandomAccessFile file, int start, Charset charSet, boolean autoDetectCharset) throws IOException {
		this(file, start, charSet, DEFAULT_BUFFER_SIZE, autoDetectCharset);
	}
	
	public RBMappedFile(String fileName) throws IOException {
		this(new RandomAccessFile(fileName, "rw"), 0, DEFAULT_CHARSET, DEFAULT_BUFFER_SIZE, false);
	}
	
	public RBMappedFile(String fileName, boolean autoDetectCharset, int bufferSize) throws IOException {
		this(new RandomAccessFile(fileName, "rw"), 0, DEFAULT_CHARSET, bufferSize, autoDetectCharset);
	}
	
	public RBMappedFile(String fileName, boolean autoDetectCharset) throws IOException {
		this(new RandomAccessFile(fileName, "rw"), 0, DEFAULT_CHARSET, DEFAULT_BUFFER_SIZE, autoDetectCharset);
	}
	
	private RBMappedFile(ByteBuffer bb, CharsetDecoder charsetDecoder, CharBuffer buffer) {
		mbb = bb;
		mbb.reset();
		
		offset = mbb.position();
		
		this.charsetDecoder = charsetDecoder;
		this.buffer = buffer;
	}
	
	@Override
	public Character nextChar() {
		if(buffer.hasRemaining()) return buffer.get();
		
		if(mbb.remaining() == 0) return null;
		
		if(!fillBuffer()) return null;
		
		return buffer.get();	
	}
	
	private boolean fillBuffer() {
		charsetDecoder.reset();
		buffer.clear();
		charsetDecoder.decode(mbb, buffer, false);
		buffer.limit(buffer.position());
		//realBufferSize = buffer.position();
		buffer.rewind();
		
		return buffer.hasRemaining();
	}

	//In multithread environnement we have to manage concurrent access of 'mbb'
	@Override
	public String substring(int start, int end, EscapeCharMan em)  {
		if(start<0) throw new IllegalArgumentException();
		if(end<start) throw new IllegalArgumentException();
		
		ByteBuffer bb = mbb.duplicate();

		CharBuffer resCB = CharBuffer.allocate(end - start);
		
		bb.reset();
		bb.position(bb.position() + start);
		
		charsetDecoder.decode(bb, resCB, true);
	
		em.normalized(resCB);
		
		return resCB.toString();
	}

	@Override
	public int position() {	
		return mbb.position() - offset - buffer.limit() + buffer.position(); 
	}

	@Override
	public void position(int newPosition) {	mbb.position(newPosition + offset); }

	@Override
	public ReadingBuffer clone() {
		return new RBMappedFile(mbb.duplicate(), charsetDecoder.charset().newDecoder(), buffer.duplicate());
	}

	@Override
	public Character currentChar() { 
		if(buffer.limit() == 0 || buffer.position()<=0) return null;
		return buffer.get(buffer.position()-1); 
	}

	@Override
	public char charAt(int index, EscapeCharMan em) {
		if(index<0) throw new IllegalArgumentException();
		
		ByteBuffer bb = mbb.duplicate();
		
		CharBuffer resCB = CharBuffer.allocate(1);
		
		bb.reset();
		bb.position(bb.position() + index);
		
		charsetDecoder.decode(bb, resCB, true);
	
		em.normalized(resCB);
		
		return resCB.get();
	}

	@Override
	public void close() {
		
	}	
	
	public boolean back(String str, EscapeCharMan em) throws ParsingException {
		int p1 = str.length() - 1; int p2 = buffer.position()-1;
		StringBuilder sbStr = new StringBuilder(str);
		
		Character escapable = null;
		while(p1>=0 && p2>=0) {
			char c1 = sbStr.charAt(p1);
			char c2 = buffer.get(p2);
			
			if(c1 == c2) {
				--p1; --p2; 
				escapable = em.escapable(c2) ? c2 : null;
				continue;
			}
			String ec = em.escaped(c1);
			
			if(ec == null) {
				if(escapable == null) return false;
				ec = c2+escapable.toString();
				if(em.translated(ec) == escapable) {
					sbStr.replace(p1+1, p1+2, ec);
					--p2;
					continue;
				}
				
				return false;
				
			}
			
			sbStr.replace(p1, p1+1, ec);
			if(sbStr.charAt(p1+ec.length()-1) != c2) return false;
			p1 += ec.length()-2;
			--p2;
		}
		
		if(p1 < 0) {
			buffer.position(buffer.position()-sbStr.length());
			return true;
		}
		
		//mbb.position(mbb.position() - bufferBackByteSize(sbStr, em)-1);
		
		CharBuffer cb = CharBuffer.wrap(sbStr);
		CharsetEncoder csEncoder = charsetDecoder.charset().newEncoder();
		
		ByteBuffer bb = mbb.duplicate();
		p2 = bb.position() - bufferBackByteSize(sbStr, em)-1;
		
		do {
			cb.position(p1); cb.limit(p1+1);
			ByteBuffer translationBB;
			try {translationBB = csEncoder.encode(cb);} catch(CharacterCodingException e) { throw new ParsingException(e); }
			//System.out.println(translationBB.get(0));
			bb.position(p2 - translationBB.limit());
			bb.limit(p2);
			//System.out.println(bb.get(p2 - translationBB.limit()));
			
			if(translationBB.compareTo(bb) != 0) {
				String ec = em.escaped(cb.get(p1));
				if(ec == null) return false;
				
				try {
					translationBB = csEncoder.encode(CharBuffer.wrap(ec));
				} catch (CharacterCodingException e) {
					throw new ParsingException(e);
				}
				
				bb.position(p2 - translationBB.limit());
				
				if(translationBB.compareTo(bb) != 0) return false;
			}
			
			p2 -= translationBB.limit(); 
			--p1; 
		} while(p1>=0);
		
		buffer.limit(0);
		mbb.position(p2);
		return true;
	}
	
	protected int bufferBackByteSize(StringBuilder sbStr, EscapeCharMan em) throws ParsingException {
		CharsetEncoder csEncoder = charsetDecoder.charset().newEncoder();
		
		ByteBuffer bb;
		try {
			bb = csEncoder.encode(buffer);
		} catch (CharacterCodingException e) {
			throw new ParsingException(e);
		}
		
		return bb.limit();
	}

	@Override
	public void reset() {
		mbb.reset();
		charsetDecoder.reset();
		buffer.clear();
	}
	
	public Charset charset() { return charsetDecoder.charset(); }

	@Override
	public RBMappedFile asMappedFileReadingBuffer() { return this; }

	@Override
	public String string(EscapeCharMan ecm) throws IOException {
		ByteBuffer bb = mbb.duplicate();
		bb.reset();
		
		CharBuffer cb = charsetDecoder.decode(bb);
		
		ecm.normalized(cb);
		
		return cb.toString();
	}
	
	
}
