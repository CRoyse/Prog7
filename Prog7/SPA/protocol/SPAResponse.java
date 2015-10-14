package SPA.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Corey Royse
 * Assignment: Program 4
 * 
 * Class that represents a SPAResponse, a type of the Abstract SPAMessage that
 * implements its own encoding.
 */
public class SPAResponse extends SPAMessage{
	
	//4 Byte, big endian int containing the time in seconds since 1970
    //of the last application execution. 0 if no execution has occurred.
	private int timeStamp;
	//Number of application records present.
	private byte appCount;
	//array of shorts, each representing the  number of times 
    //a given app has been used.
	private short[] appUseCount;
	//array of bytes, each containing the number of characters
    //in a given application's name.
	private byte[] appNameLength;
	//A two dimensional byte array, containing ASCII encoded characters
    //that comprise each application's name.
	private byte[][] appNames;
	
	/**
	 * default constructor
	 */
	public SPAResponse(){
		super();
		timeStamp = 0;
		appCount = 0;
		appUseCount = new short[0];
		appNameLength = new byte[0];
		appNames = new byte[0][0];
	}
	
	/**
	 * constructor from a single packet of bytes
	 * @param pkt
	 */
	public SPAResponse(byte[] pkt) throws SPAException{
		super(pkt);
		if(!queryResponse){
			throw new SPAException("Error: Attempted to build SPAResponse from SPAQuery");
		}
		//Inputstreams used to parse the packet
		ByteArrayInputStream bs = new ByteArrayInputStream(pkt);
		DataInputStream in = new DataInputStream(bs);
		
		try{
			//read past the VQR and msgID bytes - we already validated those in the superconstructor.
			in.readShort();
			//read timestamp
			int time = in.readInt();
			setTimeStamp(time);
			//Read appCount
			appCount = in.readByte();
			int appCountInt = appCount & 0xFF;
			appUseCount = new short[appCountInt];
			appNameLength = new byte[appCountInt];
			appNames = new byte[appCountInt][];
			//For each app
			int appsRead = 0;
			boolean doneReading = false;
			if(appCountInt != 0){
				do{
					//Read use count
					appUseCount[appsRead] = in.readShort();
					//Read length
					appNameLength[appsRead] = in.readByte();
					int nameLengthInt = appNameLength[appsRead] & 0xFF;
					appNames[appsRead] = new byte[nameLengthInt];
					//Read in appropriate number of characters
					int read = in.read(appNames[appsRead]);
					if(read == -1){
						//if we run out of buffer, we're done.
						doneReading = true;
					}
					appsRead++;
				}while(appsRead < appCountInt && !doneReading);
			}
			else{
				doneReading = true;
			}
			
			if(doneReading && appsRead < appCountInt){
				throw new SPAException("Error: Error parsing packet");
			}
			
		}catch(IOException e){
			throw new SPAException("Error: Error parsing packet");
		}
	}
	
	/* (non-Javadoc)
	 * @see SPA.protocol.SPAMessage#encode()
	 */
	@Override
	public byte[] encode() throws SPAException {
		//simply store all our data in a single byte array in the proper order
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		byte[] header;
		try {
			header = super.encode();
			out.writeInt(timeStamp);
			out.writeByte(appCount);
			int appCountInt = appCount & 0xFF;
			for(int i = 0; i < appCountInt; i++){
				out.writeShort(appUseCount[i]);
				out.writeByte(appNameLength[i]);
				int appLength = appNameLength[i] & 0xFF;
				for(int j = 0; j < appLength; j++){
					out.writeByte(appNames[i][j]);
				}
			}
			out.flush();
		} catch (IOException e) {
			throw new SPAException("Error Encoding Response: " + e.getMessage());
		}
		byte[] msg = bytes.toByteArray();
		byte[] ret = new byte[header.length + msg.length];
		System.arraycopy(header,0,ret,0,header.length);
		System.arraycopy(msg,0,ret,header.length,msg.length);
		//return that array.
		return ret;
	}
	
	/**
	 * @return timeStamp
	 */
	public int getTimeStamp(){
		return timeStamp;
	}
	
	/**
	 * Sets timeStamp - no validation is necessary
	 */
	public void setTimeStamp(int time){
		timeStamp = time;
	}
	
	/**
	 * @return appCount
	 */
	public byte getAppCount(){
		return appCount;
	}
	
	/**
	 * @return appUseCount
	 */
	public short[] getAppUseCounts(){
		return appUseCount;
	}
	
	/**
	 * @return appNames
	 */
	public byte[][] getAppNames(){
		return appNames;
	}
	
	
}
