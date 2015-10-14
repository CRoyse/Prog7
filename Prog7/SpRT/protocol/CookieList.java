package SpRT.protocol;
/*******************
 *
 * Author:     Corey Royse
 * Assignment: Program 0
 * Class:      4321, Spring 2015
 * Date:       1/16/2014
 *
 * This is a CookieList that serializes and deserializes a cookie list according
 * to the grammer given to us with the assignment
 *******************/

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;


/**
 * List of cookies - that is, name/value pairs
 * @Author:    Corey Royse
 * Assignment: Program 0
 */
public class CookieList implements Serializable{
 

  /**
	* Serial UID
	*/
 private static final long serialVersionUID = -2604908935069187353L;
 
 //Map in which we store a keys and values - our cookies
 private Map<String,String> cookies;
 //String representing the sort of encoding we expect to use.
 private static final String ENCODING = "US-ASCII";
 //String representing our delimiter at the end of cookies
 private static final String CRLF = "\r\n";
 //String representing the end of a cookielist.
 private static final String ENDOFLIST = CRLF + CRLF;

 /**
  * Creates a new CookieList with an empty list of name/value pairs
  * 
  */ 
 public CookieList(){
	 cookies = new HashMap<>();
 }
 

 /**
  * Creates a new CookieList by decoding from an inputstream
  * The input stream will be comprised of a stream of ASCII characters
  * Of these, cookie tokens may only be comprised of alphanumeric characters
  * 
  * @param  in stream to decode from
  * @throws SpRTException in event of invalid input
  * @throws NullPointerException in event of null input
  */ 
 public CookieList(InputStream in)
		 throws SpRTException, NullPointerException{
	 if(in == null){
		 throw new NullPointerException("Null Inputstream Passed to CookieList");
	 }
	try {
		InputStreamReader reader = new InputStreamReader(in, ENCODING);
		//Initialize cookie set
		cookies = new HashMap<>();
		//Retrieve names and values
		String name = "";
		String token = "";
		Boolean listEnd = false;
		Boolean listNotEmpty = false;
		boolean keyOrValue = false; //false = key, true = value
		char c = '\n';
		int data;
		
		try {
			data = reader.read();
			while(data != -1 && !listEnd){
				c = (char)data;
				data = reader.read();
				//If character is outside of the SpRT Protocol, immediately
				//throw exception
				if(!Character.isLetterOrDigit(c) && 
						!(c == '=' || c == '\r' || c == '\n')){
					throw new SpRTException("CookieList contains invalid character");
				}
				//If character is alphanumeric, make sure it's where
				//we expect it to be.
				else if(Character.isLetterOrDigit(c)){
					if(token.length() > 0 && 
							token.charAt(token.length()-1) == '\r'){
						throw new SpRTException("Syntax error");
					}
					if(CRLF.equals(token)){
						token = "";
					}
					token += c;
				}
				//If character is '=', we expect to have just finished reading
				//a name.
				else if(c == '='){
					if(!keyOrValue){
						name = token;
						token = "";
						keyOrValue = true;
					}
					else{
						throw new SpRTException("Syntax Error");
					}
				}
				//We expect a '\r' to mark the end of a value
				//or to come as pair of back-to-back CRLFs marking
				//the end of a cookielist. 
				//(Or as part of a single CRLF in an empty list)
				else if(c == '\r'){
					if(!keyOrValue){
						if(!CRLF.equals(token) && token.length() != 0){
							throw new SpRTException("Syntax Error");
						}
						else{
							token += c;
						}
					}
					if(keyOrValue){
						//Since we have just finished reading a value,
						//we should have a name-value pair to add to
						//our list.
						//Note that the add function does its own validation.
						add(name,token);
						if(!listNotEmpty){
							listNotEmpty = true;
						}
						token = "" + c;
						keyOrValue = false;
					}
				}
				//We only expect '\n' after a '\r'
				//If the character in question marks the end of a 
				//pair of CRLFs, we are done parsing this cookielist.
				else if(c == '\n'){
					if(keyOrValue){
						throw new SpRTException("Syntax Error");
					}
					else{
						if(CRLF.equals(token)){
							throw new SpRTException("Syntax Error");
						}
						else if(ENDOFLIST.equals(token + c)){
							listEnd = true;
							token += c;
						}
						else if(token.length() == 1 
								&& token.charAt(0) == '\r'){
							token += c;
						}
						else{
							throw new SpRTException("Syntax Error");
						}
					}
				}
			}
			if(!listEnd && !(isEmpty() && CRLF.equals(token)) && !token.isEmpty()){
				throw new SpRTException("Ran out of data before end of list");
			}
		} catch (IOException e) {
			throw new SpRTException("I/O Error while decoding CookieList");
		}
	} catch (UnsupportedEncodingException e) {
		throw new SpRTException("Unsupported Encoding Error while decoding CookieList", e);
	}	
 }
 

 /**
  * Creates a new CookieList by decoding from the console(user)
  * NOTE: Not implemented yet.
  * 
  * @param in
  * @param out
  */
public CookieList(Scanner in, PrintStream out){
  /*NOT TO BE IMPLEMENTED YET*/
 }

 
 /**
  * Simple check that returns true if the CookieList is empty.
  * 
  * @return true if empty
  */
public boolean isEmpty(){
	 boolean ret = false;
	 if(cookies.isEmpty()){
		 ret = true;
	 }
	 return ret;
 }

 
 /**
  * Adds the name/value pair to our cookie list
  * If the name already exists, the new value replaces the old value
  * 
  * @param name key
  * @param value value
  * @throws SpRTException in event of invalid cookie
  */
 public void add(String name, String value) throws SpRTException{
	 //Check to see if name exists
	 //if name exists, replace value
	if(name.equals(null)){
		throw new SpRTException("Cookie Key must have nonzero length");
	}
	 if(cookies.size() > 0){
		 Iterator<Entry<String, String>> it = cookies.entrySet().iterator();
		 while(it.hasNext()){
			 Map.Entry<String, String> c = (Entry<String, String>)it.next();
			 if(c.getKey().equals(name)){
				if(value.length() > 0){
				   c.setValue(value);
				}
				else{
					throw new SpRTException("Invalid Value: Length 0");
				}
			 }
		 }
	 }
	 //Otherwise, validate and insert new cookie.
	 if(name.length() > 0 && value.length() > 0){
		 for(int i = 0; i < name.length(); i++){
			 if(!Character.isLetterOrDigit(name.charAt(i))){
				 throw new SpRTException("Invalid name token");
			 }
		 }
		 for(int i = 0; i < value.length(); i++){
			 if(!Character.isLetterOrDigit(value.charAt(i))){
				 throw new SpRTException("Invalid name token");
			 }
		 }
		 cookies.put(name, value);
	 }
	 else{
		 throw new SpRTException("Invalid Token: Name or Value length 0");
	 }
 }

 
 /**
  * 
  * Encodes the name-value list
  * As with the decode, see the specifications for details on the encoding scheme
  * Note that the serialized data must be in alphabetical order.
  * @param out outputStream to be encoded to
  * @throws SpRTException in event of invalid data being encoded
  * @throws NullPointerException in event of null data being encoded
  * @throws UnsupportedEncodingException in event of unsupported encoding standard.
  */
public void encode(java.io.OutputStream out) 
		 throws SpRTException, NullPointerException, UnsupportedEncodingException{
	 //Verify that out is not null
	 if(out == null){
		 throw new NullPointerException("Null output stream");
	 }
	 else{
		 //write name, '=', value, CRLF as a string of bytes for all cookies.
		 //Contains all the names of our cookies
		 List<String> nameList = new ArrayList<>();
		 nameList.addAll(getNames());
		 //Name
		 String n;
		 //Value
		 String v;
		 //String to encode
		 String str;
		 Collections.sort(nameList, String.CASE_INSENSITIVE_ORDER);
		 for(int i = 0; i < nameList.size(); i++){
			 n = nameList.get(i);
			 v = getValue(n);
			 str = n + "=" + v + '\r' + '\n';
			 try {
					out.write(str.getBytes(ENCODING));
			} catch (IOException e) {
					throw new SpRTException("IO Error",e);
			}
		 }
		 str = "" + '\r' + '\n';
		 try {
			out.write(str.getBytes(ENCODING));
		} catch (IOException e) {
			throw new UnsupportedEncodingException("Improper Encoding Standard");
		}
	 }
  }



 /**
  * Return true if and only if the contents of our name/value list
  * is identical to that of the object given
  * 
  * @param  Object obj	object to compare to
  * @return boolean		true if equal
  */
 public boolean equals(Object obj){
  if(obj instanceof CookieList){ 
	  //CookieList we are comparing to.
	  CookieList otherList = (CookieList)obj;
	  //Cookie Names from other list.
	  Set<String> otherCookies = otherList.getNames();
	  
	  //If our sizes are not equal, we know immediately that the lists are not equal.
	  if(cookies.size() == otherCookies.size()){
		  //Iterator used to traverse our list
		  Iterator<Entry<String, String>> it1 = cookies.entrySet().iterator();
		  //Iterator used to traverse the other list
		  Iterator<String> it2 = otherCookies.iterator();
		  while(it1.hasNext()){
			  Map.Entry<String,String> c1 = (Entry<String, String>)it1.next();
			  String c2 = it2.next();
			  if(!c1.getKey().equals(c2)
					  || !(getValue(c1.getKey()).equals(otherList.getValue(c2)))){
				  return false;
			  }
		  }
	  }
	  else{
		  return false;
	  }
  	}
  return true;
  }

 
 
 /**
  * Simple getter for our set of names.
  *
  * @return Set<String> names
  */
 public Set<String> getNames(){
  //Set of cookie Names.
  Set<String> names = new HashSet<>();
  //Iterator used to traverse our cookieList.
  Iterator<Entry<String, String>> it = cookies.entrySet().iterator();
  while(it.hasNext()){
	  Map.Entry<String,String> cookie = (Entry<String, String>)it.next();
	  names.add((String)cookie.getKey());
  }
  return names; 
 }
 

 /**
  * Checks to see if the given name is represented in the CookieList 
  * if so, it returns the value,
  * Otherwise, returns null
  * 
  * @param name
  * @return value associated with the name, or null.
  */
 public String getValue(String name){
    //Find name in set
	//Iterator used to traverse list.
    Iterator<Entry<String, String>> it = cookies.entrySet().iterator();
    while(it.hasNext()){
	  Map.Entry<String,String> cookie = (Entry<String,String>)it.next();
	  if(cookie.getKey().equals(name)){
		  //When we find the cookie with the right name, we immediately return the associated value
		  return (String)cookie.getValue();
	  }
    }
    //if name is not found return null
    return null;
 }



 /** 
  * Returns the integer produced by applying
  * a hash function to the contents of
  * the cookie list.
  * HASH FUNCTION: Number of cookies - 1 * 32
  * 
  * @see java.lang.Object#hashCode()
  */
public int hashCode(){
  //Hash value
  int hash = (cookies.size()-1)*32;
  return hash;
 }



 /** 
  * Returns string representation of CookieList
  * format: [name1=value1 name2=value2]
  * NOTE: output must be in alphabetical order by name, as with encode
  * 
  * @see java.lang.Object#toString()
  */
public String toString(){
  //open brackets
  String cookieString = "Cookies=[";
  //retrieve data, store in this list
  List<String> nameList = new ArrayList<>();
  nameList.addAll(getNames());
  //Sort names alphabetically
  Collections.sort(nameList, String.CASE_INSENSITIVE_ORDER);
  //Print name, equals sign, value, iterate over all names.
  for(int i = 0; i < nameList.size(); i++){
	  String s = nameList.get(i);
	  cookieString = cookieString + s + "=" + getValue(s);
	  //We don't want to add a space between the last value and the closing bracket.
	  if(i < nameList.size()-1){
		  cookieString = cookieString + " ";
	  }
  }
  //close brackets, return
  cookieString = cookieString + "]";
  return cookieString;
 }
}
