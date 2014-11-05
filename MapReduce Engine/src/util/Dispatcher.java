package util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Dispatcher class instantiates threads for dispatching functions
 * 
 * @author zhiyiting
 *
 */
public class Dispatcher implements Runnable {

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean canRun;

	/**
	 * Constructor that keeps track of input/output stream 
	 * @param socket
	 * @param server listener
	 */
	public Dispatcher(Socket socket) {
		try {
			this.in = new ObjectInputStream(socket.getInputStream());
			this.out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Dispatcher: fail to create socket");

		}
		canRun = true;
	}

	/**
	 * unmarshall incoming message
	 * 
	 * @param message
	 * @return return value
	 */
	Message dispatch(Message m) {
		return null;
	}

	/**
	 * Start a thread and reuse current input/output stream
	 */
	@Override
	public void run() {
		while (canRun) {
			try {
				// read the incoming message
				Message m = (Message) in.readObject();
				// dispatch the message
				Message ret = dispatch(m);
				// compose the return message
				out.writeObject(ret);
				out.flush();
			} catch (IOException e) {
				break;
			} catch (ClassNotFoundException e) {
				System.out.println("RegistryWorker: Class Not Found Exception"
						+ e.getMessage());

			}
		}
	}
	
	public void stop() {
		canRun = false;
	}

}