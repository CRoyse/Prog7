package SPA.protocol;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * 
 * @author Corey Royse
 * Assignment: Program 4
 * 
 * Class that represents a SPAQuery, a type of the Abstract SPAMessage that
 * implements its own encoding.
 */
public class SPAQuery extends SPAMessage{
	
	private byte businessNameLength; //Unsigned byte representing the length
	                                 //in bytes of the requesting business' name.
	private byte[] businessName;     //array of bytes representing the ASCII encoded characters
	
	/**
	 * default constructor
	 */
	public SPAQuery(){
		super();
		businessNameLength = 0;
		businessName = new byte[0];
	}
	
	/**
	 * Constructor that takes a byte array.
	 * Called by SPAMessage decode function.
	 * @param pkt byte array containing our query
	 */
	public SPAQuery(byte[] pkt) throws SPAException{
		super(pkt);
		//Validate that we are constructing a query and
		//have enough data to do so.
		if(queryResponse){
			throw new SPAException("Error: Attempted to construct Query from Response");
		}
		if(pkt.length < 3){
			throw new SPAException("Error: packet too short to construct full query");
		}
		int bizLen = pkt[2] & 0xFF;
		byte[] bizName = null;
		
		if(bizLen > 0){
		  bizName = Arrays.copyOfRange(pkt, 3, pkt.length);
		}
		else{
			bizName = new byte[0];
		}
		//set
		setBusinessName(pkt[2],bizName);
	}
	
	/** 
	 * @see SPA.protocol.SPAMessage#encode()
	 */
	@Override
	public byte[] encode() throws SPAException {
		//simply store all our data in a single byte array in the proper order
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] header;
		out.write(businessNameLength);
		try {
			header = super.encode();
			out.write(businessName);
		} catch (IOException e) {
			throw new SPAException(e.getMessage());
		}
		
		byte[] msg = out.toByteArray();
		
		byte[] ret = new byte[header.length + msg.length];
		System.arraycopy(header,0,ret,0,header.length);
		System.arraycopy(msg,0,ret,header.length,msg.length);
		// return that array.
		return ret;
	}
	
	/**
	 * @return businessNameLength
	 */
	public byte getBusinessNameLength(){
		return businessNameLength;
	}
	
	/**
	 * @return businessName
	 */
	public byte[] getBusinessName(){
		return businessName;
	}
	
	/**
	 * Validates and sets a business name of the specified length and content.
	 * 
	 * @param nameLen
	 * @param bizName
	 * @throws SPAException
	 */
	public void setBusinessName(byte nameLen, byte[] bizName) throws SPAException{
		//convert to signed int
		int bizLength = nameLen & 0xFF;
		if(bizLength != bizName.length){
			throw new SPAException("Specified Length does not match actual");
		}
		else{
			businessNameLength = nameLen;
			businessName = bizName;
		}
	}

}
