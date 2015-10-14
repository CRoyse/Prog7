/*******************
 *
 * Author:     Corey Royse
 * Assignment: Program 4
 * Class:      4321, Spring 2015
 * Date:       3/15/2014
 *
 * Represents a generic portion of a SPA Message
 * Provides Serialization/Deserialization
 *******************/

package SPA.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 
 * @author     Corey Royse
 * Assignment: Program 4
 *
 * Represents a generic portion of a SPA Message
 * Provides Serialization/Deserialization
 */
public abstract class SPAMessage {
	//Byte containing the protocol version (0010),QR flag, and Error Code (0-4)
	protected byte verQRErr;
	//false for queries, true for responses
	protected boolean queryResponse;
	//Message ID - randomly generated number that the client uses to map
	//server responses to outstanding requests.
	protected byte msgID;
	
	private final static byte VERSION = 0x20; // Version should be 0010.
	private final static byte NOERR = 0x00; //No error is represented by 000
	private final static byte BADVERERR = 0x01; //Version error is 001
	private final static byte BADMSGLENERR = 0x02; //Bad Message Length is 010
	private final static byte BADMSGERR = 0x03; //Bad Message is 011
	private final static byte SYSERR = 0x04; //System error is 100.
	
	/**
	 * default constructor
	 */
	public SPAMessage(){
		verQRErr = 0x20;
		msgID = 0x1F;
		queryResponse = false;
	}
	
	/**
	 * Constructor for a byte array
	 * @param pkt
	 */
	public SPAMessage(byte[] pkt) throws SPAException{
		if(pkt == null){
			throw new SPAException("Error: Attempted to construct message from null array.");
		}
		if(pkt.length < 2){
			throw new SPAException("Error: Insufficient data to construct SPAMessage");
		}
		setVerQRErr(pkt[0]);
		setMsgID(pkt[1]);
	}
	
	/**
	 * Function that encodes this SPA Message as an array of bytes.
	 * Abstracted for implementation by queries and responses.
	 * @return array of bytes containing the SverQRErrPA message
	 * @throws SPAException
	 * @throws IOException 
	 */
	public byte[] encode() throws SPAException, IOException{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		out.writeByte(verQRErr);
		out.writeByte(msgID);
		out.flush();
		byte[] msg = bytes.toByteArray();
		//return that array.
		return msg;
	}
	
	/**
	 * Function that parses an array of bytes and returns a SPAQuery or SPAResponse
	 * as appropriate
	 * 
	 * @param pkt
	 * @return SPAMessage of correct type, determined by reading the header.
	 * @throws SPAException in event of decoding error
	 */
	static public SPAMessage decode(byte[] pkt) throws SPAException{
		//Verify that pkt is not null
		if(pkt == null){
			throw new SPAException("Error: attempted to decode null packet.");
		}
		//verify that the pkt is long enough to contain at least a full header.
		if(pkt.length < 2){
			throw new SPAException("SPAMessage too short: Incomplete Header");
		}
		//What we want to do is parse the header, which is common to both
		//queries and responses, and determine what sort of message we're dealing with.
		byte vqr = pkt[0]; //the first byte contains our version, QR flag, and Error code.
		byte QRcomp = (byte)(vqr & (byte)0x08); //we AND our vqr byte with 0000 1000
		//This allows us to determine the value of the QR flag.
		//Once we've identified what sort of message we need to construct, we simply
		//call the appropriate constructor using the packet we've been given, handle any errors,
		//and return the message.
		SPAMessage msg = null;
		
		if(QRcomp == 0x00){ //if the result is 0, we construct a query
			msg = new SPAQuery(pkt);
		}
		else if(QRcomp == 0x08){ // if the result is 0000 1000, we construct a response.
			msg = new SPAResponse(pkt);
		}
		
		return msg;
	}
	
	/**
	 * @return verQRErr
	 */
	public byte getVerQRErr(){
		return verQRErr;
	}
	
	/**
	 * @return msgID
	 */
	public byte getMsgID(){
		return msgID;
	}
	
	/**
	 * @param id - new ID.
	 */
	public void setMsgID(byte id){
		msgID = id;
	}
	
	/**
	 * Validates vqe for valid version and error values,
	 * then either sets verQRErr or throws an exception
	 * 
	 * @param vqe
	 */
	public void setVerQRErr(byte vqe) throws SPAException{
		//First we validate the version
		byte verComp = (byte)(VERSION & vqe);
		if(verComp != VERSION){
			throw new SPAException("Error: Bad Version Assignment");
		}
		//Next we determine our QR flag.
		byte QRcomp = (byte)(vqe & 0x08);
		if(QRcomp == 0x00){
			queryResponse = false;
		}
		else if(QRcomp == 0x08){
			queryResponse = true;
		}
		else{
			throw new SPAException("Unexpected QR value");
		}
		//Then we validate the error code.
		byte errComp = (byte)(vqe & 0x07); //this zeroes out the Version byte and QR bit,
		//allowing for easier comparison to our error code constants.
		if(errComp != NOERR && errComp != BADVERERR
			&& errComp != BADMSGLENERR && errComp != BADMSGERR
			&& errComp != SYSERR){
			throw new SPAException("Error: Bad Error Code assignment");
		}
		
		//then we assign.
		verQRErr = vqe;
	}
	
}
