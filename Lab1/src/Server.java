import java.io.IOException;
import java.net.ServerSocket;

public class Server extends Thread {
	private int port;
	private ServerSocket serverSocket;
	private ProcessManager processManager;
	
	public Server(int p, ProcessManager mgr) {
		this.port = p;
		this.processManager = mgr;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not listen on port " + port);
			System.exit(-1);
		}
	}

	public void run() {
		try {
			//Socket socket = serverSocket.accept();
			//new Thread(w).start();
		} catch (IOException e) {
			System.out.println("Accept failed: port " + port);
		}
		
	}
}
