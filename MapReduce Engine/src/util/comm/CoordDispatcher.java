package util.comm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayDeque;

import util.core.Job;
import util.core.JobTracker;
import util.core.MapTask;
import util.core.ReduceTask;

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

	private Message dispatch(Message m) {
		Message ret = null;
		String method = m.getContent();
		switch (method) {
		// from client
		case "start":
			Job job = ((JobMessage) m).getJob();
			tracker.submitMapJob(job);
			ret = new Message("Job #" + job.getId() + " " + job.getName()
					+ " started");
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
		case "idle":
			WorkMessage msg = (WorkMessage) m;
			int mapSlot = msg.getMapSlot();
			int reduceSlot = msg.getReduceSlot();
			if (mapSlot == 0 && reduceSlot == 0) {
				ret = new Message("ACK");
			} else {
				int i = msg.getSlaveID();
				ArrayDeque<MapTask> maps = tracker.assignMapTask(i, mapSlot);
				ArrayDeque<ReduceTask> reduces = tracker.assignReduceTask(i,
						reduceSlot);
				if (maps.size() == 0 && reduces.size() == 0) {
					ret = new Message("ACK");
				} else {
					ret = new TaskMessage("todo");
					((TaskMessage) ret).setMapTask(maps);
					((TaskMessage) ret).setReduceTask(reduces);
				}
			}
			tracker.markDone(msg.getFinishedTask());
			break;
		case "busy":
			ret = new Message("ACK");
			break;
		case "slave":
			ret = new ShowSlaveMessage("slave");
			((ShowSlaveMessage)ret).setSlaveList(tracker.getSlaveList());
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
