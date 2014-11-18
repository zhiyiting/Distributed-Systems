package util.comm;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import util.core.JobTracker;

public class SlaveMonitor implements Runnable {

	private int interval;
	private int retryNum;
	private ConcurrentHashMap<Integer, Integer> slaveList;
	private JobTracker tracker;

	public SlaveMonitor(int n, int retryNum, JobTracker trk) {
		this.interval = n;
		this.retryNum = retryNum;
		this.tracker = trk;
		slaveList = new ConcurrentHashMap<Integer, Integer>();
	}

	public void resetSlave(int id) {
		slaveList.put(id, 0);
	}

	@Override
	public void run() {
		System.out.println("Slave monitor ran");
		while (true) {
			try {
				for (Iterator<ConcurrentHashMap.Entry<Integer, Integer>> it = slaveList
						.entrySet().iterator(); it.hasNext();) {
					Entry<Integer, Integer> slave = it.next();
					int curRetry = slave.getValue();
					if (curRetry > retryNum * 2) {
						System.out.println("Slave #" + slave.getKey()
								+ " has left");
						// report to job tracker that the slave is dead
						tracker.loseContact(slave.getKey());
						it.remove();
					} else {
						slaveList.put(slave.getKey(), ++curRetry);
					}
				}
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
