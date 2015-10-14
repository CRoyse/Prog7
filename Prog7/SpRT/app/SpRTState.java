package SpRT.app;

import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import SPA.app.SPAServer;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;

public class SpRTState {
	
	
	//String we put in a Response signifying an Error State
	private final static String STATUSERROR = "ERROR";
	//String we put in a response signifying regular runtime.
	private final static String STATUSOK = "OK";
	//String we put in a Response upon function completion
	private final static String NULLFUNC = "NULL";
	//Name of FName cookie
	private final static String FNAME = "FName";
	//Name of LName cookie
	private final static String LNAME = "LName";
	//String signifying Poll function
	private final static String POLL = "Poll";
	//String signifying Hello function
	private final static String HELLO = "Hello";
	//String signifying NameStep function
	private final static String NAMESTEP = "NameStep";
	//String signifying FoodStep function
	private final static String FOODSTEP = "FoodStep";
	//String prompting for names
	private final static String NAMEPROMPT = "NAME (First Last)>";
	//String for Mexican food
	private final static String MEXICAN = "Mexican";
	//Outlet associated with Mexican Food
	private final static String MEXOUTLET = "Tacopia";
	//Base discount associated with Mexican Food
	private final static int MEXDISCOUNT = 20;
	//String for Italian food
	private final static String ITALIAN = "Italian";
	//Outlet associated with Italian Food
	private final static String ITALIOUTLET = "Pastastic";
	//Base discount associated with Italian Food
	private final static int ITALIDISCOUNT = 25;
	//Generic outlet
	private final static String GENERICOUTLET = "McDonald's";
	//Base discount associated with Generic Outlet
	private final static int GENERICDISCOUNT = 10;
	
	/**
	 * Enumerates the state of our function for a given connection
	 * @author Corey Royse
	 *
	 */
	static public enum State{
		STARTSTATE, NEEDNAME, NEEDFOOD, DONE;
	}
	
	private SpRTState(){}
	
	
	/**
	 * Sends the given SpRTResponse over the given output stream,
	 * makes a log.
	 * 
	 * @param resp response being sent
	 * @param l logger recording event
	 * @param s socket being used
	 * @param out outputstream used to send response
	 * @throws NullPointerException
	 * @throws SpRTException
	 */
	public static void sendResponse(SpRTResponse resp, Logger l, Socket s, OutputStream out) throws NullPointerException, SpRTException{
		//Send response
		resp.encode(out);
		//Message posted to log
		String msgLog = "Sent Response: " + s.getInetAddress() + ":" + s.getPort() 
				+ "-" + Thread.currentThread().getId() + " Sent: " + resp;
		//Log response
		l.log(Level.INFO, msgLog + System.getProperty("line.separator"));
	}
	
	
	/**
	 * Makes a log of a request containing a function the server did not expect.
	 * @param req request containing bad function
	 * @param l logger keeping record.
	 * @throws SpRTException 
	 */
	public static void badFunction(SpRTRequest req, Logger l) throws SpRTException{
		String error = "Unexpected Function: " + req.getFunction();
		l.log(Level.WARNING, error + System.getProperty("line.separator"));
	}
	
	/**
	 * Sends a generic response in response to a request that represented
	 * an invalid state transition
	 * 
	 * @param req
	 * @param l
	 * @param s
	 * @param out
	 * @throws NullPointerException
	 * @throws SpRTException
	 */
	public static void badTransition(SpRTRequest req,Logger l, Socket s, OutputStream out) throws NullPointerException, SpRTException{
		String error = "Unexpected Function: " + req.getFunction();
		l.log(Level.WARNING, error + System.getProperty("line.separator"));
		CookieList c = new CookieList();
		SpRTResponse resp = new SpRTResponse(STATUSERROR, NULLFUNC,error,c);
		sendResponse(resp, l, s, out);
	}

	/**
	 *  Handles the operations required when the connection 
	 *  is in the STARTSTATE state
	 * 
	 * @param req request received
	 * @param l observing logger
	 * @param s client socket
	 * @param out outputstream to respond over
	 * @param state current state
	 * @param spaServ 
	 * @return new state
	 * @throws SpRTException in event
	 */
	public static State StartState(SpRTRequest req, Logger l, Socket s, 
			OutputStream out, State state, SPAServer spaServ) throws SpRTException{
		State newState = state;
		
		//Determine which function we're using
		switch(req.getFunction()){
		
			//If Poll, check cookies for name - can transition to NEEDNAME or NEEDFOOD
			case POLL:
				
				CookieList cookies = req.getCookieList();
				//ask for FName and LName
				String FName = cookies.getValue(FNAME);
				String LName = cookies.getValue(LNAME);
				//if either is null, we ask for both and transition to NEEDNAME
				if(FName == null | LName == null){
					SpRTResponse resp = new SpRTResponse(STATUSOK, NAMESTEP, NAMEPROMPT, new CookieList());
					//Send Response
					sendResponse(resp, l, s, out);
					newState = State.NEEDNAME;
				}
				//otherwise, we ask for the name's FoodMood and transition to NEEDFOOD
				else{
					String foodPrompt = FName + "'s food mood>";
					SpRTResponse resp = new SpRTResponse(STATUSOK, FOODSTEP, foodPrompt, new CookieList());
					//Send response
					sendResponse(resp, l, s, out);
					//Set new state
					newState = State.NEEDFOOD;
				}
				break;
				
			//If Hello, return greeting and transition to done.
			case HELLO:
				SpRTResponse resp = new SpRTResponse(STATUSOK, NULLFUNC, "Hello!", new CookieList());
				//Send response
				sendResponse(resp, l, s, out);
				newState = State.DONE;
				//Increment number of Hello calls.
				spaServ.recordInvocation(HELLO);
				break;
				
			//Otherwise we log an error message and terminate (return done)
			default:
				badTransition(req, l, s, out);
				newState = State.DONE;
		}
		return newState;
	}
	
	/**
	 * Handles the operations required when the connection is in the NEEDNAME state
	 * 
	 * @param req request received
	 * @param l observing logger
	 * @param s client socket
	 * @param out outputstream to respond over
	 * @param state current state
	 * @return new state
	 * @throws SpRTException in event
	 */
	public static State NameStep(SpRTRequest req, Logger l, Socket s, 
			OutputStream out, State state) throws SpRTException{
		State newState = state;
		//In NeedName, we expect to receive a first and last name
		String [] names = req.getParams(); //this should contain our names
		CookieList cookies = req.getCookieList(); //retrieve any attached cookies.
		//Assuming we receive both, we add those to the received cookieList
		if(names.length == 2){
			cookies.add(FNAME, names[0]);
			cookies.add(LNAME, names[1]);
			//then we prompt for food mood
			String foodPrompt = names[0] + "'s food mood>";
			SpRTResponse resp = new SpRTResponse(STATUSOK,FOODSTEP,foodPrompt,cookies);
			//Send response
			sendResponse(resp, l, s, out);
			//Transition to NEEDFOOD
			newState = State.NEEDFOOD;
		}
		//Otherwise error
		else{
			String poorName = "Error: Poorly formed Name. Name (First Last)>";
			SpRTResponse resp = new SpRTResponse(STATUSERROR,NAMESTEP,poorName,cookies);
			sendResponse(resp, l, s, out);
			//Note that we stay in our current state, which newState is initialized to.
			//This is because we prompt for a new name rather than terminating or proceeding.
		}
		return newState;
	}
	
	/**
	 * Handles the operations required when the connection is in the NEEDFOOD state
	 * 
	 * @param req request received
	 * @param l observing logger
	 * @param s client socket
	 * @param out outputstream to respond over
	 * @param state current state
	 * @param d 
	 * @param spaServ 
	 * @return new state
	 * @throws SpRTException in event
	 */
	public static State FoodMood(SpRTRequest req, Logger l, Socket s, 
			OutputStream out, State state, SPAServer spaServ) throws SpRTException{
		State newState = state;
		//In NeedFood, we expect to receive a string representing the client's mood
		String[] mood = req.getParams(); //Request parameters - should contain mood.
		if(mood.length == 1){
			//We return the associated outlet and discount
			//Note that discount is based on Repeat cookie.
			int baseDiscount = 0; //Base discount associated with a given outlet
			String outlet = ""; //Name of the outlet associated with the given mood.
			switch(mood[0]){
				case MEXICAN:
					outlet = MEXOUTLET;
					baseDiscount = MEXDISCOUNT;
					break;
					
				case ITALIAN:
					outlet = ITALIOUTLET;
					baseDiscount = ITALIDISCOUNT;
					break;
				
				default:
					outlet = GENERICOUTLET;
					baseDiscount = GENERICDISCOUNT;
			}
			//Construct total discount and response.
			CookieList cookies = req.getCookieList(); //Cookies received
			String repeat = cookies.getValue("Repeat"); //String received from repeat clients.
			int repeatDiscount = 0; //Additional discount applied to repeat clients.
			if(repeat == null){
				repeatDiscount++;
				String msg = baseDiscount + "% + " + repeatDiscount +"% off at " + outlet;
				CookieList newCookies = new CookieList();
				newCookies.add("Repeat","" + repeatDiscount);
				SpRTResponse resp = new SpRTResponse(STATUSOK,NULLFUNC,msg,newCookies);
				sendResponse(resp, l, s, out);
			}
			else{
				repeatDiscount = Integer.parseInt(repeat);
				repeatDiscount++;
				String msg = baseDiscount + "% + " + repeatDiscount + "% off at " + outlet;
				CookieList newCookies = new CookieList();
				newCookies.add("Repeat","" + repeatDiscount);
				SpRTResponse resp = new SpRTResponse(STATUSOK,NULLFUNC,msg,newCookies);
				sendResponse(resp, l, s, out);
			}
			//Transition to DONE.
			newState = State.DONE;
			//Increment number of Poll usage
			spaServ.recordInvocation(POLL);
		}
		else{
			//Otherwise handle poorly formed food mood - do not change state.
			CookieList cookies = req.getCookieList();
			String FName = cookies.getValue(FNAME);
			String poorMood = "Poorly formed food mood." + FName +  "'s Food mood>";
			SpRTResponse resp = new SpRTResponse(STATUSERROR,FOODSTEP,poorMood,cookies);
			sendResponse(resp, l, s, out);
		}
		
		return newState;
	}
	
	/**
	 * Processes and responds to the given request based on the given state,
	 * then returns the next connection state
	 * 
	 * @param currState
	 * @param req request to be processed
	 * @param out outputStream to respond over
	 * @param l logger to log with.
	 * @param d 
	 * @param spaServ 
	 * @Param s socket in use
	 * @return newState
	 */
	public static State transition(State currState, SpRTRequest req, OutputStream out, Logger l,
			Socket s, SPAServer spaServ){
		//Represents the state we are in after processing the message
		//can be same as initial state.
		State newState = currState;
		
		try{
			//Construct and send response according to current state and
			//content of request.
			switch(currState){
				case STARTSTATE:
					newState = StartState(req, l, s, out, newState,spaServ);
					break;
				case NEEDNAME:
					if(!req.getFunction().equals(NAMESTEP)){
						badTransition(req, l, s, out);
						newState = State.DONE;
					}
					else{
						newState = NameStep(req, l, s, out, newState);
					}
					break;
				case NEEDFOOD:
					if(!req.getFunction().equals(FOODSTEP)){
						badTransition(req, l, s, out);
						newState = State.DONE;
					}
					else{
						newState = FoodMood(req, l, s, out, newState, spaServ);
					}
					break;
				default:
					//Otherwise we do nothing
			}
		} catch(SpRTException e){
			String error = "Communication problem: " + e.getMessage() + "***client terminated";
			l.log(Level.WARNING, error + System.getProperty("line.separator"));
			//We set our state to done so that the handler knows to terminate this connection.
			newState = State.DONE;
		}
		
		return newState;
	}
	
	
}
