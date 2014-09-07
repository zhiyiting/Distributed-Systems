import java.io.IOException;
import java.net.ServerSocket;

public class Server extends Thread {
	private int port;
	private ServerSocket serverSocket;
	
	public Server(int p) {
		this.port = p;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not listen on port " + port);
			System.exit(-1);
		}
	}

	public void run() {
		ClientWorker w;
		try {
			w = new ClientWorker(serverSocket.accept(), "abc");
			new Thread(w).start();
		} catch (IOException e) {
			System.out.println("Accept failed: port " + port);
		}
		
	}
}
