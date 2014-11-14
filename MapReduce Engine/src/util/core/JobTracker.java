package util.core;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import util.comm.CoordListener;
import util.dfs.DFSMaster;
import conf.Configuration;

public class JobTracker {

	private Map<Integer, Job> jobList;
	private Map<Integer, ArrayDeque<MapTask>> slaveToMapTaskList;
	private Map<Integer, ArrayDeque<ReduceTask>> slaveToReduceTaskList;
	private Map<Integer, HashSet<MapTask>> jobToMapTask;
	private Map<Integer, HashSet<ReduceTask>> jobToReduceTask;

	private DFSMaster dfs;

	private int slaveID = 0;
	private int jobID = 0;
	private int taskID = 0;

	public JobTracker() {
		CoordListener listener = new CoordListener(Configuration.SERVER_PORT,
				this);
		Thread t = new Thread(listener);
		t.setDaemon(false);
		t.start();
		jobList = new ConcurrentHashMap<Integer, Job>();
		slaveToMapTaskList = new ConcurrentHashMap<Integer, ArrayDeque<MapTask>>();
		slaveToReduceTaskList = new ConcurrentHashMap<Integer, ArrayDeque<ReduceTask>>();
		jobToMapTask = new ConcurrentHashMap<Integer, HashSet<MapTask>>();
		jobToReduceTask = new ConcurrentHashMap<Integer, HashSet<ReduceTask>>();

		dfs = new DFSMaster(this);
	}

	public void submitMapJob(Job job) {
		job.setID(getJobID());
		job.setOutputPath("Map_" + job.getId() + "/");
		synchronized (jobList) {
			jobList.put(jobID, job);
		}
		// create and distribute splits
		dfs.distributeFile(Configuration.INPUT_DIR, Configuration.REPLICA, job);
	}
	
	private synchronized void submitReduceJob(Job job) {
	}

	public synchronized ArrayDeque<MapTask> assignMapTask(int slaveID,
			int taskNum) {
		ArrayDeque<MapTask> tasks = new ArrayDeque<MapTask>();
		ArrayDeque<MapTask> potential = slaveToMapTaskList.get(slaveID);
		for (int i = 0; i < taskNum; i++) {
			MapTask task = potential.pollFirst();
			if (task == null)
				break;
			tasks.add(task);
		}
		return tasks;
	}

	public synchronized ArrayDeque<ReduceTask> assignReduceTask(int slaveID,
			int taskNum) {
		ArrayDeque<ReduceTask> tasks = new ArrayDeque<ReduceTask>();
		ArrayDeque<ReduceTask> potential = slaveToReduceTaskList.get(slaveID);
		for (int i = 0; i < taskNum; i++) {
			ReduceTask task = potential.pollFirst();
			if (task == null)
				break;
			tasks.add(task);
		}
		return tasks;
	}

	public synchronized void markDone(int slaveID, ArrayDeque<Task> finishedTask) {
		for (Task task : finishedTask) {
			int jobID = task.getJob().getId();
			// if the job is in mapping process
			if (jobToMapTask.containsKey(jobID)) {
				HashSet<MapTask> hs = jobToMapTask.get(jobID);
				hs.remove(task);
				if (hs.isEmpty()) {
					// start reduce for this job
					jobToMapTask.remove(jobID);
					submitReduceJob(task.getJob());
				}
			}
			// if the job is in reducing process
			else if (jobToReduceTask.containsKey(jobID)) {
				HashSet<ReduceTask> hs = jobToReduceTask.get(jobID);
				hs.remove(task);
				if (hs.isEmpty()) {
					// current job is officially finished
					jobToReduceTask.remove(jobID);
					// report finish~~
				}
			}
		}

	}

	public int addSlave(String host) {
		dfs.addSlave(getSlaveID(), host);
		ArrayDeque<MapTask> hs = new ArrayDeque<MapTask>();
		slaveToMapTaskList.put(slaveID, hs);
		return slaveID;
	}

	public void addQueuedMapTask(int n, MapTask task) {
		task.setTaskID(getTaskID());
		ArrayDeque<MapTask> taskList = slaveToMapTaskList.get(n);
		taskList.push(task);
		int jobID = task.getJob().getId();
		HashSet<MapTask> tasks = jobToMapTask.get(jobID);
		if (tasks == null) {
			tasks = new HashSet<MapTask>();
		}
		tasks.add(task);
	}

	public void list() {

	}

	public void stop() {

	}

	@Override
	public String toString() {
		return null;
	}

	private synchronized int getJobID() {
		jobID++;
		return jobID;
	}

	private synchronized int getSlaveID() {
		slaveID++;
		return slaveID;
	}

	private synchronized int getTaskID() {
		taskID++;
		return taskID;
	}
}
