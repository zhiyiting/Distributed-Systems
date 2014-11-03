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
	private Listener listener;
	private boolean canRun;

	/**
	 * Constructor that keeps track of input/output stream 
	 * @param socket
	 * @param server listener
	 */
	public Dispatcher(Socket socket, Listener l) {
		try {
			this.in = new ObjectInputStream(socket.getInputStream());
			this.out = new ObjectOutputStream(socket.getOutputStream());
			this.listener = l;
		} catch (IOException e) {
			System.out.println("Dispatcher: fail to create socket");

		}
		canRun = true;
	}

	/**
	 * unmarshall method name and arguments from message, and start the method
	 * invocation
	 * 
	 * @param invocation
	 *            message
	 * @return return value
	 */
	private Object dispatch(Message m) {
		Object ret = null;
		if (m == null) {
			System.out.println("Message is empty");
			return ret;
		}
		// get service name from message and get corresponding remote object
		String method = m.getMethod();
		switch (method) {
		case "start":
			break;
		}
		return ret;

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
				// invoke the message locally and get return value
				Object returnVal = dispatch(m);
				// compose the return message
				Message ret = new Message(returnVal);
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