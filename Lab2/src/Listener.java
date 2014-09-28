import java.io.IOException;
import java.net.ServerSocket;

public class Listener implements Runnable {
	
	protected ServerSocket serverSocket;
	protected boolean canRun;
	
	public Listener(int port) {
		canRun = true;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println("Fail to listen on port " + port);
		}
	}
	
	public void run() {
	}
	
	public void stop() {
		canRun = false;
	}
}
