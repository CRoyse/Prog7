package SpRT.app.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;
import SpRT.protocol.SpRTResponse;

/**
 * Client designed to help test our server.
 * 
 * @author Corey Royse
 *
 */
public class ServerTestClient2 {
	
	
	/**
	 * Driver designed to test Server response to various unexpected functions
	 * and other scenarios.
	 * 
	 * @param args
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws SpRTException 
	 */
	public static void main(String [] args) throws UnknownHostException, IOException, InterruptedException, SpRTException{
		String serverName = "localhost";
		int servPort = 8080;
		String params[] = new String[0];
		CookieList cookies = new CookieList();
		cookies.add("fName", "Bob");
		cookies.add("lName", "Smith");
		SpRTRequest req = new SpRTRequest("RUN","Hello",params, cookies);
		for(int i = 0; i < 200; i++){
			Socket socket =  new Socket(serverName, servPort);
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			req.encode(out);
			@SuppressWarnings("unused")
			SpRTResponse resp = new SpRTResponse(in);
			socket.close();
		}
		//Note that as we get deeper in, the code doesn't represent all the tests I ran
		//I found that it was fastest to hardcode new bogus functions rather than come up
		//with a more elegant solution.
		/*params = new String[]{"Mexican"};
		req = new SpRTRequest("RUN","NameStep",params, cookies);
		//req.setCookies(null);
		req.encode(out);
		resp = new SpRTResponse(in);*/
		
	}
}
