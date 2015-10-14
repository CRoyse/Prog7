/*******************
 *
 * Author:     Corey Royse
 * Assignment: Program 3
 * Class:      4321, Spring 2015
 * Date:       2/22/2015
 * 
 *******************/

package SpRT.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import SPA.app.SPAServer;
import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;
import SpRT.app.SpRTState;
import SpRT.app.SpRTState.State;


/**
 * Server that allows a specified number of clients to execute the Poll and
 * Hello functions
 * Server can be restarted immediately and will terminate any connection in which network I/O
 * blocks for 5 seconds or more.
 * Server also logs all messages to a local file named connections.log
 * Logging Syntax:
 * 	<Client IP>:<Client Port>-<Thread ID><space>[Received: <SpRTRequest>|Sent:<SpRTResponse>]
 * @Author:    Corey Royse
 * Assignment: Program 3
 */
public class SpRTServer {
	
	//Number of Milliseconds we will allow the server to block on a given client.
	private final static int BLOCKINGTIME = 25000;
	
	/**
	 *  Retrieves and returns an SpRTRequest from the given inputstream,
	 * 	handles any errors that occur.
	 * 
	 * @param in inputstream from which we retrieve our request
	 * @return request
	 * @throws SpRTException in event of error decoding request.
	 */
	public static SpRTRequest getRequest(InputStream in) throws SpRTException{
		SpRTRequest req = null;
		req = new SpRTRequest(in);
		return req;
	}
	
	
	/**
	 * Takes a client's socket, parses input and passes off to state-transition handler
	 * until we acheive an accept state
	 * 
	 * @param s client socket
	 * @param l logger
	 * @param d 
	 * @param spaServ 
	 */
	public static void handleClient(Socket s, Logger l, SPAServer spaServ){
		try{
			//Input from Socket
			InputStream in = s.getInputStream();
			//Output from socket
			OutputStream out = s.getOutputStream();
			//We keep track of our state to determine how to handle
			//each message.
			State currentState = State.STARTSTATE;
			
			while(currentState != State.DONE){
				//Request received from client
				SpRTRequest req = getRequest(in);
				//Message posted to log
				String msgLog = "Received Request: " + s.getInetAddress() + ":" + s.getPort() 
						+ "-" + Thread.currentThread().getId() + " Received: " + req;
				//Log message received.
				l.log(Level.INFO, msgLog + System.getProperty("line.separator"));
				//Transition to next state
				currentState = SpRTState.transition(currentState, req, out, l, s, spaServ);
			}
			
		} catch(IOException | SpRTException e){
			//In event of IO or SpRT Error, log error message
			String errorMessage = "Communication Problem: " + e.getMessage() + "***client terminated";
			l.log(Level.WARNING, errorMessage+System.getProperty("line.separator"));
			CookieList c = new CookieList();
			try {
				SpRTResponse resp = new SpRTResponse("ERROR","NULL",errorMessage, c);
				SpRTState.sendResponse(resp, l, s, s.getOutputStream());
			} catch (SpRTException | NullPointerException | IOException e1) {
				errorMessage = "Error responding to bad message: " + e.getMessage();
				l.log(Level.WARNING, errorMessage+System.getProperty("line.separator"));
			}
		} finally{
			try{
				s.close();
			}catch(IOException e){
				String errorMessage = "Error closing connection: " + e.getMessage();
				l.log(Level.WARNING, errorMessage+System.getProperty("line.separator"));
			}
		}
	}
	
	/**
	 * Method called by each of the server's threads
	 * to accept and handle client connections.
	 * 
	 * @param servSock
	 * @param log
	 * @param d 
	 * @param spaServ 
	 */
	public static void runThread(ServerSocket servSock, Logger log, 
			SPAServer spaServ){
		try{
			Socket clientSocket = servSock.accept(); //wait for connection
			//set Blocking time
			clientSocket.setSoTimeout(BLOCKINGTIME);
			//Enable address reuse.
			clientSocket.setReuseAddress(true);
			handleClient(clientSocket,log, spaServ); //process
		} catch(IOException e){
			System.err.println("Unable to start: " + e.getMessage());
			terminator(servSock);
		}
	}
	
	/**
	 * Closes socket and handles any error that occurs.
	 * @param servSock ServerSocket being closed.
	 */
	public static void terminator(ServerSocket servSock){
		try {
			servSock.close();
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Termination error: " + e.getMessage());
			System.exit(2);
		}
	}
	
	/**
	 * Creates a pool of threads to take clients for the Poll and Hello functions
	 * 
	 * @param args server port, number of threads
	 */
	public static void main(String[] args){
		//Retrieve arguments
		if(args.length != 2){
			System.err.println("Unable to start: Expect server port and number of threads");
			System.exit(1);
		}
		//Server Port
		int serverPort = Integer.parseInt(args[0]);
		//Number of threads to run at once.
		int numThreads = Integer.parseInt(args[1]);
		
		//Create Logger - Note: Logger is thread-safe
		//File to which we want to log
		FileHandler logFile = null;
		//Formatter used in logging
		SimpleFormatter formatter = null;
		try {
			
			//SPA server which handles SPA Requests alongside the SpRT server.
			final SPAServer spaServ;	
			//Logger
			final Logger log = Logger.getLogger("ServerLog");
			logFile = new FileHandler("connections.log");
			formatter = new SimpleFormatter();
			logFile.setFormatter(formatter);
			logFile.setEncoding("US-ASCII");
			log.addHandler(logFile);
			
			//Create Server Socket
			//Server Socket used to take connections
			final ServerSocket servSock = new ServerSocket(serverPort);
			//Create SPA server with a single socket to service SPA queries.
			spaServ = new SPAServer(serverPort, log);
			//The SPA Server spins indefinitely serving clients,
			//so we give it its own thread.
			Thread spaThread = new Thread(){
				public void run(){
					//Handle SPA clients
					spaServ.takeClients();
				}
			};
			spaThread.start();
			
			//Spawn specified number of threads to service clients
			//NOTE: Each message sent or received is logged to file.
			for(int i = 0; i < numThreads; i++){
				Thread thread = new Thread(){
					public void run(){
						while(true){
							runThread(servSock, log, spaServ);
						}
					}
				};
				thread.start();
			}
			
			
		} catch (SecurityException | IOException  e) {
			System.err.println("Unable to start: " + e.getMessage());
			System.exit(1);
		}
	}

}
