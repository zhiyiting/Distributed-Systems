package util;


import conf.Configuration;

public class SlaveMessenger implements Runnable {
	
	private TaskTracker tracker;
	private CommModule commModule;
	private int sleepInterval;
	private String toHost;
	private int toPort;
	
	public SlaveMessenger(TaskTracker tracker) {
		this.tracker = tracker;
		this.commModule = new CommModule();
		this.toHost = Configuration.MASTER_ADDRESS;
		this.toPort = Configuration.SERVER_PORT;
		this.sleepInterval = Configuration.HEART_BEAT_INTERVAL;
		registerSlave();
	}
	
	private void registerSlave() {
		Message msg = new Message("hi", toHost, toPort);
		try {
			Message ret = commModule.send(msg);
			if (ret.getContent().equals("ACK")) {
				System.out.println("Connected to master at " + toHost);
			}
			else {
				System.out.println("Master isn't there");
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void dispatch(Message msg) {
		String method = msg.getContent();
		switch (method) {
		case "todo":
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
				if (tracker.hasFinishedTasks()) {
					WorkMessage msg = new WorkMessage("done", toHost, toPort);
					msg.setFinishedTask(tracker.getFinishedTaskID());
					msg.setMapSlot(tracker.getIdleMapSlot());
					msg.setReduceSlot(tracker.getIdleReduceSlot());
					ret = commModule.send(msg);
				}
				else if (tracker.getIdleMapSlot() > 0 || tracker.getIdleReduceSlot() > 0){
					WorkMessage msg = new WorkMessage("idle", toHost, toPort);
					msg.setMapSlot(tracker.getIdleMapSlot());
					msg.setReduceSlot(tracker.getIdleReduceSlot());
					ret = commModule.send(msg);
				}
				else {
					Message msg = new Message("busy", toHost, toPort);
					ret = commModule.send(msg);
				}
				if (ret == null) {
					System.out.println("Coordinator died... retry in " + sleepInterval + " seconds...");
				}
				else {
					dispatch(ret);
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
