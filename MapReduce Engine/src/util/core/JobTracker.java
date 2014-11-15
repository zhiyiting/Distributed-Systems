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
	private HashSet<Task> assignedTask;

	private DFSMaster dfs;

	private int slaveID = 0;
	private int jobID = 0;

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
		assignedTask = new HashSet<Task>();

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
		System.out.println("start reduce");
	}

	public synchronized ArrayDeque<MapTask> assignMapTask(int slaveID,
			int taskNum) {
		ArrayDeque<MapTask> tasks = new ArrayDeque<MapTask>();
		ArrayDeque<MapTask> potential = slaveToMapTaskList.get(slaveID);
		if (potential != null && potential.size() > 0) {
			for (int i = 0; i < taskNum; i++) {
				MapTask task;
				do {
					task = potential.pollFirst();
					if (task == null) break;
				} while (assignedTask.contains(task));
				if (task == null) {
					break;
				}
				tasks.add(task);
				assignedTask.add(task);
			}
		}
		return tasks;
	}

	public synchronized ArrayDeque<ReduceTask> assignReduceTask(int slaveID,
			int taskNum) {
		ArrayDeque<ReduceTask> tasks = new ArrayDeque<ReduceTask>();
		ArrayDeque<ReduceTask> potential = slaveToReduceTaskList.get(slaveID);
		if (potential != null && potential.size() > 0) {
			for (int i = 0; i < taskNum; i++) {
				ReduceTask task = null;
				do {
					task = potential.pollFirst();
					if (task == null) break;
				} while (assignedTask.contains(task));
				if (task == null) {
					break;
				}
				tasks.add(task);
				assignedTask.add(task);
			}
		}
		return tasks;
	}

	public synchronized void markDone(ArrayDeque<Task> finishedTask) {
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
				jobToMapTask.put(jobID, hs);
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

	public synchronized int addSlave(String host) {
		dfs.addSlave(getSlaveID(), host);
		ArrayDeque<MapTask> hs = new ArrayDeque<MapTask>();
		slaveToMapTaskList.put(slaveID, hs);
		return slaveID;
	}

	public synchronized void addQueuedMapTask(int n, MapTask task) {
		ArrayDeque<MapTask> taskList = slaveToMapTaskList.get(n);
		taskList.push(task);
		slaveToMapTaskList.put(n, taskList);
		int jobID = task.getJob().getId();
		HashSet<MapTask> tasks = jobToMapTask.get(jobID);
		if (tasks == null) {
			tasks = new HashSet<MapTask>();
		}
		tasks.add(task);
		jobToMapTask.put(jobID, tasks);
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
}
