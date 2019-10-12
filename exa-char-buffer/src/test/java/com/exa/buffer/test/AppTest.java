package com.exa.buffer.test;

import java.io.File;
import java.io.IOException;


import com.exa.buffer.CharReader;
import com.exa.buffer.CharReader.ClientBuffer;
import com.exa.buffer.RBMappedFile;
import com.exa.buffer.RBRAM;
import com.exa.buffer.ReadingBuffer;
import com.exa.chars.EscapeCharMan;
import com.exa.lexing.CommentWord;
import com.exa.lexing.LexingRules;
import com.exa.lexing.ParsingException;
import com.exa.utils.ManagedException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AppTest extends TestCase {

	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }
    
    public void testReadingBuffer0() throws IOException {
    	ReadingBuffer rb = new RBRAM("test\\n\r\nwith escape");
    	
    	assertTrue("test".equals(rb.substring(0, 4, EscapeCharMan.STANDARD)));
    	assertTrue("test\n".equals(rb.substring(0, 6, EscapeCharMan.STANDARD)));
    	assertTrue("test\n\r".equals(rb.substring(0, 7, EscapeCharMan.STANDARD)));
    	assertTrue("test\n\r\n".equals(rb.substring(0, 8, EscapeCharMan.STANDARD)));
    	assertTrue("test\n\r\nw".equals(rb.substring(0, 9, EscapeCharMan.STANDARD)));
    	assertTrue("test\n\r\nwith ".equals(rb.substring(0, 13, EscapeCharMan.STANDARD)));
    	
    	File file = new File(".");
    	System.out.println(file.getAbsolutePath());
    	
    	rb.close();
    	
    	rb = new RBMappedFile("./src/test/java/com/exa/buffer/test/test", true);
    	
    	assertTrue("test".equals(rb.substring(0, 4)));
    	
    	rb.close();
    	
    	rb = new RBMappedFile("./src/test/java/com/exa/buffer/test/test-with-escape", true);
    	
    	assertTrue("test".equals(rb.substring(0, 4, EscapeCharMan.STANDARD)));
    	assertTrue("test\n".equals(rb.substring(0, 6, EscapeCharMan.STANDARD)));
    	assertTrue("test\n\r".equals(rb.substring(0, 7, EscapeCharMan.STANDARD)));
    	assertTrue("test\n\r\n".equals(rb.substring(0, 8, EscapeCharMan.STANDARD)));
    	assertTrue("test\n\r\nw".equals(rb.substring(0, 9, EscapeCharMan.STANDARD)));
    	assertTrue("test\n\r\nwith ".equals(rb.substring(0, 13, EscapeCharMan.STANDARD)));
    	
    	rb.close();
    }
    
    public void testCharReaderBuffer() throws ManagedException, IOException {
    	CharReader charReader = new CharReader("test0 test1");
    	
    	ClientBuffer bf = charReader.listen();
    	charReader.nextChar();
    	bf.release();
    	//System.out.println(bf);
    	assertTrue("t".equals(bf.toString()));
    	
    	charReader.close();
    	
    	charReader = new CharReader("test0 test1");
    	charReader.nextChar();
    	bf = charReader.listen();
    	charReader.nextChar();
    	bf.release();
    	//System.out.println(bf);
    	assertTrue("e".equals(bf.toString()));
    	
    	charReader.close();
    	
    	charReader = new CharReader("test0 test1");
    	bf = charReader.listen();
    	bf.release();
    	//System.out.println(bf);
    	assertTrue("".equals(bf.toString()));
    	
    	charReader.close();
    	
    	charReader = new CharReader("test0 test1");
    	charReader.nextChar();
    	bf = charReader.listen();
    	charReader.nextChar();
    	charReader.nextChar();
    	bf.release();
    	//System.out.println(bf);
    	assertTrue("es".equals(bf.toString()));
    	
    	charReader.close();
    	
    	charReader = new CharReader("test0 test1");
    	charReader.nextChar();
    	bf = charReader.listen();
    	while(charReader.nextChar() != null);
    	bf.release();
    	//System.out.println(bf);
    	assertTrue("est0 test1".equals(bf.toString()));
    	
    	charReader.close();
    	
    	charReader = new CharReader("test0 test1");
    	bf = charReader.listen();
    	while(charReader.nextChar() != null);
    	bf.release();
    	//System.out.println(bf);
    	assertTrue("test0 test1".equals(bf.toString()));
    	
    	charReader.close();
    	
    	charReader = new CharReader("test0 test1");
    	bf = charReader.listen();
    	while(charReader.nextChar() != null);
    	charReader.back("1");
    	bf.release();
    	//System.out.println(bf);
    	assertTrue("test0 test".equals(bf.toString()));
    	
    	charReader.close();
    	
    	charReader = new CharReader("test0 test1");
    	bf = charReader.listen();
    	while(charReader.nextChar() != null);
    	bf.release();
    	//System.out.println(bf);
    	charReader.back("1");
    	assertTrue("test0 test1".equals(bf.toString()));
    	
    	charReader = new CharReader("test0 test1");
    	bf = charReader.listen();
    	ClientBuffer bf1 = charReader.listen();
    	while(charReader.nextChar() != null);
    	bf.release();
    	bf1.release();
    	//System.out.println(bf);	System.out.println(bf1);
    	assertTrue("test0 test1".equals(bf.toString()));
    	assertTrue("test0 test1".equals(bf1.toString()));
    	
    	charReader.close();
    	
    	charReader = new CharReader("test0 test1");
    	bf = charReader.listen();
    	charReader.nextChar();
    	bf1 = charReader.listen();
    	while(charReader.nextChar() != null);
    	bf.release();
    	bf1.release();
    	//System.out.println(bf);	System.out.println(bf1);
    	assertTrue("test0 test1".equals(bf.toString()));
    	assertTrue("est0 test1".equals(bf1.toString()));
    	
    	charReader.close();
    	
    	charReader = new CharReader("test0 test1");
    	bf = charReader.listen();
    	while(charReader.nextChar() != null);
    	bf.release();
    	bf1 = charReader.listen();
    	bf1.release();
    	//System.out.println(bf);	System.out.println(bf1);
    	assertTrue("test0 test1".equals(bf.toString()));
    	assertTrue("".equals(bf1.toString()));
    	
    	charReader = new CharReader("test0 test1");
    	bf = charReader.listen();
    	bf.release();
    	bf1 = charReader.listen();
    	while(charReader.nextChar() != null);
    	bf1.release();

    	assertTrue("".equals(bf.toString()));
    	assertTrue("test0 test1".equals(bf1.toString()));
    	
    	charReader.close();
    	
    	charReader = new CharReader("test0 test1");
    	bf = charReader.listen();
    	charReader.nextChar();
    	assertTrue("t".equals(bf.toString()));
    	bf.rewind();
    	assertTrue("".equals(bf.toString()));
    	bf.release();
    	
    	charReader.close();
    	
    	charReader = new CharReader("test0 test1");
    	bf = charReader.listen();
    	while(charReader.nextChar() != null);
    	assertTrue("test0 test1".equals(bf.toString()));
    	bf.rewind();
    	assertTrue("".equals(bf.toString()));
    	bf.release();
    	
    }
    
    public void testRBBack() throws IOException, ManagedException {
    	ReadingBuffer rb = new RBRAM("test\\n\r\nwith escape");
    	rb.nextChar();
    	assertTrue(rb.back("t", EscapeCharMan.STANDARD));
    	
    	rb.reset();
    	rb.nextChar(); rb.nextChar();
    	assertTrue(rb.back("te", EscapeCharMan.STANDARD));
    	
    	rb.reset();
    	rb.nextChar(); rb.nextChar(); rb.nextChar(); rb.nextChar(); rb.nextChar(); rb.nextChar(); 
    	assertTrue(rb.back("test\n", EscapeCharMan.STANDARD));
    	
    	rb = new RBMappedFile("./src/test/java/com/exa/buffer/test/test-with-escape", true);
    	rb.nextChar();
    	assertTrue(rb.back("t", EscapeCharMan.STANDARD));
    	
    	rb.reset();
    	rb.nextChar(); rb.nextChar();
    	assertTrue(rb.back("te", EscapeCharMan.STANDARD));
    	
    	rb = new RBMappedFile("./src/test/java/com/exa/buffer/test/test-with-escape", true, 5);
    	rb.nextChar(); rb.nextChar(); rb.nextChar(); rb.nextChar(); rb.nextChar(); rb.nextChar(); 
    	assertTrue(rb.back("test\n", EscapeCharMan.STANDARD));
    	
    	rb.nextChar();
    	assertTrue("t".equals(rb.currentChar().toString()));
    }
    
    public void testClose() throws IOException {
    	CharReader cr = CharReader.forFile("./src/test/java/com/exa/buffer/test/test", false);
    	
    	cr.close();
    }
    
    public void testComment() throws IOException, ParsingException {
    	CharReader cr = CharReader.forFile("./src/test/java/com/exa/buffer/test/test-comment", false);
    	
    	LexingRules lr = new LexingRules(" \n");
    	
    	CommentWord cmtWrd = new CommentWord("/*", lr, sb -> "*/".equals(sb.substring(sb.length()-2)) );
    	
    	lr.addWordSeparator(cmtWrd);
    	
    	String str = lr.nextString(cr);
    	
    	assertTrue("ab".equals(str));
    	
    	str = lr.nextString(cr);
    	
    	assertTrue("fg".equals(str));
    	
    	cr.close();
    }
    
    public void testComment2() throws IOException, ParsingException {
    	CharReader cr = CharReader.forFile("./src/test/java/com/exa/buffer/test/test-comment2", false);
    	
    	LexingRules lr = new LexingRules(" \n");
    	
    	CommentWord cmtWrd = new CommentWord("/*", lr, sb -> "*/".equals(sb.substring(sb.length()-2)) );
    	
    	lr.addWordSeparator(cmtWrd);
    	
    	String str = lr.nextString(cr);
    	
    	assertTrue("de".equals(str));
    	
    	cr.close();
    }
    
    public void testComment3() throws IOException, ParsingException {
    	CharReader cr = CharReader.forFile("./src/test/java/com/exa/buffer/test/test-comment3", false);
    	
    	LexingRules lr = new LexingRules(" \n");
    	
    	CommentWord cmtWrd = new CommentWord("/*", lr, sb -> "*/".equals(sb.substring(sb.length()-2)) );
    	
    	lr.addWordSeparator(cmtWrd);
    	
    	lr.addWordSeparator(new CommentWord("//", lr, sb -> {
			if(sb.length() == 2) return true;
			char ch = sb.charAt(sb.length()-1);
			
			return ch == '\n' || ch == '\r';
		}));
    	
    	String str = lr.nextString(cr);
    	
    	assertTrue("ab".equals(str));
    	
    	str = lr.nextString(cr);
    	
    	assertTrue("cd".equals(str));
    	
    	str = lr.nextString(cr);
    	
    	assertTrue("ef".equals(str));
    	
    	cr.close();
    }
    
    
    /*public static void testCharset() throws IOException {
    	byte[] b = {(byte)0xFE, (byte)0xFF};
    	
    	assertTrue("FEFF".equals(ByteCharDecoder.bytesToHex(b)));
    	
    	RandomAccessFile file = new RandomAccessFile("/exa-char-buffer/src/test/java/com/exa/buffer/test/test", "rw");
    	
    	ByteBuffer mbb = file.getChannel().map(MapMode.READ_WRITE, 0, file.length());
    	
    	System.out.println(ByteCharDecoder.charsetOf(mbb));
    	
    	file.close();
    	
    	file = new RandomAccessFile("C:/Users/valery.nzi/Desktop/test-encodage.txt", "rw");
    	
    	mbb = file.getChannel().map(MapMode.READ_ONLY, 0, file.length());
    	
    	System.out.println(ByteCharDecoder.charsetOf(mbb));
    	
    	file.close();
    	
    	//System.out.println(ByteCharDecoder.bytesToHex(b));
    }*/
}
