package SPA.app.test;

import java.net.SocketException;
//import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.logging.Logger;

//import SPA.app.SPAServer;

public class SPAServerTestBed {
	
	private static ConcurrentHashMap<String, Short> map = new ConcurrentHashMap<>();
	
	public static void main(String args[]) throws SocketException{
		
		//final Logger log = Logger.getLogger("ServerLog");
		//final SPAServer s = new SPAServer(8080,log);
		
		/*Thread spaThread = new Thread(){
			public void run(){
				while(true){
					//Handle SPA clients
					s.takeClients(log, map, new Date());
				}
			}
		};
		spaThread.start();*/
		
		
		map.putIfAbsent("Hi", (short) 2);
		
		//Handle SPA clients
		//s.takeClients(log, map, new Date());
		
	}
	
}
