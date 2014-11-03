package util;

import java.io.IOException;
import java.net.ServerSocket;

public class Listener implements Runnable {
	
	protected ServerSocket serverSocket;
	
	public Listener(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Cannot listen on port " + port);
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
	}
}
