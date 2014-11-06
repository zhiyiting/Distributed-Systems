package util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import conf.Configuration;

public class JobTracker {
	
	private Map<Integer, String> slaveList;

	private int slaveID = 0;
	private int jobID = 0;
	
	public JobTracker() {
		CoordListener listener = new CoordListener(Configuration.SERVER_PORT, this);
		Thread t = new Thread(listener);
		t.setDaemon(false);
		t.start();
		slaveList = new HashMap<Integer, String>();
	}
	
	public void start(Job job) {
		job.setID(getJobID());
		List<Task> mapTasks = FileSplit(job);
		
	}
	
	public void addSlave(String s) {
		slaveList.put(getSlaveID(), s);
	}

	public void list() {

	}
	
	public void stop() {
		
	}
	
	@Override
	public String toString() {
		return null;
	}
	
	private int getJobID() {
		jobID++;
		return jobID;
	}
	
	private int getSlaveID() {
		slaveID++;
		return slaveID;
	}
}
