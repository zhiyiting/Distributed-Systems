package util.comm;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import util.core.TaskTracker;
import util.dfs.DFSClient;
import conf.Configuration;

public class SlaveMessenger implements Runnable {

	private TaskTracker tracker;
	private CommModule commModule;
	private int sleepInterval;
	private String toHost;
	private int toPort;
	private int slaveID;
	private DFSClient dfsClient;

	public SlaveMessenger(TaskTracker tracker) {
		this.tracker = tracker;
		this.commModule = new CommModule();
		this.toHost = Configuration.MASTER_ADDRESS;
		this.toPort = Configuration.SERVER_PORT;
		this.sleepInterval = Configuration.HEART_BEAT_INTERVAL;
		this.slaveID = -1;
		this.dfsClient = new DFSClient();
		registerSlave();
	}

	private void registerSlave() {
		Message msg = new Message("hi", toHost, toPort);
		try {
			Message ret = commModule.send(msg);
			if (ret != null && ret.getContent() != null) {
				slaveID = Integer.parseInt(ret.getContent());
				System.out.println("Slave #" + slaveID
						+ ": connected to master at " + toHost);
			} else {
				System.out.println("Master isn't there");
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void dispatch(Message msg) {
		String method = msg.getContent();
		switch (method) {
		case "todo":
			TaskMessage m = (TaskMessage) msg;
			tracker.addMapTask(m.getMapTask());
			tracker.addReduceTask(m.getReduceTask());
			break;
		default:
			break;
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Message ret = null;
				int idleMapSlot = tracker.getIdleMapSlot();
				int idleReduceSlot = tracker.getIdleReduceSlot();
				if (idleMapSlot > 0 || idleReduceSlot > 0) {
					WorkMessage msg = new WorkMessage("idle", toHost, toPort);
					msg.setFinishedTask(tracker.getFinishedTasks());
					msg.setMapSlot(idleMapSlot);
					msg.setReduceSlot(idleReduceSlot);
					msg.setSlaveID(slaveID);
					ret = commModule.send(msg);
				} else {
					Message msg = new Message("busy", toHost, toPort);
					ret = commModule.send(msg);
				}
				if (ret == null) {
					System.out.println("Coordinator died... retry in "
							+ sleepInterval / 1000 + " seconds...");
				} else {
					dispatch(ret);
				}

				Thread.sleep(sleepInterval);

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				System.out.println("Coordinator died... retry in "
						+ sleepInterval + " seconds...");
				e.printStackTrace();
				continue;
			}
		}

	}

}
