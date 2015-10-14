package SpRT.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import SPA.app.SPAServer;
import SpRT.protocol.SpRTException;

/**
 * Server that allows a specified number of clients to execute the Poll and
 * Hello functions
 * Server can be restarted immediately
 * Server uses selector-based IO to handle multiple clients
 * Server also logs all messages to a local file named connections.log
 * Logging Syntax
 * 	<Client IP>:<Client Port>-<Thread ID><space>[Received: <SpRTRequest>|Sent:<SpRTResponse>]
 * @Author:    Corey Royse
 * Assignment: Program 6
 */
public class SpRTServerSIO {
	
	private static final int BUFSIZE = 15000; //Buffer size (Bytes)
	private static final int TIMEOUT = 3000; //Wait timeout (milliseconds)
	private final static String ENCODING = "US-ASCII"; //Encoding standard to be passed to logger.

	
	
	public static void main(String[] args) throws SpRTException{
		
		//Retrieve arguments
		if(args.length != 1){
			System.err.println("Unable to start: Expect server port");
			System.exit(1);
		}
		
		//Create Logger - Note: Logger is thread-safe
		//File to which we want to log
		FileHandler logFile = null;
		//Formatter used in logging
		SimpleFormatter formatter = null;
		
		try {
			
			//Logger
			final Logger log = Logger.getLogger("ServerLog");
			logFile = new FileHandler("connections.log");
			formatter = new SimpleFormatter();
			logFile.setFormatter(formatter);
			logFile.setEncoding(ENCODING);
			log.addHandler(logFile);
			
			//Create a selector to multiplex listening sockets and connections
			Selector selector = Selector.open();
			
			//Create listening socket channel and register selector
			int servPort = Integer.parseInt(args[0]);
			ServerSocketChannel listnChannel = ServerSocketChannel.open();
			listnChannel.socket().bind(new InetSocketAddress(servPort));
			listnChannel.configureBlocking(false);// must be nonblocking to register
			//Register selector with channel, ignore returned key
			listnChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			
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
			
			//Create a handler that will implement the protocol
			TCPProtocol protocol = new SpRTSelectorProtocol(BUFSIZE);
			while(true){ //Run forever, processing IO as it becomes available
				//Wait for some channel to be ready (or else timeout)
				if(selector.select(TIMEOUT) == 0){ //# of ready channels
					continue; //If no channels are ready, we iterate the loop again.
				}
				//Get iterator on set of keys with I/O waiting to be processed
				Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
				while(keyIter.hasNext()){
					SelectionKey key = keyIter.next(); //this is a bit mask
					if(key.isAcceptable()){ 
						//If the channel has pending connection requests
						protocol.handleAccept(key);
					}
					if(key.isReadable()){
						//Client channel has pending data
						protocol.handleRead(key,log,spaServ);
					}
					if(key.isValid() && key.isWritable()){
						//Client channel is available for writing and 
						//key is valid (the channel is not closed)
						protocol.handleWrite(key);
					}
					keyIter.remove(); //remove from set of selected keys
				}
			
			}
			
		} catch (IOException e) {
			System.err.println("Unable to start: IO error: " + e.getMessage());
		}
	}
	
}
