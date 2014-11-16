package util.core;

import java.util.ArrayDeque;
import java.util.HashMap;
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
		System.out.println("Start Job #" + job.getId());
		synchronized (jobList) {
			jobList.put(job.getId(), job);
		}
		System.out.println("Start mapping job #" + job.getId());
		// create and distribute splits
		dfs.distributeFile(Configuration.INPUT_DIR, Configuration.REPLICA, job);
	}

	private synchronized void submitReduceJob(Job job) {
		System.out.println("Start reducing job #" + job.getId());
		int reducerNum = Configuration.REDUCER_NUM;
		int taskID = 0;
		for (int i = 1; i <= reducerNum; i++) {
			ReduceTask task = new ReduceTask();
			task.setJob(job);
			task.setSlaveID(i);
			task.setTaskID(taskID);
			ArrayDeque<ReduceTask> tasks = new ArrayDeque<ReduceTask>();
			tasks.push(task);
			slaveToReduceTaskList.put(i, tasks);
			HashSet<ReduceTask> hs = jobToReduceTask.get(job.getId());
			if (hs == null) {
				hs = new HashSet<ReduceTask>();
			}
			hs.add(task);
			jobToReduceTask.put(job.getId(), hs);
			taskID++;
		}
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
					if (task == null)
						break;
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
				ReduceTask task;
				do {
					task = potential.pollFirst();
					if (task == null)
						break;
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
			if (task.getType() == 'M') {
				HashSet<MapTask> hs = jobToMapTask.get(jobID);
				hs.remove(task);
				if (hs.isEmpty()) {
					// start reduce for this job
					jobToMapTask.remove(jobID);
					System.out.println("Finished mapping job #" + jobID);
					submitReduceJob(task.getJob());
					return;
				}
				jobToMapTask.put(jobID, hs);
			}
			// if the job is in reducing process
			else if (task.getType() == 'R') {
				HashSet<ReduceTask> hs = jobToReduceTask.get(jobID);
				hs.remove(task);
				String path = ((ReduceTask) task).getOutput();
				System.out.println("Reducing output generated at " + path);
				if (hs.isEmpty()) {
					jobToReduceTask.remove(jobID);
					System.out
							.println("MapReduce job #" + jobID + " finished.");
				}
			} else {
				System.out.println("Invalid task type");
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

	public HashMap<Integer, String> getSlaveList() {
		return dfs.getSlaveList();
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
