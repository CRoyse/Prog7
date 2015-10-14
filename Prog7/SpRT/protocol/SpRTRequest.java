/*******************
 *
 * Author:     Corey Royse
 * Assignment: Program 1
 * Class:      4321, Spring 2015
 * Date:       1/26/2014
 *
 * Represents a SpRT request
 * Provides Serialization/Deserialization
 *******************/

package SpRT.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array; //getLength
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author     Corey Royse
 * Assignment: Program 1
 *
 */
public class SpRTRequest extends SpRTMessage{
	
	//Request's command to the server - typically this will be "RUN" as in "RUN (some function)"
	private String command;
	//Parameters of whatever function we wish to run.
	private String[] params;
	//Encoding Standard to be used
	private final static String ENCODING = "US-ASCII";
	//Signature we expect to find at the start of all messages
	private final static String MAGICTOKEN = "SpRT/1.0 ";
	//Signifier that we wish to run a given function
	private final static String COMMANDRUN = "RUN";
	//Delimiter found within cookieList
	private final static String CRLF = "\r\n";
	//Delimiter found at end of CookieList
	private final static String ENDOFLIST = CRLF + CRLF;
	
	/**
	 * Constructs an SpRTRequest manually using passed in values.
	 * 
	 * @param command
	 * @param function
	 * @param params
	 * @param cookies
	 * @throws SpRTException
	 */
	public SpRTRequest(String command,
					   String function,
					   String[] params,
					   CookieList cookies) throws SpRTException{
		super();
		setFunction(function);
		setCommand(command);
		setParams(params);
		setCookies(cookies);
	}
	
	/**
	 * Constructs request using deserialization
	 * 
	 * @param in
	 * @throws SpRTException
	 */
	public SpRTRequest(InputStream in)
				throws SpRTException{
		super();
		this.command     = "";
		String token     = "";
		String []emptyParams = new String[0];
		setParams(emptyParams);
		ArrayList<String> newParams = new ArrayList<>();
		char c = '\n';
		int data = 0;
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
		
		//Now we move on to reading the command
		try {
			data = in.read();
		} catch (IOException e) {
			throw new SpRTException("Error Retrieving Bytes",e);
		}
		c = (char) data;
		while(c != ' '){
			token += c;
			try {
				data = in.read();
			} catch (IOException e) {
				throw new SpRTException("Error Retrieving Bytes",e);
			}
			c = (char) data;
		}
		setCommand(token);
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
		
		//Next, parameters.
		//we may have zero parameters, indicated by a CRLF immediately
		//after our function.
		if(c == '\r'){
			//in this case we expect the next read to complete
			//the CRLF.
			token += c;
			try {
				data = in.read();
			} catch (IOException e) {
				throw new SpRTException("Error Retrieving Bytes",e);
			}
			c = (char) data;
			token += c;
			if(!CRLF.equals(token)){
				throw new SpRTException("Invalid Syntax Error");
			}
		}
		//Otherwise, our function is followed by a space, and
		//then we alternate between parameters and spaces 
		//until we hit the CRLF
		else{
			while(c != '\r'){
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
				newParams.add(token);
				token = "";
			}
			token += c;
			try {
				data = in.read();
			} catch (IOException e) {
				throw new SpRTException("Error Retrieving Bytes",e);
			}
			c = (char) data;
			token += c;
			if(!CRLF.equals(token)){
				throw new SpRTException("Invalid Syntax Error");
			}
			setParams(newParams.toArray(new String[newParams.size()]));
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
	 * Not to be implemented yet
	 * 
	 * @param in
	 * @param out
	 * @throws SpRTException
	 */
	public SpRTRequest(Scanner in,
					   PrintStream out)
				throws SpRTException{
		//NOT TO BE IMPLEMENTED YET
	}
	

	/** ToString
	 * Outputs this Request's status in a human-readable format
	 * @see SpRT.protocol.SpRTMessage#toString()
	 */
	public String toString(){
		//String representation of our request
		String s = "Command: " + command + '\n' + "Function: " + function + '\n';
		s += "Parameters: ";
		
		for(int i = 0; i < Array.getLength(params); i++){
			s += params[i] + " ";
		}
		
		s += "" + '\n';
		s += "Cookies: ";
		s += cookies.toString();
		return s;
	}
	
	/**
	 * Returns command
	 * 
	 * @return
	 */
	public String getCommand(){
		return this.command;
	}
	
	/**
	 * Validates and sets Command
	 * 
	 * @param command
	 * @throws SpRT.protocol.SpRTException
	 */
	public void setCommand(String command) throws SpRTException{
		if(command.length() == 0 || !COMMANDRUN.equals(command) || command == null){
			throw new SpRTException("Attempted to set invalid Command");
		}
		this.command = command;
	}
	
	/**
	 * Returns a string array containing our parameters
	 * @return
	 */
	public String[] getParams(){
		return this.params;
	}
	
	/**
	 * set parameters
	 * 
	 * @param params
	 * @throws SpRT.protocol.SpRTException
	 */
	public void setParams(String[] params)
			throws SpRTException{
		if(params == null){
			throw new NullPointerException("Null Parameter Array passed to SpRT Request");
		}
		int numParams = Array.getLength(params);
		this.params = new String[numParams];
		for(int i = 0; i < numParams; i++){
			if(params[i].length() == 0){
				throw new SpRTException("Empty Parameter passed to SpRTRequest");
			}
			this.params[i] = params[i];
		}
	}

	/**
	 * Encodes this SpRT Message as a string of ASCII-encoded bytes
	 * according to the SpRT Syntax
	 * @see SpRT.protocol.SpRTMessage#encode(java.io.OutputStream)
	 */
	@Override
	public void encode(OutputStream out) throws SpRTException,
			NullPointerException {
		
		//Verify that out is not null
		 if(out == null){
			 throw new NullPointerException("Null output stream");
		 }
		 //validate function
		 if(this.function.isEmpty()){
			 throw new SpRTException("Attempted to Encode Empty Function");
		 }
		 for(int i = 0; i < function.length(); i++){
			 if(!Character.isLetterOrDigit(function.charAt(i))){
				 throw new SpRTException("Attempted to Encode Invalid Function");
			 }
		 }
		 //Construct string
		 String str = MAGICTOKEN + command + " " + function;
		 for(int i = 0; i < Array.getLength(params); i++){
			 str += " " + params[i];
		 }
		 str += "" + '\r' + '\n';
		 
		 //Output with ASCII encoding
		 try {
				out.write(str.getBytes(ENCODING));
				cookies.encode(out);
		 } catch (IOException e) {
				throw new SpRTException("IO Error",e);
		 }
	}
	
}
