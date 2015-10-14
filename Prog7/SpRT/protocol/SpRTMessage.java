/*******************
 *
 * Author:     Corey Royse
 * Assignment: Program 1
 * Class:      4321, Spring 2015
 * Date:       1/26/2014
 *
 * Represents a generic portion of a SpRT message
 * Provides Serialization/Deserialization
 * Must be abstract, may contain concrete functions
 *******************/

package SpRT.protocol;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author     Corey Royse
 * Assignment: Program 1
 *
 */
public abstract class SpRTMessage{
	
	//CookieList in which we store all our cookies
	protected CookieList cookies;
	//Function specified by the message
	protected String function;
	
	/**
	 * default constructor
	 */
	public SpRTMessage(){
		cookies = new CookieList();
		function = "";
	}
	
	/**
	 * Constructs generic SpRT Message according to passed in params
	 * 
	 * @param function
	 * @param cookies
	 * @throws SpRT.protocol.SpRTException
	 * @throws NullPointerException
	 */
	public SpRTMessage(String function, CookieList cookies) throws SpRTException,NullPointerException{
		setFunction(function);
		setCookies(cookies);
	}
	

	/**
	 * Decodes ASCII-encoded file to create new Message
	 * 
	 * @param in inputStream to decode from
	 * @throws SpRT.protocol.SpRTException
	 * @throws NullPointerException
	 */
	public SpRTMessage(InputStream in) throws SpRTException, NullPointerException{
		this.cookies = new CookieList();
	}
	

	/**
	 * Constructs message according to user input
	 * NOT TO BE IMPLEMENTED YET
	 * 
	 * @param in
	 * @param out
	 * @throws SpRT.protocol.SpRTException
	 * @throws NullPointerException
	 */
	public SpRTMessage(Scanner in, PrintStream out) throws SpRTException, NullPointerException{
		//NOT TO BE IMPLEMENTED YET
	}
	
	
	/**
	 * Encodes the given SpRT message
	 * Abstracted so that Requests and Responses may encode themselves.
	 * 
	 * @param out OutputStream to decode to
	 * @throws SpRT.protocol.SpRTException
	 * @throws NullPointerException
	 */
	abstract public void encode(OutputStream out) throws SpRTException, NullPointerException;
	
	
	/**
	 * Abstract toString function
	 * messages may print themselves in human-readable format
	 * @see java.lang.Object#toString()
	 */
	abstract public String toString();
	
	/**
	 * Simple getter for function
	 * @return function
	 */
	public String getFunction(){
		return function;
	}
	
	/**
	 * Validates and sets function according to SpRT syntax
	 * @param function to be validated
	 * @throws SpRTException in event of invalid function
	 */
	public void setFunction(String function) throws SpRTException{
		//Verify valid function
		if(function == null){
			throw new NullPointerException("Null Function passed to SpRT Message");
		}
		if(function.isEmpty()){
			throw new SpRTException("Empty Function passed to SpRT Message");
		}
		for(int i = 0; i < function.length(); i++){
			if(!Character.isLetterOrDigit(function.charAt(i))){
				throw new SpRTException("Invalid Function passed to SpRT Message");
			}
		}
		this.function = function;
	}
	
	/**
	 * Simple getter for cookielist
	 * @return cookies
	 */
	public CookieList getCookieList(){
		return cookies;
	}
	

	/**
	 * Validates and sets cookielist.
	 * @param cookies
	 */
	public void setCookies(CookieList cookies){
		if(cookies == null){
			throw new NullPointerException("Null CookieList passed to SpRT Message");
		}
		this.cookies = cookies;
	}
	
}
