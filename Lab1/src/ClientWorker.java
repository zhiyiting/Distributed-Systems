import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientWorker implements Runnable {

	private Socket clientSocket;
	private ProcessManager processManager;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public ClientWorker(Socket client, ProcessManager mgr) {
		this.clientSocket = client;
		this.processManager = mgr;
	}

	@Override
	public void run() {
		try {
			in = new ObjectInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			System.out.println("Read Error");
			e.printStackTrace();
		}
		try {
			out = new ObjectOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Write Error");
			e.printStackTrace();
		}
		try {
			String cmd = (String) in.readObject();
			processManager.launch(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
