package util;

import conf.Configuration;

public class SlaveMessenger implements Runnable {
	
	private TaskTracker tracker;
	private CommModule commModule;
	private int sleepInterval;
	private String toHost;
	private int toPort;
	private Message pingMessage;
	
	public SlaveMessenger(TaskTracker tracker) {
		this.tracker = tracker;
		this.commModule = new CommModule();
		this.toHost = Configuration.MASTER_ADDRESS;
		this.toPort = Configuration.SERVER_PORT;
		this.sleepInterval = Configuration.HEART_BEAT_INTERVAL;
		this.pingMessage = new Message("ping", toHost, toPort);
	}

	@Override
	public void run() {
		while (true) {
			try {
				Object ret = null;
				if (tracker.hasFinishedTasks()) {
					WorkMessage msg = new WorkMessage("done", toHost, toPort);
					msg.setFinishedTask(tracker.getFinishedTaskID());
					msg.setMapSlot(tracker.getIdleMapSlot());
					msg.setReduceSlot(tracker.getIdleReduceSlot());
					ret = commModule.send(msg);
				}
				else {
					ret = commModule.send(pingMessage);
				}
				if (ret == null) {
					System.out.println("Coordinator died... retry in " + sleepInterval + " seconds...");
				}
				
				Thread.sleep(sleepInterval);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				System.out.println("Coordinator died... retry in " + sleepInterval + " seconds...");
				e.printStackTrace();
				continue;
			}
		}
		
	}

}
