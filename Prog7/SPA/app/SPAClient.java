/*******************
 *
 * Author:     Corey Royse
 * Assignment: Program 4
 * Class:      4321, Spring 2015
 * Date:       3/18/2014
 *
 * This is a client that sends SPAQueries to a given server port and handles SPAResponses
 *******************/

package SPA.app;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Date;

import SPA.protocol.SPAException;
import SPA.protocol.SPAMessage;
import SPA.protocol.SPAQuery;
import SPA.protocol.SPAResponse;


/**
 * Client that allows a user to send and receive SPAMessages
 * to a specified server port.
 * @Author:    Corey Royse
 * Assignment: Program 4
 */
public class SPAClient {
	
	//Encoding standard to be used
	private final static String ENCODING = "US-ASCII";
	//Number of milliseconds to block on network I/O
	private final static int BLOCKTIME = 3000;
	//Maximum number of times to attempt retransmission if necessary
	private final static int MAXTRIES = 5;
	//number of bytes we give our buffer to receive responses.
	//Slightly larger than the maximum size of a DatagramPacket
	private final static int RESPONSELENGTH = 65600;
	
	/**
	 * Decodes a packet and determines whether it is a response intended
	 * for this application
	 * 
	 * @param receivePacket
	 * @param msgID
	 * @param serverName
	 * @return
	 * @throws IOException
	 * @throws SPAException
	 */
	public static boolean processPacket(DatagramPacket receivePacket, byte[] msgID, 
			InetAddress serverName) throws IOException, SPAException{
		boolean received = false;
		int len = receivePacket.getLength();
		byte[] msg = new byte[len];
		System.arraycopy(receivePacket.getData(), 0, msg, 0, len);
		//Check source
		if(!receivePacket.getAddress().equals(serverName)){
			throw new IOException("Received packet from an unknown source");
		}
		//Check ID
		//decode packet.
		SPAMessage m = SPAMessage.decode(msg);
		if(!(m instanceof SPAResponse)){
			//if we receive a query or other type of message, we print
			//an error message and disregard.
			System.err.println("ERROR: Received Query instead of response");
		}
		else if(m.getMsgID() == msgID[0]){
			//If we receive a response whose ID matches
			//our query, we stop awaiting a response and process this one.
			//Otherwise we disregard.
			received = true;
		}
		return received;
	}
	
	/**
	 * Decodes a given packet and either identifies it as an invalid request
	 * or identifies it as a valid response and prints its contents
	 * 
	 * @param receivePacket
	 * @throws SPAException Error decoding
	 * @throws UnsupportedEncodingException Error encoding
	 */
	public static void decodePacket(DatagramPacket receivePacket) throws SPAException, UnsupportedEncodingException{
		int len = receivePacket.getLength();
		byte[] msg = new byte[len];
		System.arraycopy(receivePacket.getData(), 0, msg, 0, len);
		SPAMessage m = SPAMessage.decode(msg);
		if(!(m instanceof SPAResponse)){
			System.err.println("ERROR: Received Response instead of query");
		}
		else{
			SPAResponse response = (SPAResponse) m;
			byte vqr = response.getVerQRErr();
			byte version = vqr;
			version >>>= 4;
			byte error = (byte)(vqr & 0x07);
			byte respID = response.getMsgID();
			int IDint = respID & 0xFF;
			long time = (response.getTimeStamp()&0xFFFFFFFFL) * 1000L;
			Date date = new Date(time);
			byte numApps = response.getAppCount();
			int numAppsInt = numApps & 0xFF;
			short[] appUses = response.getAppUseCounts();
			byte[][] appNames = response.getAppNames();
			//Print the entire contents of the reply to the terminal.
			System.out.println("Version: " + version);
			System.out.println("Error Code: " + error);
			System.out.println("ID: " + IDint);
			System.out.println("Timestamp: " + date);
			System.out.println("Number of Apps: " + Integer.toString(numApps&0xFFFF));
			for(int i = 0; i < numAppsInt; i++){
				String appName = new String(appNames[i],ENCODING);
				System.out.println("App " + (i+1) + ": " + appName);
				System.out.println("Uses: " + Integer.toString((appUses[i]&0xFFFF)));
			}
		}
	}
	
	public static byte[] encodeQuery(SPAQuery query, byte[] msgID, byte bizLength, byte[] bizName) throws SPAException{
		query.setVerQRErr((byte)0x20); //version: 0010, QR: 0, Err: 000
		query.setMsgID(msgID[0]);
		query.setBusinessName(bizLength, bizName);
		//Encode as DatagramPacket
		return query.encode();
	}
	
	/**
	 * @param args Server IP/Name, port, business name
	 */
	public static void main(String [] args){
		//Verify, retrieve arguments
		if(args.length != 3){
			System.err.println("Unable to start: expects Server Identity, Server Port, Cookie File");
			System.exit(1);
		}
		//name of the server we wish to connect to
		InetAddress serverName = null;
		try {
			serverName = InetAddress.getByName(args[0]);
		} catch (UnknownHostException e2) {
			System.err.println("Unable to start: " + e2.getMessage());
			System.exit(1);
		}
		int servPort = (args.length == 3) ? Integer.parseInt(args[1]) : 7; //port to connect to
		String businessName = args[2]; //name of the business making the query.
		
		//Initialize DatagramSocket
		DatagramSocket socket = null; //socket to facilitate communication with the server.
		try {
			socket = new DatagramSocket();
			//Set receive blocking time
			socket.setSoTimeout(BLOCKTIME);
		} catch (SocketException e) {
			System.err.println("Error Initializing Socket: " + e.getMessage());
			System.exit(1);
		}
		//Create message, initialize
		SPAQuery query = new SPAQuery();
		SecureRandom rand = new SecureRandom(); //Random number generator used to provide a unique Message ID.
		byte msgID[] = new byte[1]; //message ID to be generated by rand.
		rand.nextBytes(msgID); //random byte generation
		byte[] bizName = null;
		byte bizLength = 0;
		try {
			bizName = businessName.getBytes(ENCODING);
			bizLength = (byte) (bizName.length & 0xFF);
		} catch (UnsupportedEncodingException e1) {
			System.err.println("Unsupported Encoding Error: " + e1);
			System.exit(1);
		}
		try {
			byte[] encQuery = encodeQuery(query, msgID, bizLength, bizName);
			DatagramPacket sendPacket = new DatagramPacket(encQuery, encQuery.length, serverName, servPort);
			//Create DatagramPacket to receive response
			DatagramPacket receivePacket = new DatagramPacket(new byte[RESPONSELENGTH],RESPONSELENGTH);
			//Send packet and retrieve response - packets may be lost, so
			//we attempt until we hit/exceed MAXTRIES attempts or successfully receive a response
			//that maps to our query.
			int tries = 0; //tracker for how many times we attempt retransmission
			boolean received = false; //true when we have received a response that corresponds
			do{
				try{
					//Send packet
					socket.send(sendPacket);
					//Receive packet
					socket.receive(receivePacket);
					received = processPacket(receivePacket, msgID, serverName);
				}catch(InterruptedIOException e){ //We timed out
					tries++;
					System.out.println("Timed out, " + (MAXTRIES-tries) + " retransmissions remain...");
				}
			}while((!received)&& (tries < MAXTRIES));
			//decode packet.
			decodePacket(receivePacket);
			
		} catch (SPAException e) {
			System.err.println("SPA error: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("NetworkIO error: " + e.getMessage());
		}
		//Not reached
	}
}
