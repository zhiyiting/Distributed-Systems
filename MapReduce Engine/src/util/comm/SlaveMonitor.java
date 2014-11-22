package util.comm;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import util.core.JobTracker;

/**
 * Slave Monitor class to monitor the current health of the slaves
 * 
 * @author zhiyiting
 *
 */
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

	/**
	 * Reset the responding time from the slave
	 * 
	 * @param id
	 */
	public void resetSlave(int id) {
		slaveList.put(id, 0);
	}

	/**
	 * Heart-beat and track the status of the slaves
	 */
	@Override
	public void run() {
		System.out.println("Slave monitor ran");
		while (true) {
			try {
				for (Iterator<ConcurrentHashMap.Entry<Integer, Integer>> it = slaveList
						.entrySet().iterator(); it.hasNext();) {
					Entry<Integer, Integer> slave = it.next();
					int curRetry = slave.getValue();
					// if the retry count exceeds the limit, define the slave as
					// unreachable
					if (curRetry > retryNum * 2) {
						System.out.println("Slave #" + slave.getKey()
								+ " has left");
						// report to job tracker that the slave is unreachable
						tracker.loseContact(slave.getKey());
						// remove the slave from the job tracker
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
