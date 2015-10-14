package SpRT.app;

import java.nio.ByteBuffer;
import SpRT.app.SpRTState.State;

/**
 * Class containing a byte buffer and a State enum, meant to be attached to each connection.
 * @author Corey Royse
 * Assignment: Program 6
 */
public class SpRTAttachment {
	private ByteBuffer buf; //Buffer associated with client
	private State state; //State of connection
	
	/**
	 * Default constructor
	 */
	public SpRTAttachment(){
		buf = ByteBuffer.allocate(0);
		state = State.STARTSTATE;
	}
	
	/**
	 * @param bufSize size of ByteBuffer
	 * @param state	Initial State
	 */
	public SpRTAttachment(int bufSize, State state){
		buf = ByteBuffer.allocate(bufSize);
		this.state = state;
	}
	
	public ByteBuffer getBuf(){
		return this.buf;
	}
	
	public State getState(){
		return this.state;
	}
	
	public void setState(State s){
		this.state = s;
	}
}
