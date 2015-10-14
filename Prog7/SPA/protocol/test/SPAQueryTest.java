package SPA.protocol.test;

import static org.junit.Assert.*;

import org.junit.Test;

import SPA.protocol.SPAException;
import SPA.protocol.SPAMessage;
import SPA.protocol.SPAQuery;

/***********************
*
* Author:     Corey Royse
* Assignment: Program 4
* Class:      4321, Spring 2015
* Date:       3/16/2014
*
* This is a series of JUnit 4 tests designed to ensure successful development
* of the SPAQuery class
**********************/

/**
 * 
 * @author Corey Royse
 * Assignment 4
 * 
 */
public class SPAQueryTest {
	
	/**
	 * tests default, empty constructor
	 */
	@SuppressWarnings("unused") //we're just testing that our constructor runs to completion
	@Test
	public void testDefaultConstructor(){
		SPAQuery q = new SPAQuery();
	}
	
	/**
	 * Tests our constructor that takes an array of bytes
	 * @throws SPAException
	 */
	@Test
	public void testByteArrayConstructor() throws SPAException{
		byte[] expected = new byte[6];
		expected[0] = 0x20; //ver:0010 QR: 0 Err: 000
		expected[1] = 0x03; //MsgID = 0011
		expected[2] = 0x03; //BuisnessNameLength = 0011 = 3 bytes
		expected[3] = 'b';
		expected[4] = 'i';
		expected[5] = 'z';
		
		SPAQuery q = new SPAQuery(expected);
		
		byte expectedLength = 0x03;
		byte actualLength = q.getBusinessNameLength();
		byte[] expectedName = {'b','i','z'};
		byte[] actualName = q.getBusinessName();
		
		assertEquals(expectedLength,actualLength);
		for(int i = 0; i < 0x03; i++){
			assertEquals(expectedName[i],actualName[i]);
		}
	}
	
	/**
	 * Expects the byte constructor to throw an exception upon being passed
	 * a header specifying a response rather than a query
	 * @throws SPAException 
	 * 
	 */
	@SuppressWarnings("unused")
	@Test(expected = SPAException.class)
	public void testBadTypeByteArrayConstructor() throws SPAException{
		byte[] expected = new byte[6];
		expected[0] = 0x28; //ver:0010 QR: 1 Err: 000
		expected[1] = 0x03; //MsgID = 0011
		expected[2] = 0x03; //BuisnessNameLength = 0011 = 3 bytes
		expected[3] = 'b';
		expected[4] = 'i';
		expected[5] = 'z';
		
		SPAQuery q = new SPAQuery(expected);
	}
	
	/**
	 * Tests an attempt to decode a null byte array
	 * @throws SPAException
	 */
	@SuppressWarnings("unused")
	@Test(expected = SPAException.class)
	public void testNullConstructor() throws SPAException{
		byte[] expected = null;
		
		SPAQuery actualQ = new SPAQuery(expected);
	}
	
	/**
	 * Tests a standard run of the Query's encoding functionality
	 * @throws SPAException
	 */
	@Test
	public void testEncode() throws SPAException{
		byte[] expected = new byte[6];
		expected[0] = 0x20; //ver:0010 QR: 0 Err: 000
		expected[1] = 0x03; //MsgID = 0011
		expected[2] = 0x03; //BuisnessNameLength = 0011 = 3 bytes
		expected[3] = 'b';
		expected[4] = 'i';
		expected[5] = 'z';
		
		SPAQuery q = new SPAQuery(expected);
		byte[] actual = q.encode();
		
		assertEquals(expected.length, actual.length);
		for(int i = 0; i < expected.length; i++){
			assertEquals(expected[i], actual[i]);
		}
	}
	
	/**
	 * Tests a standard run through of the SPAMessage Decode functionality,
	 * expecting a SPAQuery
	 * @throws SPAException
	 */
	@Test
	public void testDecode() throws SPAException{
		byte[] expected = new byte[6];
		expected[0] = 0x20; //ver:0010 QR: 0 Err: 000
		expected[1] = 0x03; //MsgID = 0011
		expected[2] = 0x03; //BuisnessNameLength = 0011 = 3 bytes
		expected[3] = 'b';
		expected[4] = 'i';
		expected[5] = 'z';
		SPAQuery expectedQ = new SPAQuery(expected);
		
		SPAQuery actualQ = (SPAQuery) SPAMessage.decode(expected);
		
		byte expectedVQR = expectedQ.getVerQRErr();
		byte expectedMSGID = expectedQ.getMsgID();
		byte expectedBizNameLen = expectedQ.getBusinessNameLength();
		byte[] expectedBizName = expectedQ.getBusinessName();
		
		byte actualVQR = actualQ.getVerQRErr();
		byte actualMSGID = actualQ.getMsgID();
		byte actualBizNameLen = actualQ.getBusinessNameLength();
		byte[] actualBizName = actualQ.getBusinessName();
		
		assertEquals(expectedVQR,actualVQR);
		assertEquals(expectedMSGID,actualMSGID);
		assertEquals(expectedBizNameLen,actualBizNameLen);
		for(int i = 0; i < expectedBizName.length; i++){
			assertEquals(expectedBizName[i],actualBizName[i]);
		}
	}
	
	/**
	 * Tests an attempt to decode a null byte array
	 * @throws SPAException
	 */
	@SuppressWarnings("unused")
	@Test(expected = SPAException.class)
	public void testDecodeNull() throws SPAException{
		byte[] expected = null;
		
		SPAQuery actualQ = (SPAQuery) SPAMessage.decode(expected);
	}
	
	/**
	 * Tests a valid attempt to set a buisness name and length
	 * @throws SPAException
	 */
	@Test
	public void testSetBizLen() throws SPAException{
		SPAQuery q = new SPAQuery();
		byte[] name = {'b','i','z'};
		q.setBusinessName((byte)3, name);
		
		assertEquals(name,q.getBusinessName());
	}
	
	/**
	 * Tests an invalid attempt to set a buisness name and length
	 * @throws SPAException
	 */
	@Test(expected = SPAException.class)
	public void testSetBadBizLen() throws SPAException{
		SPAQuery q = new SPAQuery();
		byte[] name = {'b','i','z'};
		q.setBusinessName((byte)7, name);
		
		assertEquals(name,q.getBusinessName());
	}
}
