package util.comm;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;

import util.core.TaskTracker;
import conf.Configuration;

public class SlaveMessenger implements Runnable {

	private TaskTracker tracker;
	private CommModule commModule;
	private int sleepInterval;
	private String toHost;
	private int toPort;
	private int slaveID;
	private int retryCount;
	private int retryNum;

	public SlaveMessenger(TaskTracker tracker) {
		this.tracker = tracker;
		this.commModule = new CommModule();
		this.toHost = Configuration.MASTER_ADDRESS;
		this.toPort = Configuration.SERVER_PORT;
		this.sleepInterval = Configuration.HEART_BEAT_INTERVAL;
		this.retryCount = 0;
		this.retryNum = Configuration.RETRY_NUM;
		this.slaveID = -1;
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
		if (msg == null) {
			System.out.println("Coordinator died... retry in " + sleepInterval
					/ 1000 + " seconds...");
		}
		String method = msg.getContent();
		switch (method) {
		case "todo":
			TaskMessage m = (TaskMessage) msg;
			tracker.addMapTask(m.getMapTask());
			tracker.addReduceTask(m.getReduceTask());
			break;
		case "slave":
			ShowSlaveMessage mm = (ShowSlaveMessage) msg;
			tracker.setSlaveList(mm.getSlaveList());
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
				dispatch(ret);

				HashMap<Integer, HashMap<Integer, ArrayDeque<String[]>>> partitions = tracker
						.getPartition();
				if (partitions.size() > 0) {
					Message msg = new Message("slave", toHost, toPort);
					ret = commModule.send(msg);
					dispatch(ret);
					HashMap<Integer, String> slaveList = tracker.getSlaveList();
					for (Entry<Integer, HashMap<Integer, ArrayDeque<String[]>>> item : partitions
							.entrySet()) {
						int jobID = item.getKey();
						HashMap<Integer, ArrayDeque<String[]>> slave = item
								.getValue();
						for (Entry<Integer, ArrayDeque<String[]>> par : slave
								.entrySet()) {
							int slaveID = par.getKey();
							ArrayDeque<String[]> partition = par.getValue();
							String host = slaveList.get(slaveID);
							PartitionMessage pm = new PartitionMessage(
									"partition", host, toPort);
							pm.setPartition(jobID, partition);
							ret = commModule.send(pm);
						}
					}
				}
				Thread.sleep(sleepInterval);

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				retryCount++;
				if (retryCount > retryNum) {
					System.out
							.println("Coordinator not responding. Job termitated");
					System.exit(-1);
				}
				System.out.println("Coordinator not responding. Retry in "
						+ sleepInterval / 1000 + " seconds... (attempt "
						+ retryCount + "/" + retryNum + ")");
			}
		}

	}
}
