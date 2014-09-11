import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Listener extends Thread {
	private int port;
	public ServerSocket serverSocket;
	private ProcessManager processManager;

	public Listener(int p, ProcessManager mgr) {
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
		if (processManager.isMaster) {
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					ObjectInputStream in = new ObjectInputStream(
							socket.getInputStream());
					ObjectOutputStream out = new ObjectOutputStream(
							socket.getOutputStream());
					Object o = in.readObject();
					if (o != null) {
						String message = o.toString();
						if (message.contains("connect")) {
							String[] ip = message.split(":");
							processManager.addSlave(ip[1] + ":" + ip[2]);
							out.writeObject("connected");
							out.flush();
						}
					}
					in.close();
					out.close();
				} catch (IOException e) {
					System.out.println("IOException");
				} catch (ClassNotFoundException e) {
					System.out.println("Class Not Found");
				}
			}
		}
		else {
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					ObjectInputStream in = new ObjectInputStream(
							socket.getInputStream());
					ObjectOutputStream out = new ObjectOutputStream(
							socket.getOutputStream());
					Object o = in.readObject();
					System.out.println(o);
					if (o != null) {
						String message = o.toString();
						if (message.equals("Are you there?")) {
							
							out.writeObject("Yes");
							out.flush();
						}
					}
					in.close();
					out.close();
				} catch (IOException e) {
					System.out.println("IOException");
				} catch (ClassNotFoundException e) {
					System.out.println("Class Not Found");
				}
			}
		}

	}
}
