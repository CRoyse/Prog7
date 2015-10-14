package SPA.app;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import SPA.protocol.SPAException;
import SPA.protocol.SPAMessage;
import SPA.protocol.SPAQuery;
import SPA.protocol.SPAResponse;

/**
 * Class that allows a user to receive, process, and send
 * SPAMessages from a given server port.
 * @author Corey Royse
 * Assignment: Program 5
 */
public class SPAServer {
	private int servPort; //Server port assigned to us by the larger SpRT Server
	private final static int MSGLENGTH = 65600; //Maximum size of a SPAMessage
	private DatagramSocket sock; //socket used to communicate with clients
	private DatagramPacket pkt; //packet used to store incoming requests.
	private final static String ENCODING = "US-ASCII"; //Encoding standard to be used
	private ConcurrentHashMap<String, Short> appCounts;
	private Logger log; //Logger assigned for us to use on construction
	private static Date date;
	
	/**
	 * Default constructor
	 */
	public SPAServer(){
		servPort = 0;
		date = new Date(0L);
	}
	
	/**
	 * Initializes a socket on the given port
	 * and readies a datagram packet
	 * 
	 * @param port port to be mounted on
	 * @param l	   logger to record connections
	 * @throws SocketException in the event of trouble initializing the socket
	 */
	public SPAServer(int port, Logger l) throws SocketException{
		servPort = port;
		sock = new DatagramSocket(servPort);
		pkt = new DatagramPacket(new byte[MSGLENGTH], MSGLENGTH);
		appCounts = new ConcurrentHashMap<>();
		log = l;
		date = new Date(0L);
	}
	
	/**
	 * Updates our mapping of app names and their use counts by incrementing
	 * the usage of the specified app.
	 * @param function name of the function to be incremented
	 */
	public synchronized void recordInvocation(String function){
		//If our map doesn't already contain function, we atomically insert
		if(appCounts.putIfAbsent(function, (short)1) != null){
			//If it already contains function, we have to check that incrementing
			//will not cause overflow, then increment if this is true.
			short count = appCounts.get(function);
			if((short)(count+1) != 0){
				appCounts.replace(function, (short)(count+1));
			}
		}
		//update timestamp
		date.setTime(new Date().getTime());
	}
	
	/**
	 * Receives, processes, and responds to SPAMessages sent by other applications.
	 * 
	 * @param d date of last app invocation
	 */
	public void takeClients(){
		while(true){ //run forever, servicing clients as they come.
			try {
				//receive packet
				sock.receive(pkt);
				//Parse incoming message, log query, construct response
				SPAResponse resp = buildResponse(date);
				//send
				sendResponse(resp);
			} catch (IOException e) {
				//Respond to System Error
				systemError();
			} catch (SPAException e) {
				//Respond to error parsing packet
				parsingError();
			}
			
			//reset length to maintain buffer
			pkt.setLength(MSGLENGTH);
		}
		//Not reached
	}
	
	/**
	 * Constructs and sends a generic response when unable to parse a received
	 * packet properly
	 */
	private void parsingError() {
		//ErrCode = 2, msgID = 0, time = 0, AppCount = 0
		byte vqe = 0x2A; //0010 1010
		SPAResponse resp;
		try {
			resp = generateErrorMsg(vqe);
			sendResponse(resp);
		} catch (IOException | SPAException e1) {
			//This shouldn't go wrong, but if it does we log the error and stop
			//so that the server can continue to take new clients.
			String logMsg ="ERROR: Exception thrown handling System error: " + e1.getMessage();
			log.log(Level.WARNING, logMsg + System.getProperty("line.separator"));
		}
	}

	/**
	 * Constructs and sends a generic response when a system error is encountered.
	 */
	private void systemError(){
		//ErrCode = 4, msgID = 0, timestamp = 0, AppCount = 0
		byte vqe = 0x2C; // 0010 1100
		SPAResponse resp;
		try {
			resp = generateErrorMsg(vqe);
			sendResponse(resp);
		} catch (IOException | SPAException e1) {
			//This shouldn't go wrong, but if it does we log the error and stop
			//so that the server can continue to take new clients.
			String logMsg ="ERROR: Exception thrown handling System error: " + e1.getMessage();
			log.log(Level.WARNING, logMsg + System.getProperty("line.separator"));
		}
		
	}
	
	/**
	 * Generates a SPAResponse with the given version and error message
	 * called in response to an error
	 * @param vqe
	 * @throws IOException 
	 * @throws SPAException 
	 */
	private SPAResponse generateErrorMsg(byte vqe) throws IOException, SPAException{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		out.writeByte(vqe);
		out.writeByte(0); //msgID = 0
		out.writeInt(0);  //timestamp = 0
		out.writeByte(0); //appCount = 0
		out.flush();
		out.close();
		byte[] rBytes = bytes.toByteArray();
		bytes.close();
		SPAResponse resp = (SPAResponse) SPAMessage.decode(rBytes);
		return resp;
	}

	/**
	 * Parse a given packet for a SPAMessage, then parse that message
	 * in order to build an appropriate response
	 * 
	 * @param d 
	 * @return
	 * @throws SPAException in the event of a parsing error
	 * @throws IOException  in the event of a system error
	 */
	public SPAResponse buildResponse(Date d) throws SPAException, IOException{
		
		int len = pkt.getLength();
		byte[] msg = new byte[len];
		System.arraycopy(pkt.getData(), 0, msg, 0, len);
		SPAMessage m = SPAMessage.decode(msg);
		byte vqe = 0;
		byte msgID = 0;
		SPAResponse resp = new SPAResponse();
		if(m instanceof SPAQuery){
			SPAQuery q = (SPAQuery) m;
			resp = parseQuery(q, d);
		}
		else{
			//First we verify proper version and error code = 0
			vqe = m.getVerQRErr();
			if(((vqe & 0xF0) != 0x20) || ((vqe & 0x0F) != 0x00)){
				resp = badVersionOrError(vqe);
			}
			else{
				//If we have received a response we construct an error response:
				//ErrCode = 3, msgID = (receivedID), Timestamp = 0, numApps = 0
				vqe = 0x2B; //0010 1011
				msgID = m.getMsgID();
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(bytes);
				out.writeByte(vqe);
				out.writeByte(msgID);
				out.writeInt(0); //timestamp = 0
				out.writeByte(0); //numapps = 0
				out.flush();
				out.close();
				byte[] rBytes = bytes.toByteArray();
				bytes.close();
				resp = (SPAResponse) SPAMessage.decode(rBytes);
			}
		}
		
		return resp;
	}

	
	
	/**
	 * Validates that a query has a valid Version and error code,
	 * builds an appropriate response
	 * 
	 * @param q Query to be parsed
	 * @return appropriate SPAResponse based on our query
	 * @throws IOException  in the event of a system error
	 * @throws SPAException in the event of a parsing error
	 */
	public SPAResponse parseQuery(SPAQuery q, Date d) throws IOException, SPAException{
		SPAResponse resp = new SPAResponse();
		//First we verify proper version and error code = 0
		byte vqe = 0;
		vqe = q.getVerQRErr();
		if(((vqe & 0xF0) != 0x20) || ((vqe & 0x0F) != 0x00)){
			badVersionOrError(vqe);
		}
		else{
			//We need to log query source addresses and business names
			logQuery(q);
			//We send a response to the given ID, Error Code 0, current timestamp,
			//and the current number of applications and their entries.
			vqe = 0x28; //0010 1000
			byte msgID = 0;
			msgID = q.getMsgID();
			byte numApps = 0;
			numApps = (byte)appCounts.size();
			short[] appUses = new short[numApps];
			String[] names = new String[numApps];
			byte[][] appNames = new byte[numApps][];
			byte[] nameLengths = new byte[numApps];
			int app = 0;
			AtomicLong time = new AtomicLong();
			time.set(d.getTime()/1000L);
			int timestamp = (int)time.get();
			Iterator<Map.Entry<String, Short>> it = appCounts.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, Short> entry = it.next();
				names[app] = entry.getKey();
				nameLengths[app] = (byte)names[app].length();
				appUses[app] = entry.getValue();
				appNames[app] = names[app].getBytes(ENCODING);
				app++;
			}
			
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(bytes);
			out.writeByte(vqe);
			out.writeByte(msgID);
			out.writeInt(timestamp);
			out.writeByte(numApps);
			for(int i = 0; i < (numApps&0xFF); i++){
				out.writeShort(appUses[i]);
				out.writeByte(nameLengths[i]);
				for(int j = 0; j < (nameLengths[i]&0xFF); j++){
					out.writeByte(appNames[i][j]);
				}
			}
			out.flush();
			byte[] rBytes = bytes.toByteArray();
			resp = (SPAResponse) SPAMessage.decode(rBytes);
		}
		
		return resp;
	}
	
	/**
	 * Constructs a generic response to send in response to any received
	 * SPAMessage with a version other than 2 (0010) or an error code
	 * other than 0.
	 * 
	 * @param vqe received Verson and Error code
	 * @param numApps 
	 * @return
	 * @throws IOException
	 * @throws SPAException
	 */
	public SPAResponse badVersionOrError(byte vqe) throws IOException, SPAException{
		//If either the version or error code is abnormal,
		//we send an appropriate response
		//ErrCode = 1, MsgID = 0, Timestamp = 0, application count = 0
		vqe = 0x29; //0010 1001
		return generateErrorMsg(vqe);
	}
	
	/**
	 * Makes a log of a received query's address and business name 
	 * @param pkt pkt in which query was received
	 * @param q query received
	 * @throws UnsupportedEncodingException 
	 */
	public void logQuery(SPAQuery q) throws UnsupportedEncodingException{
		byte[] bizName = q.getBusinessName();
		String name = new String(bizName,"US-ASCII");
		String logMsg = "Received SPAQuery: source = " + pkt.getAddress() + 
				", business = " + name;
		log.log(Level.INFO, logMsg + System.getProperty("line.separator"));
	}
	
	/**
	 * Converts a given SPAResponse to a series of bytes,
	 * and then sends them in a datagram using a given socket.
	 * 
	 * @param resp response to encode
	 * @param addr address to send to
	 * @throws SPAException in event of encoding error
	 * @throws IOException in event of system error
	 */
	public void sendResponse(SPAResponse resp) throws SPAException, IOException{
		//send
		byte[] encResp = resp.encode();
		DatagramPacket sendPacket = new DatagramPacket(encResp, encResp.length, pkt.getAddress(), pkt.getPort());
		sock.send(sendPacket);
	}
}
