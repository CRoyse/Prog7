package SpRT.protocol.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;

/***********************
*
* Author:     Corey Royse
* Assignment: Program 1
* Class:      4321, Spring 2015
* Date:       1/28/2014
*
* This is a series of JUnit 4 tests designed to ensure successful development
* of the SpRTRequest class
**********************/

public class SpRTRequestTest {

	/**
	 * @throws SpRTException
	 */
	@SuppressWarnings("unused")
	@Test
	public void testSpRTRequestStringStringStringArrayCookieList() throws SpRTException {
		String[] params = {"P1","P2"};
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTRequest req = new SpRTRequest("RUN","F",params,cookies);
	}

	/**
	 * @throws SpRTException 
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	@SuppressWarnings("unused")
	@Test
	public void testSpRTRequestInputStream() throws SpRTException, UnsupportedEncodingException {
		String request = "SpRT/1.0 RUN F P1 P2" + '\r' + '\n'
		           + "n=v" + '\r' + '\n' + '\r' + '\n';
        ByteArrayInputStream in = new ByteArrayInputStream(request.getBytes("US-ASCII"));
        SpRTRequest req = new SpRTRequest(in);
	}
	
	/**
	 * @throws SpRTException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	@Test(expected = NullPointerException.class)
	public void testSpRTRequestNullInputStream() throws SpRTException, IOException {
		ByteArrayInputStream in = null;
		SpRTRequest req = new SpRTRequest(in);
	}
	
	/**
	 * @throws IOException
	 * @throws SpRTException 
	 */
	@SuppressWarnings("unused")
	@Test(expected = SpRTException.class)
	public void testSpRTRequestBadInputStream() throws IOException, SpRTException {
		String request = "SpRT/1.0 DONTRUN F P1 P2" + '\r' + '\n'
		                  + "n=v" + '\r' + '\n' + '\r' + '\n';
		ByteArrayInputStream in = new ByteArrayInputStream(request.getBytes("US-ASCII"));
		SpRTRequest req = new SpRTRequest(in);
	}

	/*@Test
	public void testSpRTRequestScannerPrintStream() {
		//This function is not yet implemented.
		fail("Not yet implemented");
	}*/

	/**
	 * @throws SpRTException
	 */
	@Test
	public void testToString() throws SpRTException {
		String expected = "Command: RUN" + '\n' + "Function: F" 
							+ '\n' + "Parameters: P1 P2 " + '\n'
							+ "Cookies: Cookies=[n=v]";
		String[] params = {"P1","P2"};
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTRequest req = new SpRTRequest("RUN","F",params,cookies);
		String actual = req.toString();
		assertEquals(expected,actual);
	}

	/**
	 * @throws SpRTException
	 */
	@Test
	public void testGetCommand() throws SpRTException {
		String expected = "RUN";
		String[] params = {"P1","P2"};
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTRequest req = new SpRTRequest("RUN","F",params,cookies);
		String actual = req.getCommand();
		assertEquals(expected,actual);
	}

	/**
	 * @throws SpRTException
	 */
	@Test
	public void testSetCommand() throws SpRTException {
		String expected = "NOTRUN";
		String[] params = {"P1","P2"};
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTRequest req = new SpRTRequest("RUN","F",params,cookies);
		req.setCommand(expected);
		String actual = req.getCommand();
		assertEquals(expected,actual);
	}

	/**
	 * @throws SpRTException
	 */
	@Test
	public void testGetParams() throws SpRTException {
		String expected = "P1P2";
		String[] params = {"P1","P2"};
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTRequest req = new SpRTRequest("RUN","F",params,cookies);
		String p[] = req.getParams();
		String actual = p[0] + p[1];
		assertEquals(expected,actual);
	}

	/**
	 * @throws SpRTException
	 */
	@Test
	public void testSetParams() throws SpRTException {
		String expected = "P3P4";
		String[] params = {"P1","P2"};
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		SpRTRequest req = new SpRTRequest("RUN","F",params,cookies);
		params[0] = "P3";
		params[1] = "P4";
		req.setParams(params);
		String p[] = req.getParams();
		String actual = p[0] + p[1];
		assertEquals(expected,actual);
	}
	
	/**
	 * @throws SpRTException 
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	@Test
	public void testEncode() throws SpRTException, UnsupportedEncodingException{
		String expected = "SpRT/1.0 RUN F P1 P2" + '\r' + '\n' +
				           "n=v" + '\r' + '\n' + '\r' + '\n';
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		String[] params = {"P1","P2"};
		SpRTRequest req = new SpRTRequest("RUN","F",params, cookies);
		req.encode(out);
		assertEquals(expected,out.toString("US-ASCII"));
	}
	
	/**
	 * @throws SpRTException 
	 * @throws UnsupportedEncodingException 
	 * 
	 */
	@Test(expected = NullPointerException.class)
	public void testEncodeNullOutput() throws SpRTException, UnsupportedEncodingException{
		CookieList cookies = new CookieList();
		cookies.add("n","v");
		String[] params = {"P1","P2"};
		SpRTRequest req = new SpRTRequest("RUN","F",params, cookies);
		req.encode(null);
	}
	
}
