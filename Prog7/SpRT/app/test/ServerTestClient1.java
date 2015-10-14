package SpRT.app.test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Client designed to help test our server.
 * 
 * @author Corey Royse
 *
 */
public class ServerTestClient1 {
	
	/**
	 * @author Corey Royse
	 * 
	 * Driver to test a 5-thread server's response to 6 consecutive client requests
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws InterruptedException 
	 */
	public static void main(String [] args) throws UnknownHostException, IOException, InterruptedException{
		/*String serverName = "localhost";
		int servPort = 8080;
		
		Socket socket1 =  new Socket(serverName, servPort);
		System.out.println("Socket 1 connected to server");
		Thread.sleep(1000);
		Socket socket2 =  new Socket(serverName, servPort);
		System.out.println("Socket 2 connected to server");
		Thread.sleep(1000);
		Socket socket3 =  new Socket(serverName, servPort);
		System.out.println("Socket 3 connected to server");
		Thread.sleep(1000);
		Socket socket4 =  new Socket(serverName, servPort);
		System.out.println("Socket 4 connected to server");
		Thread.sleep(1000);
		Socket socket5 =  new Socket(serverName, servPort);
		System.out.println("Socket 5 connected to server");
		Thread.sleep(1000);
		Socket socket6 =  new Socket(serverName, servPort);
		System.out.println("Socket 6 connected to server");
		
		
		
		Thread.sleep(10000);
		
		System.out.println("Done. Closing");
		socket1.close();
		socket2.close();
		socket3.close();
		socket4.close();
		socket5.close();
		socket6.close();*/
		
		Socket s  = new Socket(args[0], Integer.parseInt(args[1]));
		Socket s2 = new Socket(args[0], Integer.parseInt(args[1]));
		
		s.getOutputStream().write("SpRT/1.0 RUN Hello\r\n\r\n".getBytes("ASCII"));
		
		s2.getOutputStream().write("SpRT/1.0 RUN Hello\r\n\r\n".getBytes("ASCII"));
		
		
		s.close();
		s2.close();
	}
}
