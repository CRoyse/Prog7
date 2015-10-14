package SpRT.protocol.test;
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;



/***********************
*
* Author:     Corey Royse
* Assignment: Program 0
* Class:      4321, Spring 2015
* Date:       1/20/2014
*
* This is a series of JUnit 4 tests designed to ensure succesful development
* of the CookieList class
**********************/

/**
 * @author     Corey Royse
 * Assignment: Program 0
 *
 */
public class CookieListTest {

	/**
	 * Test method for {@link CookieList#hashCode()}.
	 * @throws SpRTException 
	 */
	@Test
	public void testHashCode() throws SpRTException {
		CookieList cookies = new CookieList();
		cookies.add("FName","Tom");
		cookies.add("LName","AlsoTom");
		int expected = 32;
		int actual = cookies.hashCode();
		assertEquals(expected,actual);
	}
	
	/**
	 * Test method for {@link CookieList#hashCode()}.
	 * @throws SpRTException 
	 */
	@Test
	public void testHashCodeConsistency() throws SpRTException {
		CookieList cookies = new CookieList();
		CookieList cookies2 = new CookieList();
		cookies.add("FName","Tom");
		cookies2.add("FName","Tom");
		int hash1 = cookies.hashCode();
		int hash2 = cookies2.hashCode();
		assertEquals(hash1,hash2);
	}

	/**
	 * Test method for {@link CookieList#CookieList()}.
	 */
	@Test
	public void testCookieList() {
		CookieList cookies = new CookieList();
		assertEquals(cookies.isEmpty(), true);
	}

	/**
	 * Test method for {@link CookieList#CookieList(java.io.InputStream)}.
	 * @throws SpRTException 
	 * @throws NullPointerException 
	 * @throws IOException 
	 */
	@Test
	public void testCookieListInputStream() throws NullPointerException, SpRTException, IOException {
		OutputStream out = new FileOutputStream("SampleCookie");
		String cookies = "FName=Tom" + '\r' + '\n' + "LName=Smith" + '\r' + '\n';
		try {
			out.write(cookies.getBytes());
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOException");
		}
		InputStream in = new FileInputStream("SampleCookie");
		CookieList actual;
		actual = new CookieList(in);
		in.close();
		CookieList expectedList = new CookieList();
		expectedList.add("FName","Tom");
		expectedList.add("LName","Smith");
		assertEquals(expectedList.toString(),actual.toString());
	}
	
	/**
	 * Test method for {@link CookieList#CookieList(java.io.InputStream)}.
	 * @throws IOException 
	 * @throws SpRTException 
	 * @throws NullPointerException 
	 */
	@Test(expected = NullPointerException.class)
	public void testCookieListInputStreamNullStream() throws IOException, NullPointerException, SpRTException {
		InputStream in = null;
		@SuppressWarnings("unused") //We suppress this warning since we don't need to 'use'
		                            //the cookielist to test this constructor.
		CookieList cookieList = new CookieList(in);
	}

	/**
	 * Test method for {@link CookieList#CookieList(java.util.Scanner, java.io.PrintStream)}.
	 * NOTE: Since we are not yet implementing this constructor, we are not
	 * yet testing it.*/
	//@Test
	//public void testCookieListScannerPrintStream() {
	//	fail("Not yet implemented");
	//}

	/**
	 * Test method for {@link CookieList#add(String, String)}.
	 * @throws SpRTException 
	 */
	@Test
	public void testAdd() throws SpRTException {
		CookieList cookies = new CookieList();
		cookies.add("fname","tom");
		String expected = "tom";
		Set<String> actualNames = cookies.getNames();
		String actual = "";
		for(String s : actualNames){
			actual = cookies.getValue(s);
		}
		assertEquals(expected,actual);
	}
	
	/**
	 * Test method for {@link CookieList#add(String, String)}.
	 * @throws SpRTException 
	 */
	@Test
	public void testAddMultiple() throws SpRTException {
		CookieList cookies = new CookieList();
		cookies.add("fname","tom");
		cookies.add("fname","nottom");
		String expected = "nottom";
		Set<String> actualNames = cookies.getNames();
		String actual = "";
		for(String s : actualNames){
			actual = cookies.getValue(s);
		}
		assertEquals(expected,actual);
	}
	
	/**
	 * Test method for {@link CookieList#add(String, String)}.
	 * @throws SpRTException 
	 */
	@Test (expected = SpRTException.class)
	public void testAddBad() throws SpRTException{
		CookieList cookies = new CookieList();
		cookies.add("","tom");
	}

	/**
	 * Test method for {@link CookieList#encode(java.io.OutputStream)}.
	 * @throws SpRTException 
	 * @throws NullPointerException 
	 * @throws IOException 
	 */
	@Test
	public void testEncode() throws NullPointerException, SpRTException, IOException {
		OutputStream out = new FileOutputStream("SampleOutput");
		String expectedCookie = "FName=Tom" + '\r' + '\n' + '\r' + '\n';
		CookieList cookies = new CookieList();
		cookies.add("FName","Tom");
		cookies.encode(out);
		out.close();
		String actualCookie = "";
		InputStream in = new FileInputStream("SampleOutput");
		byte[] buffer = new byte[1024];
		in.read(buffer);
		in.close();
		for(byte b : buffer){
			char c = (char)b;
			if(c == '\0'){
				break;
			}
			actualCookie = actualCookie + c;
		}
		assertEquals(expectedCookie,actualCookie);
	}
	
	/**
	 * Test method for {@link CookieList#encode(java.io.OutputStream)}.
	 * @throws SpRTException 
	 * @throws NullPointerException 
	 * @throws IOException 
	 */
	@Test(expected = NullPointerException.class)
	public void testEncodeNullStream() throws NullPointerException, SpRTException, IOException {
			OutputStream out = null;
			CookieList cookies = new CookieList();
			cookies.encode(out);
	}

	/**
	 * Test method for {@link CookieList#equals(Object)}.
	 * @throws SpRTException 
	 */
	@Test
	public void testEqualsObject() throws SpRTException {
		CookieList cookies1 = new CookieList();
		CookieList cookies2 = new CookieList();
		
		cookies1.add("FName", "Tom");
		cookies2.add("FName", "Tom");
		
		boolean expected = true;
		boolean actual = cookies1.equals(cookies2);
		assertEquals(expected,actual);
	}
	
	/**
	 * Test method for {@link CookieList#equals(Object)}.
	 * @throws SpRTException 
	 */
	@Test
	public void testEqualsObjectNotEquals() throws SpRTException {
		CookieList cookies1 = new CookieList();
		CookieList cookies2 = new CookieList();
		
		cookies1.add("FName", "Tom");
		cookies2.add("LName", "NotTom");
		
		boolean expected = false;
		boolean actual = cookies1.equals(cookies2);
		
		assertEquals(expected,actual);
	}
	
	/**
	 * Test method for {@link CookieList#equals(Object)}.
	 * @throws SpRTException 
	 */
	@Test
	public void testEqualsObjectEmpty() throws SpRTException {
		CookieList cookies1 = new CookieList();
		CookieList cookies2 = new CookieList();
		
		cookies1.add("FName", "Tom");
		
		boolean expected = false;
		boolean actual = cookies1.equals(cookies2);
		
		assertEquals(expected,actual);
	}
	
	/**
	 * Test method for {@link CookieList#equals(Object)}.
	 */
	@Test
	public void testEqualsObjectBothEmpty() {
		CookieList cookies1 = new CookieList();
		CookieList cookies2 = new CookieList();
		
		boolean expected = true;
		boolean actual = cookies1.equals(cookies2);
		
		assertEquals(expected,actual);
	}

	/**
	 * Test method for {@link CookieList#getNames()}.
	 * @throws SpRTException 
	 */
	@Test
	public void testGetNames() throws SpRTException {
		CookieList cookies = new CookieList();
		cookies.add("FName", "Tom");
		cookies.add("LName", "AlsoTom");
		Set<String> expected = new HashSet<String>();
		expected.add("FName");
		expected.add("LName");
		Set<String> actual = cookies.getNames();
		assertEquals(expected,actual);
	}

	/**
	 * Test method for {@link CookieList#getValue(String)}.
	 * @throws SpRTException 
	 */
	@Test
	public void testGetValue() throws SpRTException {
		CookieList cookies = new CookieList();
		cookies.add("fname","tom");
		String val = cookies.getValue("fname");
		assertEquals("tom",val);
	}
	
	/**
	 * Test method for {@link CookieList#getValue(String)}.
	 * where the name provided is not in the cookie list.
	 */
	@Test
	public void testGetValueBadName(){
		CookieList cookies = new CookieList();
		String expected = null;
		String actual = cookies.getValue("NoName");
		assertEquals(expected,actual);
	}

	/**
	 * Test method for {@link CookieList#toString()}.
	 * @throws SpRTException 
	 */
	@Test
	public void testToString() throws SpRTException {
		CookieList cookies = new CookieList();
		cookies.add("LName","smith");
		cookies.add("FName","tom");
		String expected = "Cookies=[FName=tom LName=smith]";
		String actual = cookies.toString();
		assertEquals(expected,actual);
	}
	
	/**
	 * Test method for {@link CookieList#toString()}.
	 */
	@Test
	public void testEmptyToString(){
		CookieList cookies = new CookieList();
		String expected = "Cookies=[]";
		String actual = cookies.toString();
		assertEquals(expected,actual);
	}

}
