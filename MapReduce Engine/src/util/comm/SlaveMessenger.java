package util.comm;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;

import util.core.TaskTracker;
import conf.Configuration;

/**
 * SlaveMessenger class to heart-beat messages to the master
 * 
 * @author zhiyiting
 *
 */
public class SlaveMessenger implements Runnable {

	private TaskTracker tracker;
	private CommModule commModule;
	private int sleepInterval;
	private String toHost;
	private int toPort;
	private int slaveID;
	private int retryCount;
	private int retryNum;

	/**
	 * Constructor to set the necessary fields
	 * 
	 * @param tracker
	 */
	public SlaveMessenger(TaskTracker tracker) {
		this.tracker = tracker;
		this.commModule = new CommModule();
		this.toHost = Configuration.MASTER_ADDRESS;
		this.toPort = Configuration.SERVER_PORT;
		this.sleepInterval = Configuration.HEART_BEAT_INTERVAL;
		this.retryCount = 0;
		this.retryNum = Configuration.RETRY_NUM;
		this.slaveID = -1;
		// register the slave to the master as the first message
		registerSlave();
	}

	/**
	 * Register itself to the master
	 */
	private void registerSlave() {
		// Compose and transmit a hi message
		Message msg = new Message("hi", toHost, toPort);
		try {
			Message ret = commModule.send(msg);
			if (ret != null && ret.getContent() != null) {
				// get its id from the master
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

	/**
	 * Dispatch the return message from master
	 * 
	 * @param message
	 */
	private void dispatch(Message msg) {
		// If no message is received, retry or abort
		if (msg == null) {
			System.out.println("Coordinator died... retry in " + sleepInterval
					/ 1000 + " seconds...");
		}
		String method = msg.getContent();
		switch (method) {
		case "todo":
			// Get the task message from master, do new tasks
			TaskMessage m = (TaskMessage) msg;
			tracker.addMapTask(m.getMapTask());
			tracker.addReduceTask(m.getReduceTask());
			break;
		case "slave":
			// Get the slave message from the master
			// Parse and get slave information
			ShowSlaveMessage mm = (ShowSlaveMessage) msg;
			tracker.setSlaveList(mm.getSlaveList());
			break;
		default:
			break;
		}
	}

	/**
	 * Heart-beat to the master with given interval
	 */
	@Override
	public void run() {
		while (true) {
			try {
				Message ret = null;
				int idleMapSlot = tracker.getIdleMapSlot();
				int idleReduceSlot = tracker.getIdleReduceSlot();
				// if there are slots for new tasks, ask the master for new task
				if (idleMapSlot > 0 || idleReduceSlot > 0) {
					WorkMessage msg = new WorkMessage("idle", toHost, toPort);
					msg.setFinishedTask(tracker.getFinishedTasks());
					msg.setMapSlot(idleMapSlot);
					msg.setReduceSlot(idleReduceSlot);
					msg.setSlaveID(slaveID);
					ret = commModule.send(msg);
				} else {
					// if there is no available slots, tell the master too
					WorkMessage msg = new WorkMessage("busy", toHost, toPort);
					msg.setSlaveID(slaveID);
					ret = commModule.send(msg);
				}
				dispatch(ret);
				// produce partitions it generated from map tasks
				HashMap<Integer, HashMap<Integer, ArrayDeque<String[]>>> partitions = tracker
						.getPartition();
				if (partitions.size() > 0) {
					// get current working slaves information
					WorkMessage msg = new WorkMessage("slave", toHost, toPort);
					msg.setSlaveID(slaveID);
					ret = commModule.send(msg);
					dispatch(ret);
					HashMap<Integer, String> slaveList = tracker.getSlaveList();
					// send the partition information to corresponding slaves
					for (Entry<Integer, HashMap<Integer, ArrayDeque<String[]>>> item : partitions
							.entrySet()) {
						int jobID = item.getKey();
						HashMap<Integer, ArrayDeque<String[]>> slave = item
								.getValue();
						for (Entry<Integer, ArrayDeque<String[]>> par : slave
								.entrySet()) {
							// get the slave ID and send the partitions
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
				// wait for some interval until the next heart beat
				Thread.sleep(sleepInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				retryCount++;
				// recognizing the master is not there, it the slave aborts
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
