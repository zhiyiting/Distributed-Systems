package util;

import java.net.Socket;

public class CoordDispatcher extends Dispatcher {

	private JobTracker tracker;
	private Socket socket;
	
	public CoordDispatcher(Socket socket, JobTracker tracker) {
		super(socket);
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
			tracker.start(job);
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
			int slaveID = tracker.addSlave(socket.getInetAddress().getHostName());
			ret = new Message(String.valueOf(slaveID));
			break;
		case "idle": {
			WorkMessage msg = (WorkMessage)m;
			ret = new TaskMessage("todo");
			((TaskMessage) ret).setMapTask(tracker.assignMapTask(msg.getSlaveID(), msg.getMapSlot()));
			((TaskMessage) ret).setReduceTask(tracker.assignReduceTask(msg.getSlaveID(), msg.getReduceSlot()));
			break;
		}
		case "busy":
			ret = new Message("ACK");
			break;
		case "done": {
			WorkMessage msg = (WorkMessage)m;
			tracker.markDone(msg.getSlaveID(), msg.getFinishedTask());
			ret = new TaskMessage("todo");
			((TaskMessage) ret).setMapTask(tracker.assignMapTask(msg.getSlaveID(), msg.getMapSlot()));
			((TaskMessage) ret).setReduceTask(tracker.assignReduceTask(msg.getSlaveID(), msg.getReduceSlot()));
			break;
		}
		default:
			break;	
		}
		return ret;	
	}


}
