package util.comm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import util.dfs.DFSClient;

/**
 * Dispatcher class at the slave side; parse the incoming messages
 * 
 * @author zhiyiting
 *
 */
public class SlaveDispatcher implements Runnable {

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean canRun;
	private DFSClient dfs;

	public SlaveDispatcher(Socket socket, DFSClient dfs) {
		try {
			this.in = new ObjectInputStream(socket.getInputStream());
			this.out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Dispatcher: fail to create socket");

		}
		canRun = true;
		this.dfs = dfs;
	}

	/**
	 * Dispatch the incoming message
	 * 
	 * @param message
	 * @return reply message
	 */
	private Message dispatch(Message m) {
		Message ret = null;
		String method = m.getContent();
		switch (method) {
		case "distribute":
			// get and create distributed from another node
			DFSMessage msg = (DFSMessage) m;
			dfs.createFile(msg.getFile());
			ret = new Message("ACK");
			break;
		case "partition":
			// get and save the partition file from another node
			PartitionMessage pm = (PartitionMessage) m;
			dfs.addPartition(pm.getPartition());
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

	/**
	 * Stop the dispatcher
	 */
	public void stop() {
		canRun = false;
	}
}
