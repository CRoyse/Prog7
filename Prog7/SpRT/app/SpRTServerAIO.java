package SpRT.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import SPA.app.SPAServer;
import SpRT.app.SpRTState.State;
import SpRT.app.SpRTState;
import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;

/**
 * SpRTServer developed for Asynchronous, non-blocking network I/O
 * @author Corey Royse
 * Assignment: Bonus Program 7
 *
 */
public class SpRTServerAIO {
	
	private final static int BUFSIZE = 1500; //Number of bytes to allocate to ByteBuffer reading from client.
	private final static String ENCODING = "US-ASCII"; //Encoding standard to be passed to logger.
	
	/**
	 * parse the given bytes 
	 * in search of a pair of '/r/n's delimiting the end of a SpRT Message
	 * 
	 * @param message bytes to be checked for a message
	 * @return -1 if no message is found, index of end of delimiter otherwise
	 */
	private static int frameMsg(byte[] message, int limit){
		boolean messageFound = false;
		boolean cr = false;
		boolean crlf = false;
		int index = 0;
		
		while(index < limit){
			byte b = message[index];
			if(b == '\r'){
				if(!cr){
					cr = true;
				}
				else if(crlf){
					crlf = false;
				}
			}
			else if (b == '\n'){
				if(cr){
					if(!crlf){
						crlf = true;
						cr = false;
					}
					else{
						messageFound = true;
						return index;
					}
				}
			}
			else{
				if(cr){
					cr = false;
				}
				if(crlf){
					crlf = false;
				}
			}
			index++;
		}
		if(!messageFound || index > limit){
			//if we fail to find our delimiter, we return -1.
			index = -1;
		}
		return index;
	}
	
	
	/**
	 * Executes SpRT Protocol on data received via given channel.
	 * 
	 * @param clientChan
	 * @param attachment
	 * @param log
	 * @param spaServ
	 * @throws IOException
	 */
	private static void handleClient(AsynchronousSocketChannel clientChan,
			Void attachment, Logger log, SPAServer spaServ) throws IOException{
		//Frame incoming messages, process, write out
		State currState = State.STARTSTATE;
		ByteBuffer buf = ByteBuffer.allocate(BUFSIZE);
		boolean timeToWrite = false; //false = read, true = write
		do{
			
			if(!timeToWrite){
				int bytesRead;
				Future<Integer> result = clientChan.read(buf);
				while(!result.isDone()){
					//do nothing
				}
				try {
					bytesRead = (int) result.get(20, TimeUnit.SECONDS);
					if(bytesRead == -1){
						//Did the other end close?
						clientChan.close();
						currState = State.DONE;
					}
					else{
						//Frame message - see if we have a complete request yet.
						int msgIndex = frameMsg(buf.array(), buf.position());
						if(msgIndex != -1){
							//If we have a request, we construct and log it 
							byte[] msg = new byte[msgIndex+1];
							buf.flip(); //prepare to write
							buf.get(msg, 0, msgIndex+1);
							buf.clear();
							ByteArrayInputStream bytes = new ByteArrayInputStream(msg);
							SpRTRequest req;
							try {
								req = new SpRTRequest(bytes);
								//Message posted to log
								String msgLog = "Received Request: " + clientChan.getRemoteAddress() + "-" 
								+ Thread.currentThread().getId() + " Received: " + req;
								//Log message received.
								log.log(Level.INFO, msgLog + System.getProperty("line.separator"));
								//Now, we process it and generate an appropriate
								//SpRTResponse. We then make room in our buffer 
								//and encode our response into it to write out to the client
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								//It's hacky, but our existing transition design expects a socket to get an address from
								//for logging purposes.
								//we pass it a generic socket to pacify it for now.
								currState = SpRTState.transition(currState, req, out, log, new Socket(), spaServ);
								buf.put(out.toByteArray());
								timeToWrite = true;
							} catch (SpRTException e) {
								// If we encounter a SpRTException, we know that
								// we found our delimiter, but these bytes did not produce
								// a valid SpRTRequest. Handle it according to protocol.
								currState = State.DONE;
								String errorMessage = "Communication Problem: " + e.getMessage() + "***client terminated";
								log.log(Level.WARNING, errorMessage+System.getProperty("line.separator"));
								try {
									CookieList c = new CookieList();
									SpRTResponse resp = new SpRTResponse("ERROR", "NULL", errorMessage, c);
									ByteArrayOutputStream out = new ByteArrayOutputStream();
									resp.encode(out);
									buf.put(out.toByteArray());
								} catch (SpRTException e1) {
									errorMessage = "Error responding to bad input: " + e.getMessage();
									log.log(Level.WARNING, errorMessage+System.getProperty("line.separator"));
								}
								clientChan.close();
							}
						}
						//If we don't have a framed response we do nothing and keep waiting.
					}
				} catch (InterruptedException | TimeoutException e2) {
					String errorMessage = "Communication Problem: " + e2.getMessage() + "***client terminated";
					log.log(Level.WARNING, errorMessage+System.getProperty("line.separator"));
					clientChan.close();
				} catch (ExecutionException e2) {
					String errorMessage = "Communication Problem: " + e2.getMessage() + "***client terminated";
					log.log(Level.WARNING, errorMessage+System.getProperty("line.separator"));
					clientChan.close();
				}
			}
			else{
				//Write out response
				buf.flip(); //prepare for writing
				//write response to client
				clientChan.write(buf);
				if(!buf.hasRemaining()){
					timeToWrite = false;
					if(currState == State.DONE){
						clientChan.close();
					}
				}
				//make room for more data to be read in.
				buf.clear();
			}
		}while(!((currState == State.DONE) && !timeToWrite));
	}
	
	/**
	 * Runs a SpRT Server using Asynchronous Socket Channels to handle 
	 * network I/O asynchronously
	 * @param args
	 */
	public static void main(String[] args){
		//Retrieve arguments
				if(args.length !=  1){
					System.err.println("Unable to start: Expect server port");
					System.exit(1);
				}
				
				
				
				//Create Logger - Note: Logger is thread-safe
				//File to which we want to log
				FileHandler logFile = null;
				//Formatter used in logging
				SimpleFormatter formatter = null;
				
				try{
					//Logger
					final Logger log = Logger.getLogger("ServerLog");
					logFile = new FileHandler("connections.log");
					formatter = new SimpleFormatter();
					logFile.setFormatter(formatter);
					logFile.setEncoding(ENCODING);
					log.addHandler(logFile);
					
					//Create listening socket channel
					int servPort = Integer.parseInt(args[0]);
					final AsynchronousServerSocketChannel servChan = 
							AsynchronousServerSocketChannel.open();
					servChan.bind(new InetSocketAddress(servPort));
					
					//Create SPA server with a single socket to service SPA queries.
					final SPAServer spaServ = new SPAServer(servPort, log);
					//The SPA Server spins indefinitely serving clients,
					//so we give it its own thread.
					Thread spaThread = new Thread(){
						public void run(){
							//Handle SPA clients
							spaServ.takeClients();
						}
					};
					spaThread.start();
					
					//Listen for new request
					
					servChan.accept(null, new 
							CompletionHandler<AsynchronousSocketChannel,Void>(){

								@Override
								public void completed(AsynchronousSocketChannel clientChan,
										Void attachment) {
									//accept next connection
									servChan.accept(null, this);
									//Handle client
									try {
										handleClient(clientChan, attachment, log,spaServ);
									} catch (IOException e) {
										String errorMessage = "Communication Problem: " + e.getMessage() + "***client terminated";
										log.log(Level.WARNING, errorMessage+System.getProperty("line.separator"));
									}
								}

								@Override
								public void failed(Throwable exc,
										Void attachment) {
									String errorMessage = "Failed call";
									log.log(Level.WARNING, errorMessage+System.getProperty("line.separator"));
								}
						
					});
				}catch(IOException e) {
					System.err.println("Unable to start: IO error: " + e.getMessage());
				}
	}
}
