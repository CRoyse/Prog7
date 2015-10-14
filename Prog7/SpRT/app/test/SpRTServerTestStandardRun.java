package SpRT.app.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import SpRT.protocol.CookieList;
import SpRT.protocol.SpRTException;
import SpRT.protocol.SpRTRequest;

public class SpRTServerTestStandardRun {
	
	@SuppressWarnings({ "resource", "unused" })
	public static void main(String [] args){
		
		try {
			ServerSocket s = new ServerSocket(12345);
			while(true){
				Socket clntSock = s.accept();
				System.out.println("Connection confirmed!");
				boolean messageReceived = false;
				
				InputStream in = clntSock.getInputStream();
				OutputStream out = clntSock.getOutputStream();
				CookieList c = new CookieList();
				
				
				int data = 0;
				String response = "";
				while(data != -1){
					try{
						data = in.read();
					}
					catch(IOException e){
						System.err.println("Communication problem: " + e);
					}
					response += (char) data;
				}
				
				ByteArrayInputStream bais = new ByteArrayInputStream(response.getBytes("US-ASCII"));
				try {
					SpRTRequest req = new SpRTRequest(bais);
					System.out.println(req);
				} catch (SpRTException e) {
					System.out.println("Oops: " + e);
				}
			}
		} catch (IOException e) {
			System.out.println("Oops: " + e);
		}
	}
}
