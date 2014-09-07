import java.net.Socket;


public class ClientWorker implements Runnable {
	
	private Socket clientSocket;
	
	public ClientWorker(Socket client, String text) {
		this.clientSocket = client;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
