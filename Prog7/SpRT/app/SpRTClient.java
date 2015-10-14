/*******************
 *
 * Author:     Corey Royse
 * Assignment: Program 2
 * Class:      4321, Spring 2015
 * Date:       2/4/2014
 *
 * This is a client that sends SpRTRequests to a given server port and handles SpRTResponses
 *******************/

package SpRT.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;

/**
 * Client that allows a user to send and receive SpRTMessages
 * to a specified server port.
 * @Author:    Corey Royse
 * Assignment: Program 2
 */
public class SpRTClient {
	
	//String we expect to see in a Response signifying an Error State
	private final static String STATUSERROR = "ERROR";
	//String we expect to see in a Response from a server that has completed its function
	private final static String NULLFUNC = "NULL";
	//String signifying our intent to execute a server function
	private final static String COMMANDRUN = "RUN";
	
	/**
	 * Serializes the given map of InetAddresses and Cookielists to a given file.
	 * 
	 * @param f  file to be serialized to
	 * @param cookieMap  map to be serialized
	 * @throws IOException  in the event of any error opening or writing to file
	 */
	public static void saveCookies(File f, Map<InetAddress,CookieList> cookieMap) throws IOException{
		//OutputStream we use to write our map to our file.
		FileOutputStream outFile;
		outFile = new FileOutputStream(f);
		//ObjectOutputStream used to quickly serialize our map.
		ObjectOutputStream mapOut = new ObjectOutputStream(outFile);
		mapOut.writeObject(cookieMap);
		mapOut.close();
		outFile.close();
	}
	
	/**
	 * Saves the given Inet/cookielist map to the given file, then closes the given socket 
	 * and buffered Reader.
	 * 
	 * @param f  file to be serialized to
	 * @param cookieMap  map to be serialized
	 * @param s  socket to be closed
	 * @param br  BufferedReader to be closed
	 */
	public static void terminator(File f, Map<InetAddress,CookieList> cookieMap, 
			Socket s, BufferedReader br){
		try{
			saveCookies(f,cookieMap);
			s.close();
			br.close();
		}catch(IOException  e){
			System.err.println("Shutdown Problem:" + e.getMessage());
			System.exit(5);
		}
	}
	
	
	/**
	 * Searches a given file for a map of InetAddresses and cookielists.
	 * If the file exists, deserializes and returns the map.
	 * If the file does not exists, returns an empty hashmap.
	 * In the event of an error, we terminate.
	 * 
	 * @param f file in which we expect to find our cookies
	 * @param fName name of file F
	 * @return HashMap as specified above
	 */
	@SuppressWarnings("unchecked") //Suppressing a warning regarding an unchecked conversion
								   //from the Object we read from our file and the CookieMap
								   //If something other than our map is in there, it will be
								   //made obvious as try to validate cookieLists and such from it.
	public static Map<InetAddress,CookieList> retrieveMap(File f,String fName){
		//cookieMap to be returned
		Map<InetAddress,CookieList> cookieMap = new HashMap<>();
		if(f.exists() && !f.isDirectory()){
			//if it exists, retrieve map of IP addresses and associated CookieLists
			try {
				//FileStream to read file
				FileInputStream fileStream = new FileInputStream(fName);
				//ObjectInputStream to convert from binary to our HashMap
				ObjectInputStream mapReader = new ObjectInputStream(fileStream); 
				cookieMap = (HashMap<InetAddress,CookieList>)mapReader.readObject();
				mapReader.close();
				fileStream.close();
			} catch (IOException | ClassNotFoundException e) {
				//If we cannot read the Cookie File, print error message and terminate.
				System.err.println("Unable to start: Error reading file " + e.getMessage());
				System.exit(3);
			}
		}
		return cookieMap;
	}
	
	
	/**
	 * Retrieves a function from the keyboard, validates, reprompts if necessary.
	 * Once valid function is retrieved, uses empty set of parameters and
	 * given CookieList to build and return a new SpRTRequest
	 * 
	 * @param cookies CookieList to attach to our Request
	 * @param br Buffered Reader used to parse user input.
	 * @return SpRTRequest
	 */
	public static SpRTRequest buildRequest(CookieList cookies, BufferedReader br){
		//Empty String array to be used in the construction of our request
		String[] params = new String[0];
		//Prompt user for function
		//Used in validating the user's function - we do this ourselves
		//because it is simpler than letting the Request constructer do it and
		//throw an exception
		boolean validFunc = false;
		//Request to be returned.
		SpRTRequest req = null;
		while(!validFunc){
			System.out.print("Function> ");
			//Function
			String function = "";
			try {
				function = br.readLine();
			} catch (IOException e3) {
				System.err.println("Unable to Start: Error parsing function " + e3.getMessage());
				System.exit(2);
			}
			try {
				req = new SpRTRequest(COMMANDRUN,function,params,cookies);
				validFunc = true;
			} catch (SpRTException e2) {
				System.err.println("Bad user input: " + e2.getMessage());
				validFunc = false;
			}
		}
		
		return req;
	}
	

	/**
	 * Retrieves a set of zero-to-many strings, seperated by spaces, from
	 * the keyboard an returns them as an array of strings
	 * 
	 * @param br BufferedReader used to parse Parameters
	 * @return newParams the array of String parameters
	 * @throws IOException in the event of an error reading from the keyboard
	 */
	public static String[] getParams(BufferedReader br) throws IOException{
		//String in which we store the user's input.
		String pString = "";
		pString = br.readLine();
		//String array in which we store the discrete values, seperated by whitespace,
		//entered by the user - this is what we return.
		String[] newParams = pString.split(" ");
		return newParams;
	}
	
	
	/**
	 * Searches the given map - if it contains the given InetAddress, it returns
	 * the associated cookielist. Otherwise, it creates an empty cookielist,
	 * stores it in the map in association with the given address, and
	 * returns the empty cookielist
	 * 
	 * @param cookieMap Map to be searched
	 * @param remoteIP Key we to be searched for
	 * @return cookies List of cookies associated with this InetAddress
	 */
	public static CookieList getCookies(Map<InetAddress,CookieList> cookieMap,
										InetAddress remoteIP){
		//CoookieList to be returned
		CookieList cookies = null;
		if(cookieMap.containsKey(remoteIP)){
			cookies = cookieMap.get(remoteIP);
		}
		else{
			cookies = new CookieList();
			cookieMap.put(remoteIP, cookies);
		}
		return cookies;
	}
	
	/**
	 * Sends an SpRTRequest over the given output stream, handles any errors that occur.
	 * 
	 * @param req
	 * @param out
	 * @param file
	 * @param cookieMap
	 * @param br
	 * @param socket
	 */
	public static void sendRequest(SpRTRequest req, OutputStream out, File file,
			Map<InetAddress,CookieList> cookieMap, BufferedReader br, Socket socket){
		try {
			req.encode(out);
		} catch (SpRTException e1) {
			System.err.println("Communication problem: Error Sending Request:" + e1.getMessage());
			terminator(file,cookieMap,socket,br);
			System.exit(3);
		}
	}
	
	/**
	 * Retrieves and returns an SpRTResponse from the given inputstream,
	 * handles any errors that occur.
	 * 
	 * @param in
	 * @param file
	 * @param cookieMap
	 * @param br
	 * @param socket
	 * @return
	 */
	public static SpRTResponse getResponse(InputStream in, File file,
			Map<InetAddress,CookieList> cookieMap, BufferedReader br, Socket socket){
		SpRTResponse resp = null;
		try {
			resp = new SpRTResponse(in);
		} catch (SpRTException e1) {
			System.err.println("Communication problem: Error Receiving Response: " + e1.getMessage());
			terminator(file,cookieMap,socket,br);
			System.exit(3);
		}
		return resp;
	}
	
	/**
	 * Reports a communication error using the given message and terminates the client.
	 * 
	 * @param message
	 * @param file
	 * @param cookieMap
	 * @param br
	 * @param socket
	 */
	public static void commError(String message, File file,
			Map<InetAddress,CookieList> cookieMap, BufferedReader br, Socket socket){
		System.err.println(message);
		terminator(file,cookieMap,socket,br);
		System.exit(3);
	}
	
	/**
	 * Connects to a specified server using cookies found in a specified file,
	 * prompts the user for a function to run on the server, then runs that
	 * function until completion or until the server returns an error.
	 * 
	 * @param args server name, port number, name of cookie file.
	 */
	public static void main(String [] args){
		//Verify proper number of command line arguments, retrieve.
		if(args.length != 3){
			System.err.println("Unable to start: expects Server Identity, Server Port, Cookie File");
			System.exit(1);
		}
		//Name of the server specified by the server
		String serverName = args[0];
		//Port to be connected to.
		int servPort = (args.length == 3) ? Integer.parseInt(args[1]) : 7;
		//Name of the file in which we want to store and from which we retrieve a set of cookies
		//associated with each InetAddress we connect to.
		String cookieFile = args[2];
		
		//Try to open Cookie File - if it exists, retrieve map of IP addresses
		File file = new File(cookieFile);
		//Map of visited InetAddresses and their associated cookies
		Map<InetAddress,CookieList> cookieMap = retrieveMap(file,cookieFile);
		
		//Create socket, retrieve IO streams
		Socket socket = null;
		InputStream in = null;
		OutputStream out = null;
		try {
			socket = new Socket(serverName, servPort);
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (IOException e2) {
			System.err.println("Unable to start: Error reading file " + e2.getMessage());
			try{
				saveCookies(file,cookieMap);
				socket.close();
			}catch(IOException  e){
				System.err.println("Shutdown Problem:" + e.getMessage());
				System.exit(5);
			}
			System.exit(3);
		}
		//InetAddress associated with the server
		InetAddress remoteIP = socket.getInetAddress();
		
		//If the IP address we're talking to exists in our map, we can use
		//existing cookies. Otherwise, we need to create them.
		//CookieList associated with remoteIP
		CookieList cookies = getCookies(cookieMap,remoteIP);
		
		//Construct SpRTRequest
		//BufferedReader used to parse user input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		//Request we send to the server
		SpRTRequest req = buildRequest(cookies, br);
		
		//Flag used to determine whether we need to construct and send another request
		boolean done = false;
		while(!done){
		    //Send
			sendRequest(req, out, file, cookieMap, br, socket);
			//Retrieve SpRTResponse
			SpRTResponse resp = getResponse(in, file, cookieMap, br, socket);
			
			//Validate Response
			//If response is invalid, print error message and terminate
			if(STATUSERROR.equals(resp.getStatus())){
				System.err.println("Error: " + resp.getMessage());
				try {
					req.setFunction(resp.getFunction());
					//New parameters to associate with new request - retrieved from keyboard
					String[] newParams = getParams(br);
					req.setParams(newParams);
				} catch (SpRTException | IOException e1) {
					String message = "Communication problem: Error Constructing Request: "
						+ e1.getMessage();
					commError(message, file, cookieMap, br, socket);
				}
			}
			else{
				//Check Response Function
				//If Response Function is "NULL" then close connection and terminate
				if(NULLFUNC.equals(resp.getFunction())){
					done = true;
					System.out.println(resp.getMessage());
				}
				else{
					System.out.print(resp.getMessage() + ">");
					try {
						req.setFunction(resp.getFunction());
						String[] newParams = getParams(br);
						req.setParams(newParams);
					} catch (SpRTException | IOException e1) {
						String message = "Communication problem: Error Constructing Request: " 
					    + e1.getMessage();
						commError(message, file, cookieMap, br, socket);
					}
				}
				CookieList RespCookies = resp.getCookieList(); //Cookies retrieved from the Server
				Set<String> names = RespCookies.getNames(); //Associated names
				Iterator<String> i = names.iterator();
				while(i.hasNext()){
					String key = i.next();
					String value = RespCookies.getValue(key);
					try {
						cookies.add(key, value);
					} catch (SpRTException e) {
						String message = "Communication problem: Error Updating Cookies: " 
					    + e.getMessage();
						commError(message, file, cookieMap, br, socket);
					}
				}
				req.setCookies(cookies);
				cookieMap.put(remoteIP, cookies);
			}	
		}
		
		//NOTE: On any termination - save the IP/CookieList map, close socket.
		//      If any part of this process fails, print error message and
		//      terminate.
		terminator(file,cookieMap,socket,br);
		return;
	}
}
