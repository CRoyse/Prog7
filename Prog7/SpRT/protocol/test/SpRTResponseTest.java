package SpRT.protocol.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTResponse;

/***********************
*
* Author:     Corey Royse
* Assignment: Program 1
* Class:      4321, Spring 2015
* Date:       1/28/2014
*
* This is a series of JUnit 4 tests designed to ensure successful development
* of the SpRTResponse class
**********************/


public class SpRTResponseTest {

	@Test
	public void testSpRTResponseStringStringStringCookieList() throws SpRTException {
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		@SuppressWarnings("unused")
		SpRTResponse req = new SpRTResponse("OK","F","MSG",cookies);
	}

	/**
	 * @throws SpRTException
	 * @throws IOException
	 */
	@Test
	public void testSpRTResponseInputStream() throws SpRTException, IOException {
		String response = "SpRT/1.0 OK F MSG" + '\r' + '\n'
				           + "n=v" + '\r' + '\n' + '\r' + '\n';
		ByteArrayInputStream in = new ByteArrayInputStream(response.getBytes("US-ASCII"));
		@SuppressWarnings("unused")
		SpRTResponse resp = new SpRTResponse(in);
	}
	
	/**
	 * @throws SpRTException
	 * @throws IOException
	 */
	@Test(expected = NullPointerException.class)
	public void testSpRTResponseNullInputStream() throws SpRTException, IOException {
		ByteArrayInputStream in = null;
		@SuppressWarnings("unused")
		SpRTResponse resp = new SpRTResponse(in);
	}
	
	/**
	 * @throws IOException
	 * @throws SpRTException 
	 */
	@Test(expected = SpRTException.class)
	public void testSpRTResponseBadInputStream() throws IOException, SpRTException {
		String response = "SpRT/1.0 Hi F MSG" + '\r' + '\n'
				           + "n=v" + '\r' + '\n' + '\r' + '\n';
		ByteArrayInputStream in = new ByteArrayInputStream(response.getBytes("US-ASCII"));
		@SuppressWarnings("unused")
		SpRTResponse resp = new SpRTResponse(in);
	}

	//NOTE: We are not implementing this function yet.
	/*@Test
	public void testSpRTResponseScannerPrintStream() {
		fail("Not yet implemented");
	}*/

	/**
	 * @throws SpRTException
	 */
	@Test
	public void testToString() throws SpRTException {
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTResponse resp = new SpRTResponse("OK","Func","Msg",cookies);
		String expected = "Status: OK" + '\n' + "Function: Func" + '\n' + 
						  "Message: Msg" + '\n' + "Cookies: Cookies=[n=v]" + '\r' + '\n';
		String actual = resp.toString();
		assertEquals(expected,actual);
	}

	/**
	 * @throws SpRTException
	 */
	@Test
	public void testGetStatus() throws SpRTException {
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTResponse resp = new SpRTResponse("OK","Func","Msg",cookies);
		String expected = "OK";
		String actual = resp.getStatus();
		assertEquals(expected,actual);
	}

	/**
	 * @throws SpRTException
	 */
	@Test
	public void testSetCommand() throws SpRTException {
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTResponse resp = new SpRTResponse("OK","Func","Msg",cookies);
		String expected = "ERROR";
		resp.setStatus(expected);
		String actual = resp.getStatus();
		assertEquals(expected,actual);
	}

	/**
	 * @throws SpRTException
	 */
	@Test
	public void testGetMessage() throws SpRTException {
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTResponse resp = new SpRTResponse("OK","Func","Msg",cookies);
		String expected = "Msg";
		String actual = resp.getMessage();
		assertEquals(expected,actual);
	}

	/**
	 * @throws SpRTException
	 */
	@Test
	public void testSetMessage() throws SpRTException {
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTResponse resp = new SpRTResponse("OK","Func","Msg",cookies);
		String expected = "NewMsg";
		resp.setMessage(expected);
		String actual = resp.getMessage();
		assertEquals(expected,actual);
	}

	/**
	 * 
	 */
	@Test
	public void testIsPrintableTrue() {
		boolean actual = SpRTResponse.isPrintable("OK");
		assertEquals(true,actual);
	}
	
	/**
	 * 
	 */
	@Test
	public void testIsPrintableFalse() {
		char ch = 0x03;
		String s = "";
		s += ch;
		boolean actual = SpRTResponse.isPrintable(s);
		assertEquals(false,actual);
	}

	/**
	 * 
	 */
	@Test
	public void testIsAsciiPrintableTrue() {
		boolean actual = SpRTResponse.isAsciiPrintable('n');
		assertEquals(true,actual);
	}
	
	/**
	 * 
	 */
	@Test
	public void testIsAsciiPrintableFalse() {
		char ch = 128;
		boolean actual = SpRTResponse.isAsciiPrintable(ch);
		assertEquals(false,actual);
	}

	
	/**
	 * @throws SpRTException 
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	@Test
	public void testEncode() throws SpRTException, UnsupportedEncodingException{
		String expected = "SpRT/1.0 OK F MSG" + '\r' + '\n' +
				           "n=v" + '\r' + '\n' + '\r' + '\n';
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTResponse resp = new SpRTResponse("OK","F","MSG",cookies);
		resp.encode(out);
		assertEquals(expected,out.toString("US-ASCII"));
	}
	
	/**
	 * @throws SpRTException 
	 * 
	 */
	@Test(expected = NullPointerException.class)
	public void testEncodeNullOutput() throws SpRTException{
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTResponse resp = new SpRTResponse("OK","F","MSG",cookies);
		resp.encode(null);
	}
}
