package util.core;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import util.comm.CommModule;
import util.comm.CoordListener;
import util.comm.Message;
import util.dfs.DFSMaster;
import conf.Configuration;

public class JobTracker {

	private Map<Integer, Job> jobList;
	private Map<Integer, String> jobClient;
	private Map<Integer, ArrayDeque<String>> jobOutput;
	private Map<Integer, ArrayDeque<MapTask>> slaveMapTaskList;
	private Map<Integer, ArrayDeque<ReduceTask>> slaveReduceTaskList;
	private Map<Integer, HashSet<MapTask>> slaveRunningTask;
	private Map<Integer, HashSet<MapTask>> jobMapTask;
	private Map<Integer, HashSet<ReduceTask>> jobReduceTask;
	private Map<Integer, HashSet<Task>> jobAssignedTask;

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
		slaveMapTaskList = new ConcurrentHashMap<Integer, ArrayDeque<MapTask>>();
		slaveReduceTaskList = new ConcurrentHashMap<Integer, ArrayDeque<ReduceTask>>();
		slaveRunningTask = new ConcurrentHashMap<Integer, HashSet<MapTask>>();
		jobMapTask = new ConcurrentHashMap<Integer, HashSet<MapTask>>();
		jobReduceTask = new ConcurrentHashMap<Integer, HashSet<ReduceTask>>();
		jobAssignedTask = new ConcurrentHashMap<Integer, HashSet<Task>>();
		jobClient = new ConcurrentHashMap<Integer, String>();
		jobOutput = new ConcurrentHashMap<Integer, ArrayDeque<String>>();
		dfs = new DFSMaster(this);
	}

	public void submitMapJob(String host, Job job) {
		job.setID(getJobID());
		// create and distribute splits
		dfs.distributeFile(job.conf.INPUT_DIR, job.conf.REPLICA, job);
		System.out.println("Start Job #" + job.getId());
		synchronized (jobList) {
			jobList.put(job.getId(), job);
		}
		synchronized (jobClient) {
			jobClient.put(job.getId(), host);
		}
		System.out.println("Start mapping job #" + job.getId());
	}

	private synchronized void submitReduceJob(Job job) {
		System.out.println("Start reducing job #" + job.getId());
		int reducerNum = job.conf.REDUCER_NUM;
		int taskID = 0;
		for (int i = 1; i <= reducerNum; i++) {
			ReduceTask task = new ReduceTask();
			task.setJob(job);
			task.setSlaveID(i);
			task.setTaskID(taskID);
			ArrayDeque<ReduceTask> tasks = new ArrayDeque<ReduceTask>();
			tasks.push(task);
			slaveReduceTaskList.put(i, tasks);
			HashSet<ReduceTask> hs = jobReduceTask.get(job.getId());
			if (hs == null) {
				hs = new HashSet<ReduceTask>();
			}
			hs.add(task);
			jobReduceTask.put(job.getId(), hs);
			taskID++;
		}
	}

	public synchronized ArrayDeque<MapTask> assignMapTask(int slaveID,
			int taskNum) {
		ArrayDeque<MapTask> tasks = new ArrayDeque<MapTask>();
		ArrayDeque<MapTask> potential = slaveMapTaskList.get(slaveID);
		if (potential != null && potential.size() > 0) {
			for (int i = 0; i < taskNum; i++) {
				MapTask task;
				int jobID = 0;
				HashSet<Task> t = null;
				do {
					task = potential.pollFirst();
					if (task == null)
						break;
					jobID = task.getJob().getId();
					t = jobAssignedTask.get(jobID);
					if (t == null)
						t = new HashSet<Task>();
				} while (t.contains(task));
				if (task == null) {
					break;
				}
				tasks.add(task);
				HashSet<MapTask> taskList = slaveRunningTask.get(slaveID);
				taskList.add(task);
				slaveRunningTask.put(slaveID, taskList);
				t.add(task);
				jobAssignedTask.put(jobID, t);
			}
		}
		return tasks;
	}

	public synchronized ArrayDeque<ReduceTask> assignReduceTask(int slaveID,
			int taskNum) {
		ArrayDeque<ReduceTask> tasks = new ArrayDeque<ReduceTask>();
		ArrayDeque<ReduceTask> potential = slaveReduceTaskList.get(slaveID);
		if (potential != null && potential.size() > 0) {
			for (int i = 0; i < taskNum; i++) {
				ReduceTask task;
				do {
					task = potential.pollFirst();
					if (task == null)
						break;
				} while (jobAssignedTask.get(task.getJob().getId()).contains(
						task));
				if (task == null) {
					break;
				}
				tasks.add(task);
				HashSet<Task> t = jobAssignedTask.get(jobID);
				int jobID = task.getJob().getId();
				t.add(task);
				jobAssignedTask.put(jobID, t);
			}
		}
		return tasks;
	}

	public synchronized void markDone(ArrayDeque<Task> finishedTask) {
		for (Task task : finishedTask) {
			int jobID = task.getJob().getId();
			// if the job is in mapping process
			if (task.getType() == 'M') {
				int slaveID = task.getSlaveID();
				HashSet<MapTask> running = slaveRunningTask.get(slaveID);
				running.remove(task);
				slaveRunningTask.put(slaveID, running);
				HashSet<MapTask> hs = jobMapTask.get(jobID);
				hs.remove(task);
				if (hs.isEmpty()) {
					// start reduce for this job
					jobMapTask.remove(jobID);
					System.out.println("Finished mapping job #" + jobID);
					submitReduceJob(task.getJob());
					return;
				}
				System.out.println("Map task remaining size: " + hs.size());
				jobMapTask.put(jobID, hs);
			}
			// if the job is in reducing process
			else if (task.getType() == 'R') {
				HashSet<ReduceTask> hs = jobReduceTask.get(jobID);
				hs.remove(task);
				String path = ((ReduceTask) task).getOutput();
				ArrayDeque<String> curOutput = jobOutput.get(jobID);
				if (curOutput == null) {
					curOutput = new ArrayDeque<String>();
				}
				curOutput.add(path);
				jobOutput.put(jobID, curOutput);
				System.out.println("Reducing output generated at " + path);
				if (hs.isEmpty()) {
					jobReduceTask.remove(jobID);
					System.out.println("Job #" + jobID + " finished.");
					String toHost = jobClient.get(jobID);
					StringBuilder sb = new StringBuilder();
					sb.append("Job #" + jobID + " finished.\n");
					sb.append("Generated output location on DFS: \n\t");
					ArrayDeque<String> outputs = jobOutput.get(jobID);
					for (String s : outputs) {
						sb.append(s);
						sb.append("\n\t");
					}
					Message msg = new Message(sb.toString(), toHost,
							Configuration.CLIENT_PORT);
					CommModule.send(msg, toHost, Configuration.CLIENT_PORT);
					//cleanUp(jobID);
				}
			} else {
				System.out.println("Invalid task type");
			}
		}
	}

	public synchronized int addSlave(String host) {
		dfs.addSlave(getSlaveID(), host);
		ArrayDeque<MapTask> hs = new ArrayDeque<MapTask>();
		slaveMapTaskList.put(slaveID, hs);
		HashSet<MapTask> t = new HashSet<MapTask>();
		slaveRunningTask.put(slaveID, t);
		return slaveID;
	}

	public synchronized void addQueuedMapTask(int n, MapTask task) {
		ArrayDeque<MapTask> taskList = slaveMapTaskList.get(n);
		taskList.push(task);
		slaveMapTaskList.put(n, taskList);
		int jobID = task.getJob().getId();
		HashSet<MapTask> tasks = jobMapTask.get(jobID);
		if (tasks == null) {
			tasks = new HashSet<MapTask>();
		}
		if (!tasks.contains(task)) {
			tasks.add(task);
			jobMapTask.put(jobID, tasks);
		}
	}

	public HashMap<Integer, String> getSlaveList() {
		return dfs.getSlaveList();
	}

	public synchronized void loseContact(int id) {
		slaveMapTaskList.remove(id);
		slaveReduceTaskList.remove(id);
		dfs.removeSlave(id);
		dfs.enforceReplication(id, slaveMapTaskList.get(id));
	}

	public synchronized ArrayDeque<MapTask> getRunningMapList(int id) {
		ArrayDeque<MapTask> list = new ArrayDeque<MapTask>();
		HashSet<MapTask> tasks = slaveRunningTask.get(id);
		Iterator<MapTask> it = tasks.iterator();
		while (it.hasNext()) {
			MapTask t = it.next();
			list.add(t);
			jobAssignedTask.get(t.getJob().getId()).remove(t);
		}
		tasks.clear();
		slaveRunningTask.put(id, tasks);
		return list;
	}

	private synchronized void cleanUp(int jobID) {
		HashSet<MapTask> m = new HashSet<MapTask>();
		jobMapTask.put(jobID, m);
		HashSet<ReduceTask> r = new HashSet<ReduceTask>();
		jobReduceTask.put(jobID, r);

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
	
	public void getRunningJobs() {
		for (Entry<Integer, ArrayDeque<MapTask>> t: slaveMapTaskList.entrySet()) {
			int slaveID = t.getKey();
			Iterator<MapTask> it = t.getValue().iterator();
			System.out.println("Slave " + slaveID);
			while (it.hasNext()) {
				MapTask task = it.next();
				System.out.println(task.getJob().getId() + "  " + task.getTaskID());
			}
		}
		
		for (Entry<Integer, HashSet<MapTask>> t: jobMapTask.entrySet()) {
			int jobID = t.getKey();
			Iterator<MapTask> it = t.getValue().iterator();
			System.out.println("Job " + jobID);
			while (it.hasNext()) {
				MapTask task = it.next();
				System.out.println(task.getTaskID());
			}
		}
	}
}
