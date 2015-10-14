package SpRT.protocol;
/*********************
 *
 * Author:     Corey Royse
 * Assignment: Program 0
 * Class:      4321, Spring 2014
 * Date:       1/16/2014
 *
 * This is an exception included with the SpRT protocol.
 ********************/

/**
 * @author Corey Royse
 * Assignment: Program 0
 * 
 * SpRTException thrown when errors occur in SpRT protocol
 */
public class SpRTException extends Exception{


    /**
	 * Auto-Generated UID.
	 */
	private static final long serialVersionUID = 1L;

/**
 * Constructor with message
 * @param msg message
 */
public SpRTException(String msg){
  super(msg);
 }

 /**
  * Constructor with message and cause
  * @param msg message
  * @param cause
  */
 public SpRTException(String msg, Throwable cause){
  super(msg, cause);
 }

}
