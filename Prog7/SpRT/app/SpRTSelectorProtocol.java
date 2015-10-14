package SpRT.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import SPA.app.SPAServer;
import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;
import SpRT.app.SpRTState.State;

/**
 * Protocol that implements the TCP protocol for our Selector-based
 * SpRT Server
 * @author Corey Royse
 * Assignment: Program 6
 */
public class SpRTSelectorProtocol implements TCPProtocol {
	
	private int bufSize; // Size of I/O Buffer
	
	
	
	/**
	 * Constructor for our protocol
	 * 
	 * @param bufSize desired buffer size
	 */
	public SpRTSelectorProtocol(int bufSize){
		this.bufSize = bufSize;
	}
	

	@Override
	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel)key.channel()).accept();
		clntChan.configureBlocking(false); //Must be nonblocking to register
		// Register the selector with new channel for read and attach byte buffer and state
		SpRTAttachment attachment = new SpRTAttachment(bufSize, State.STARTSTATE);
		clntChan.register(key.selector(), SelectionKey.OP_READ, attachment);
	}
	
	/**
	 * parse the given bytes 
	 * in search of a pair of '/r/n's delimiting the end of a SpRT Message
	 * 
	 * @param message bytes to be checked for a message
	 * @return -1 if no message is found, index of end of delimiter otherwise
	 */
	public int frameMsg(byte[] message, int limit){
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
	
	@Override
	public void handleRead(SelectionKey key, Logger l, SPAServer spaServ) throws IOException {
		//Client socket channel has pending data
		SocketChannel clntChan = (SocketChannel) key.channel();
		SpRTAttachment attachment = (SpRTAttachment)key.attachment();
		ByteBuffer buf = attachment.getBuf();
		State currState = attachment.getState();
		long bytesRead = clntChan.read(buf);
		if(bytesRead == -1){
			//Did the other end close?
			clntChan.close();
			currState = State.DONE;
		}
		else{
			//Frame message - see if we have a complete request yet.
			int msgIndex = frameMsg(buf.array(), buf.position());
			try {
				if(msgIndex != -1){
					//If we have a request, we construct and log it 
					byte[] msg = new byte[msgIndex+1];
					buf.flip(); //prepare to write
					buf.get(msg, 0, msgIndex+1);
					buf.clear();
					ByteArrayInputStream bytes = new ByteArrayInputStream(msg);
					SpRTRequest req = new SpRTRequest(bytes);
					//Message posted to log
					String msgLog = "Received Request: " + clntChan.socket().getInetAddress() + ":" 
					+ clntChan.socket().getPort() + "-" + Thread.currentThread().getId() + " Received: " + req;
					//Log message received.
					l.log(Level.INFO, msgLog + System.getProperty("line.separator"));
					//Now, we process it and generate an appropriate
					//SpRTResponse. We then make room in our buffer 
					//and encode our response into it to write out to the client
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					currState = SpRTState.transition(currState, req, out, l, clntChan.socket(), spaServ);
					attachment.setState(currState);
					buf.put(out.toByteArray());
					//Now we indicate via key that we want to write.
					key.interestOps(SelectionKey.OP_WRITE);
				}
				//If we don't have a framed response we do nothing and keep waiting.
			} catch (SpRTException e) {
				// If we encounter a SpRTException, we know that
				// we found our delimiter, but these bytes did not produce
				// a valid SpRTRequest. Handle it according to protocol.
				String errorMessage = "Communication Problem: " + e.getMessage() + "***client terminated";
				l.log(Level.WARNING, errorMessage+System.getProperty("line.separator"));
				try {
					CookieList c = new CookieList();
					SpRTResponse resp = new SpRTResponse("ERROR", "NULL", errorMessage, c);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					resp.encode(out);
					buf.put(out.toByteArray());
				} catch (SpRTException e1) {
					errorMessage = "Error responding to bad input: " + e.getMessage();
					l.log(Level.WARNING, errorMessage+System.getProperty("line.separator"));
				}
				attachment.setState(State.DONE);
			}
		}
	}

	@Override
	public void handleWrite(SelectionKey key) throws IOException{
		// TODO Auto-generated method stub
		/*
		 * Channel is available for writing, key is valid
		 * IE client channel is not closed.
		 */
		//Retrieve response stored in buffer.
		SpRTAttachment attachment = (SpRTAttachment) key.attachment();
		ByteBuffer buf = attachment.getBuf();
		State currState = attachment.getState();
		buf.flip(); //prepare for writing
		SocketChannel clntChan = (SocketChannel) key.channel();
		//write response to client
		clntChan.write(buf);
		if(!buf.hasRemaining()){
			//Nothing left, no longer interested in writing
			key.interestOps(SelectionKey.OP_READ);
		}
		//make room for more data to be read in.
		buf.clear();
		if(currState == State.DONE){
			clntChan.close();
		}
	}

}