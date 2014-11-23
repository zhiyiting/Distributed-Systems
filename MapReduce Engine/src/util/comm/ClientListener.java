package util.comm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ClientListener listens for messages transmitted to client
 * 
 * @author zhiyiting
 *
 */
public class ClientListener implements Runnable {

	private ServerSocket serverSocket;

	/**
	 * Listens at a given port number
	 * 
	 * @param port
	 */
	public ClientListener(int port) {
		try {
			this.serverSocket = new ServerSocket(port);
			System.out.println("listening on port " + port);
		} catch (IOException e) {
			System.out.println("Cannot listen on port " + port);
			e.printStackTrace();
		}
	}

	/**
	 * The listener will always listen to the master reporting job status
	 */
	@Override
	public void run() {
		while (true) {
			try {
				// wait and receive an incoming message
				Socket socket = serverSocket.accept();
				ObjectInputStream in = new ObjectInputStream(
						socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(
						socket.getOutputStream());
				Message input = (Message) in.readObject();
				// parse the message and generate output
				System.out.println("Generated output path:");
				System.out.println(input.getContent());
				out.writeObject("ACK");
				out.flush();
				out.close();
				in.close();
				System.exit(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
