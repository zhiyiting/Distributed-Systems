package util.comm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import util.dfs.DFSClient;

public class SlaveDispatcher implements Runnable {
	
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean canRun;
	private Socket socket;
	private DFSClient dfs;
	
	public SlaveDispatcher(Socket socket, DFSClient dfs) {
		try {
			this.in = new ObjectInputStream(socket.getInputStream());
			this.out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Dispatcher: fail to create socket");

		}
		canRun = true;
		this.socket = socket;
		this.dfs = dfs;
	}

	private Message dispatch(Message m) {
		Message ret = null;
		String method = m.getContent();
		switch (method) {
		// from client
		case "distribute":
			DFSMessage msg = (DFSMessage) m;
			dfs.createFile(msg.getContent(), msg.getFilename());
			ret = new Message("ACK");
			break;
		default:
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
				// dispatch the message
				Message ret = dispatch(m);
				// compose the return message
				out.writeObject(ret);
				out.flush();
			} catch (IOException e) {
				break;
			} catch (ClassNotFoundException e) {
				System.out.println("CoordDispatcher: Class Not Found Exception"
						+ e.getMessage());

			}
		}
	}

	public void stop() {
		canRun = false;
	}
}
