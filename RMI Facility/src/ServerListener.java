import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

/**
 * Server Listener to listen to requests and start new threads
 * 
 * @author zhiyiting
 *
 */
public class ServerListener implements Runnable {
	private ServerSocket serverSocket;
	private boolean canRun;
	// service lookup table
	public Hashtable<String, MyRemote> servicetbl;
	

	/**
	 * Constructor to create a server socket
	 * 
	 * @param port
	 */
	public ServerListener(int port) {
		canRun = true;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println("Fail to listen on port " + port);
		}
		System.out.println("Server started, listening on port " + port);
		servicetbl = new Hashtable<String, MyRemote>();
	}

	/**
	 * function to add remote object to the lookup table
	 * @param name
	 * @param remote object
	 */
	public void add(String name, MyRemote rm) {
		servicetbl.put(name, rm);
	}

	/**
	 * method to start the dispatcher as a thread
	 */
	public void run() {
		while (canRun) {
			try {
				Socket socket = serverSocket.accept();
				Thread t = new Thread(new Dispatcher(socket, this));
				t.setDaemon(true);
				t.start();
			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Stop the thread
	 */
	public void stop() {
		canRun = false;
	}
}
