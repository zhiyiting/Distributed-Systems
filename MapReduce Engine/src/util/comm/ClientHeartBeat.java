package util.comm;

import util.core.Job;
import conf.Configuration;

public class ClientHeartBeat implements Runnable {

	private int sleepInterval;
	private int retryCount;
	private int retryNum;
	private String toHost;
	private int toPort;
	private CommModule commModule;
	private Job job;

	public ClientHeartBeat(Job job) {
		this.sleepInterval = Configuration.HEART_BEAT_INTERVAL;
		this.retryCount = 0;
		this.retryNum = Configuration.RETRY_NUM;
		this.commModule = new CommModule();
		this.job = job;
		this.toHost = Configuration.MASTER_ADDRESS;
		this.toPort = Configuration.SERVER_PORT;
	}

	/**
	 * Heart-beat to the master with given interval
	 */
	@Override
	public void run() {
		while (true) {
			try {
				JobMessage msg = new JobMessage("status", job, toHost, toPort);
				Message ret = commModule.send(msg);
				System.out.println(ret.getContent());
				// wait for some interval until the next heart beat
				Thread.sleep(sleepInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				retryCount++;
				// recognizing the master is not there, it the slave aborts
				if (retryCount > retryNum) {
					System.out
							.println("Coordinator not responding. Job Fail");
					System.exit(-1);
				}
				System.out.println("Coordinator not responding. Retry in "
						+ sleepInterval / 1000 + " seconds... (attempt "
						+ retryCount + "/" + retryNum + ")");
			}
		}

	}
}
