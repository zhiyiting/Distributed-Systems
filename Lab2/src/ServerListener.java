import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

/**
 * Dispatcher to unmarshall the method invocation
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
				new Thread(new Dispatcher(socket, this)).start();
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
