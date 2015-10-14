package SPA.protocol.test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

import SPA.protocol.SPAException;
import SPA.protocol.SPAMessage;
import SPA.protocol.SPAResponse;

public class SPAResponseTest {
	
	/**
	 * tests default, empty constructor
	 */
	@SuppressWarnings("unused")
	@Test
	public void defaultConstructorTest() {
		SPAResponse r = new SPAResponse();
	}
	
	/**
	 * Tests an attempt to construct a valid SPAResponse
	 * 
	 * @throws IOException
	 * @throws SPAException
	 */
	@Test
	public void byteArrayConstructorTest() throws IOException, SPAException {
		byte vqr = 0x28; //ver:0010 QR: 1 Err: 000
		byte msgID = 0x03; //MsgID = 0011
		int timeStamp = 0x0; //timeStamp = 0
		byte appCount = 0x02; //appCount = 2
		short[] appUseCount = {3,4}; //App 1 used 3 times, app 2 used 4 times.
		byte[] appNameLength = {3,5}; //app name lengths
		byte[][] appNames = {{'a','p','p'},{'m','y','a','p','p'}}; //app and myapp.
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		out.writeByte(vqr);
		out.writeByte(msgID);
		out.writeInt(timeStamp);
		out.writeByte(appCount);
		out.writeShort(appUseCount[0]);
		out.writeByte(appNameLength[0]);
		out.write(appNames[0]);
		out.writeShort(appUseCount[1]);
		out.writeByte(appNameLength[1]);
		out.write(appNames[1]);
		out.flush();
		
		byte[] expected = bytes.toByteArray();
		
		SPAResponse actual = new SPAResponse(expected);
		
		assertEquals(vqr,actual.getVerQRErr());
		assertEquals(msgID,actual.getMsgID());
		assertEquals(appCount,actual.getAppCount());
	}
	
	/**
	 * Tests an attempt to construct a SPAResponse from an array enumerating a Query.
	 * @throws IOException
	 * @throws SPAException
	 */
	@Test(expected = SPAException.class)
	public void badByteArrayConstructorTest() throws IOException, SPAException {
		byte vqr = 0x20; //ver:0010 QR: 0 Err: 000
		byte msgID = 0x03; //MsgID = 0011
		int timeStamp = 0; //timeStamp = 0
		byte appCount = 0x02; //appCount = 2
		byte[] appUseCount = {3,4}; //App 1 used 3 times, app 2 used 4 times.
		byte[] appNameLength = {3,5}; //app name lengths
		byte[][] appNames = {{'a','p','p'},{'m','y','a','p','p'}}; //app and myapp.
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(vqr);
		out.write(msgID);
		out.write(timeStamp);
		out.write(appCount);
		out.write(appUseCount[0]);
		out.write(appNameLength[0]);
		out.write(appNames[0]);
		out.write(appUseCount[1]);
		out.write(appNameLength[1]);
		out.write(appNames[1]);
		
		byte[] pkt = out.toByteArray();
		
		@SuppressWarnings("unused")
		SPAResponse actual = new SPAResponse(pkt);
	}
	
	/**
	 * Tests an attempt to build a SPAResponse from insufficient data
	 * 
	 * @throws IOException
	 * @throws SPAException
	 */
	@Test(expected = SPAException.class)
	public void shortByteArrayConstructorTest() throws IOException, SPAException {
		byte vqr = 0x20; //ver:0010 QR: 0 Err: 000
		byte msgID = 0x03; //MsgID = 0011
		int timeStamp = 0; //timeStamp = 0
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		out.write(vqr);
		out.write(msgID);
		out.write(timeStamp);
		out.flush();
		byte[] pkt = bytes.toByteArray();
		
		@SuppressWarnings("unused")
		SPAResponse actual = new SPAResponse(pkt);
	}
	
	/**
	 * Tests an attempt to encode a SPAResponse
	 * 
	 * @throws IOException
	 * @throws SPAException
	 */
	@Test
	public void encodeTest() throws IOException, SPAException {
		byte vqr = 0x28; //ver:0010 QR: 1 Err: 000
		byte msgID = 0x03; //MsgID = 0011
		int timeStamp = 0x0; //timeStamp = 0
		byte appCount = 0x02; //appCount = 2
		short[] appUseCount = {3,4}; //App 1 used 3 times, app 2 used 4 times.
		byte[] appNameLength = {3,5}; //app name lengths
		byte[][] appNames = {{'a','p','p'},{'m','y','a','p','p'}}; //app and myapp.
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		out.writeByte(vqr);
		out.writeByte(msgID);
		out.writeInt(timeStamp);
		out.writeByte(appCount);
		out.writeShort(appUseCount[0]);
		out.writeByte(appNameLength[0]);
		out.write(appNames[0]);
		out.writeShort(appUseCount[1]);
		out.writeByte(appNameLength[1]);
		out.write(appNames[1]);
		out.flush();
		
		byte[] expected = bytes.toByteArray();
		
		SPAResponse r = new SPAResponse(expected);
		
		byte[] actual = r.encode();
		
		
		assertEquals(expected.length, actual.length);
		
		for(int i = 0; i < expected.length; i++){
			assertEquals(expected[i],actual[i]);
		}
		
	}
	
	/**
	 * Tests the decode functionality of our SPAResponse
	 * 
	 * @throws IOException
	 * @throws SPAException
	 */
	@Test
	public void decodeTest() throws IOException, SPAException {
		byte vqr = 0x28; //ver:0010 QR: 1 Err: 000
		byte msgID = 0x03; //MsgID = 0011
		int timeStamp = 0x0; //timeStamp = 0
		byte appCount = 0x02; //appCount = 2
		short[] appUseCount = {3,4}; //App 1 used 3 times, app 2 used 4 times.
		byte[] appNameLength = {3,5}; //app name lengths
		byte[][] appNames = {{'a','p','p'},{'m','y','a','p','p'}}; //app and myapp.
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		out.writeByte(vqr);
		out.writeByte(msgID);
		out.writeInt(timeStamp);
		out.writeByte(appCount);
		out.writeShort(appUseCount[0]);
		out.writeByte(appNameLength[0]);
		out.writeByte(appNames[0][0]);
		out.writeByte(appNames[0][1]);
		out.writeByte(appNames[0][2]);
		out.writeShort(appUseCount[1]);
		out.writeByte(appNameLength[1]);
		out.writeByte(appNames[1][0]);
		out.writeByte(appNames[1][1]);
		out.writeByte(appNames[1][2]);
		out.writeByte(appNames[1][3]);
		out.writeByte(appNames[1][4]);
		out.flush();
		
		byte[] expected = bytes.toByteArray();
		
		SPAResponse actualResp = (SPAResponse) SPAMessage.decode(expected);
		
		byte[] actual = actualResp.encode();
		
		assertEquals(expected.length,actual.length);
		for(int i = 0; i < expected.length; i++){
			assertEquals(expected[i],actual[i]);
		}
	}
	
	/**
	 * Tests an attempt to decode a null array
	 * @throws SPAException
	 */
	@Test (expected = SPAException.class)
	public void nullDecodeTest() throws SPAException {
		byte[] badPkt = null;
		
		@SuppressWarnings("unused")
		SPAMessage r = SPAMessage.decode(badPkt);
	}

}
