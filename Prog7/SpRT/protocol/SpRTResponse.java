/*******************
 *
 * Author:     Corey Royse
 * Assignment: Program 1
 * Class:      4321, Spring 2015
 * Date:       1/26/2014
 *
 * Represents a SpRT response
 * Provides Serialization/Deserialization
 *******************/

package SpRT.protocol;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * @author     Corey Royse
 * Assignment: Program 1
 *
 */
public class SpRTResponse extends SpRTMessage{
	
	//Encoding Standard to be used
	private static final String ENCODING = "US-ASCII";
	//Signature we expect to find at the start of all messages
	private final static String MAGICTOKEN = "SpRT/1.0 ";
	//Status of Response under expected parameters
	private final static String STATUSOK = "OK";
	//Status of Response sent in error state
	private final static String STATUSERROR = "ERROR";
	//Delimiter found within cookieList
	private static final String CRLF = "\r\n";
	//Delimiter found at end of CookieList
	private static final String ENDOFLIST = CRLF+CRLF;
	//Status of the Message: Either OK (Function is operating as expected)
	//or Error (Function has encountered some problem)
	private String status;
	//Message: Plain text message sent to client.
	private String message;
	
	/**
	 * Constructs response based on passed-in data
	 * 
	 * @param status
	 * @param function
	 * @param message
	 * @param cookies
	 * @throws SpRT.protocol.SpRTException
	 */
	public SpRTResponse(String     status,
					    String     function,
					    String     message,
					    CookieList cookies) throws SpRTException{
		super();
		setFunction(function);
		setStatus(status);
		setMessage(message);
		setCookies(cookies);
	}
	
	/**
	 * Constructs response using deserialization of a sequence of ASCII bytes
	 * 
	 * @param in
	 * @throws SpRT.protocol.SpRTException
	 */
	public SpRTResponse(InputStream in) throws SpRTException{
		//initialize
		super();
		this.status  = "";
		this.message = "";
		//the token we are currently reading.
		String token = "";
		//Byte to be read
		int data;
		//Char representation of read byte
		char c = '\n';
		//check for null inputstream
		if(in == null){
			throw new NullPointerException("Null input stream");
		}
		//We read in nine characters expecting to find our
		//SpRT signature
		for(int i = 0; i < 9; i++){
			try {
				data = in.read();
			} catch (IOException e) {
				throw new SpRTException("Error Retrieving Bytes",e);
			}
			if(data == -1){
				throw new SpRTException("Incomplete SpRT Message");
			}
			c = (char) data;
			token += c;
		}
		if(!MAGICTOKEN.equals(token)){
			throw new SpRTException("Syntax Violation: Expected SpRT signature");
		}
		token = "";
		
		//Next, the status
		try {
			data = in.read();
		} catch (IOException e) {
			throw new SpRTException("Error Retrieving Bytes",e);
		}
		c = (char) data;
		while(c != ' ' && c != '\r'){
			token += c;
			try {
				data = in.read();
			} catch (IOException e) {
				throw new SpRTException("Error Retrieving Bytes",e);
			}
			c = (char) data;
		}
		setStatus(token);
		token = "";
		
		//Next, the function
		try {
			data = in.read();
		} catch (IOException e) {
			throw new SpRTException("Error Retrieving Bytes",e);
		}
		c = (char) data;
		while(c != ' ' && c != '\r'){
			token += c;
			try {
				data = in.read();
			} catch (IOException e) {
				throw new SpRTException("Error Retrieving Bytes",e);
			}
			c = (char) data;
		}
		setFunction(token);
		token = "";
		
		//Next, the message
		try {
			data = in.read();
		} catch (IOException e) {
			throw new SpRTException("Error Retrieving Bytes",e);
		}
		c = (char) data;
		while(c != '\r'){
			token += c;
			try {
				data = in.read();
			} catch (IOException e) {
				throw new SpRTException("Error Retrieving Bytes",e);
			}
			c = (char) data;
		}
		setMessage(token);
		token = "";
		//Verify CRLF
		try {
			data = in.read();
		} catch (IOException e) {
			throw new SpRTException("Error Retrieving Bytes",e);
		}
		c = (char) data;
		if(c != '\n'){
			throw new SpRTException("Syntax Error");
		}
		
		//Next we determine if there is a 
		//cookie list attached to this message
		token = "";
		try {
			data = in.read();
		} catch (IOException e) {
			throw new SpRTException("Error Retrieving Bytes",e);
		}
		c = (char) data;
		if(c == '\r'){
			token += c;
			try {
				data = in.read();
			} catch (IOException e) {
				throw new SpRTException("Error Retrieving Bytes",e);
			}
			c = (char) data;
			token += c;
			if(!CRLF.equals(token)){
				throw new SpRTException("Syntax Error");
			}
		}
		else if(Character.isLetterOrDigit(c)){
			boolean done = false;
			while(data != -1 && !done){
				token += c;
				if(token.contains(ENDOFLIST)){
					done = true;
				}
				else{
					try {
						data = in.read();
					} catch (IOException e) {
						throw new SpRTException("Error Retrieving Bytes",e);
					}
					c = (char) data;
				}
			}
			
			ByteArrayInputStream bais;
			try {
				bais = new ByteArrayInputStream(token.getBytes(ENCODING));
			} catch (UnsupportedEncodingException e) {
				throw new SpRTException("Error decoding cookies", e);
			}
			cookies = new CookieList(bais);
		}
		else{
			throw new SpRTException("Syntax Error");
		}
	}
	
	
	/**
	 * Constructs SpRT Response based on user input
	 * NOT TO BE IMPLEMENTED YET
	 * 
	 * @param in
	 * @param out
	 * @throws SpRT.protocol.SpRTException
	 */
	public SpRTResponse(Scanner in, PrintStream out)
				throws SpRT.protocol.SpRTException{
		//NOT TO BE IMPLEMENTED YET
	}
	
	/**
	 * Returns human-readable expression of this message.
	 * @see SpRT.protocol.SpRTMessage#toString()
	 */
	public String toString(){
		String str = "Status: " + status + '\n' + "Function: " + function +
				      '\n' + "Message: " + message + '\n' + "Cookies: ";
		str += cookies.toString();
		str += "" + '\r' + '\n';
		return str;
	}
	
	/**
	 * Returns the current status
	 * @return
	 */
	public String getStatus(){
		return this.status;
	}
	
	/**
	 * Validates and sets status according to SpRT Syntax
	 * @param status
	 * @throws SpRT.protocol.SpRTException
	 */
	public void setStatus(String status) 
			throws SpRT.protocol.SpRTException{
		if(status.equals(STATUSOK) || status.equals(STATUSERROR)){
			this.status = status;
		}
		else{
			throw new SpRTException("Invalid Status");
		}
	}
	
	/**
	 * Returns the current message
	 * @return
	 */
	public String getMessage(){
		return this.message;
	}
	
	/**
	 * Validates and sets message according to SpRT Syntax
	 * @param message
	 * @throws SpRT.protocol.SpRTException
	 */
	public void setMessage(String message) 
			throws SpRT.protocol.SpRTException{
		if(isPrintable(message)){
			this.message = message;
		}
		else{
			throw new SpRTException("Message is not Printable");
		}
	}
	
	/**
	 * Determines if String is printable.
	 * NOTE: Not to be implemented yet.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isPrintable(String str){
		//Check each character of the given string
		for(int i = 0; i < str.length(); i++){
			//if any character is not printable, return false
			if(!isAsciiPrintable(str.charAt(i))){
				return false;
			}
		}
		//Otherwise, return true
		return true;
	}
	
	/**
	 * Determines if the given character is within the range of
	 * printable ASCII characters.
	 * @param ch
	 * @return
	 */
	public static boolean isAsciiPrintable(char ch){
		//return whether or not ch is within the range of ASCII printable characters
		return (ch >= 32 && ch < 127);
	}

	/**
	 * Encodes this SpRT Message as a string of ASCII-encoded bytes
	 * according to the SpRT Syntax
	 * @see SpRT.protocol.SpRTMessage#encode(java.io.OutputStream)
	 */
	@Override
	public void encode(OutputStream out) throws SpRTException, NullPointerException {
		//Verify that out is not null
		 if(out == null){
			 throw new NullPointerException("Null output stream");
		 }
		 //Validate Function
		 if(this.function.isEmpty()){
			 throw new SpRTException("Attempted to Encode Empty Function");
		 }
		 for(int i = 0; i < this.function.length(); i++){
			 if(!Character.isLetterOrDigit(this.function.charAt(i))){
				 throw new SpRTException("Attempted to Encode Invalid Function");
			 }
		 }
		 //Construct string
		 String str = MAGICTOKEN + status + " " 
		              + function + " " + message + '\r' + '\n';
		 //output with ASCII encoding
		 try {
				out.write(str.getBytes(ENCODING));
				cookies.encode(out);
		 } catch (IOException e) {
				throw new SpRTException("IO Error",e);
		 }
	}
	
}
