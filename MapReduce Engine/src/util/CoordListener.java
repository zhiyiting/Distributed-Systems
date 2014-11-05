package util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CoordListener implements Runnable {

	private ServerSocket serverSocket;
	private JobTracker tracker;
	
	public CoordListener(int port, JobTracker tracker) {
		try {
			this.serverSocket = new ServerSocket(port);
			this.tracker = tracker;
		} catch (IOException e) {
			System.out.println("Cannot listen on port " + port);
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				Thread t = new Thread(new CoordDispatcher(socket, tracker));
				t.setDaemon(true);
				t.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
