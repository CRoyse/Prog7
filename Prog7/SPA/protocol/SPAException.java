package SPA.protocol;

/**
 * 
 * @author Corey Royse
 * Assignment: Program 4
 * SPAException thrown when errors are encountered in SPA protocol.
 */
public class SPAException extends Exception{

	/**
	 * Auto-generated serial ID
	 */
	private static final long serialVersionUID = -4032178887969762904L;
	
	/**
	 * Constructor with message
	 * @param msg message
	 */
	public SPAException(String msg){
	  super(msg);
	 }

	 /**
	  * Constructor with message and cause
	  * @param msg message
	  * @param cause
	  */
	 public SPAException(String msg, Throwable cause){
	  super(msg, cause);
	 }

}
