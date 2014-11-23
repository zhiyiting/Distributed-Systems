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

/**
 * Dispatcher class at the master side
 * 
 * @author zhiyiting
 *
 */
public class CoordDispatcher implements Runnable {

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean canRun;
	private JobTracker tracker;
	private Socket socket;
	private SlaveMonitor slaveMonitor;

	public CoordDispatcher(Socket socket, JobTracker tracker,
			SlaveMonitor monitor) {
		try {
			this.in = new ObjectInputStream(socket.getInputStream());
			this.out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Dispatcher: fail to create socket");

		}
		canRun = true;
		this.socket = socket;
		this.tracker = tracker;
		this.slaveMonitor = monitor;
	}

	/**
	 * Function to dispatch message
	 * 
	 * @param message
	 * @return return message
	 */
	private synchronized Message dispatch(Message m) {
		Message ret = null;
		String method = m.getContent();
		switch (method) {
		// message from client
		case "start":
			Job job = ((JobMessage) m).getJob();
			// report to job tracker and start the job
			tracker.submitMapJob(socket.getInetAddress().getHostName(), job);
			ret = new Message(job.getId() + "");
			break;
		case "status":
			// return the percentage of the jobs
			int jobID = ((JobMessage) m).getJob().getId();
			ret = new Message(tracker.getPercent(jobID));
			break;
		// message from slaves
		case "hi":
			// first message from a slave; get the slaveID and return
			int slaveID = tracker.addSlave(socket.getInetAddress()
					.getHostName());
			ret = new Message(String.valueOf(slaveID));
			slaveMonitor.resetSlave(slaveID);
			System.out.println("Slave #" + slaveID + "("
					+ socket.getInetAddress().getHostName() + ") connected");
			break;
		case "idle":
			// slave reports its work status
			WorkMessage msg = (WorkMessage) m;
			// get slave's available slot for tasks
			int mapSlot = msg.getMapSlot();
			int reduceSlot = msg.getReduceSlot();
			if (mapSlot == 0 && reduceSlot == 0) {
				ret = new Message("ACK");
			} else {
				int i = msg.getSlaveID();
				// assign slaves with tasks
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
			// job tracker marks which tasks are finished
			tracker.markDone(msg.getFinishedTask());
			// monitor resets the respond time of the slave
			slaveMonitor.resetSlave(msg.getSlaveID());
			break;
		case "busy":
			// slave is busy; master do nothing
			ret = new Message("ACK");
			// monitor resets the respond time of the slave
			slaveMonitor.resetSlave(((WorkMessage) m).getSlaveID());
			break;
		case "slave":
			// slave asks for the current slave information
			ret = new ShowSlaveMessage("slave");
			slaveMonitor.resetSlave(((WorkMessage) m).getSlaveID());
			// return the current slave information
			((ShowSlaveMessage) ret).setSlaveList(tracker.getSlaveList());
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
