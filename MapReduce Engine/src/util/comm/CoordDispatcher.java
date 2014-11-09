package util.comm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import util.core.Job;
import util.core.JobTracker;

public class CoordDispatcher implements Runnable {

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean canRun;
	private JobTracker tracker;
	private Socket socket;

	public CoordDispatcher(Socket socket, JobTracker tracker) {
		try {
			this.in = new ObjectInputStream(socket.getInputStream());
			this.out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Dispatcher: fail to create socket");

		}
		canRun = true;
		this.socket = socket;
		this.tracker = tracker;
	}

	protected Message dispatch(Message m) {
		Message ret = null;
		String method = m.getContent();
		switch (method) {
		// from client
		case "start":
			Job job = ((JobMessage) m).getJob();
			tracker.startMap(job);
			ret = new Message("ACK");
			break;
		case "list":
			ret = new Message(tracker.toString());
			break;
		case "stop":
			tracker.stop();
			ret = new Message("Job stopped");
			break;
		// from slaves
		case "hi":
			int slaveID = tracker.addSlave(socket.getInetAddress()
					.getHostName());
			ret = new Message(String.valueOf(slaveID));
			System.out.println("Slave #" + slaveID + "("
					+ socket.getInetAddress().getHostName() + ") connected");
			break;
		case "idle": {
			WorkMessage msg = (WorkMessage) m;
			ret = new TaskMessage("todo");
			((TaskMessage) ret).setMapTask(tracker.assignMapTask(
					msg.getSlaveID(), msg.getMapSlot()));
			((TaskMessage) ret).setReduceTask(tracker.assignReduceTask(
					msg.getSlaveID(), msg.getReduceSlot()));
			break;
		}
		case "busy":
			ret = new Message("ACK");
			break;
		case "done": {
			WorkMessage msg = (WorkMessage) m;
			tracker.markDone(msg.getSlaveID(), msg.getFinishedTask());
			ret = new TaskMessage("todo");
			((TaskMessage) ret).setMapTask(tracker.assignMapTask(
					msg.getSlaveID(), msg.getMapSlot()));
			((TaskMessage) ret).setReduceTask(tracker.assignReduceTask(
					msg.getSlaveID(), msg.getReduceSlot()));
			break;
		}
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
