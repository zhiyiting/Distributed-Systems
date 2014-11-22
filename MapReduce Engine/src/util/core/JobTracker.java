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

/**
 * Job tracker class to coordinate and process the jobs submitted
 * 
 * @author zhiyiting
 *
 */
public class JobTracker {

	// map job id with job
	private Map<Integer, Job> jobList;
	// map job id to its submission client
	private Map<Integer, String> jobClient;
	// map job id to its output folder
	private Map<Integer, ArrayDeque<String>> jobOutput;
	// map slave id to list of map tasks to be run on this slave
	private Map<Integer, ArrayDeque<MapTask>> slaveMapTaskList;
	// map slave id to list of reduce tasks to be run on this slave
	private Map<Integer, ArrayDeque<ReduceTask>> slaveReduceTaskList;
	// map slave id to the task it is running
	private Map<Integer, HashSet<MapTask>> slaveRunningTask;
	// map job id to associated map tasks
	private Map<Integer, HashSet<MapTask>> jobMapTask;
	// map job id to associated reduce tasks
	private Map<Integer, HashSet<ReduceTask>> jobReduceTask;
	// map job id to associated tasks that has been assigned
	private Map<Integer, HashSet<Task>> jobAssignedTask;

	// dfs master node
	private DFSMaster dfs;

	private int slaveID = 0;
	private int jobID = 0;

	/**
	 * Constructor to initialize the job tracker
	 */
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

	/**
	 * get job id and distribute the input files
	 * 
	 * @param host
	 * @param job
	 */
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
		synchronized (jobAssignedTask) {
			HashSet<Task> hs = new HashSet<Task>();
			jobAssignedTask.put(job.getId(), hs);
		}
		System.out.println("Start mapping job #" + job.getId());
	}

	/**
	 * create reduce tasks
	 * 
	 * @param job
	 */
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

	/**
	 * Function to return a list of to do tasks for a given slave
	 * 
	 * @param slaveID
	 * @param taskNum
	 * @return map tasks to do
	 */
	public synchronized ArrayDeque<MapTask> assignMapTask(int slaveID,
			int taskNum) {
		ArrayDeque<MapTask> tasks = new ArrayDeque<MapTask>();
		ArrayDeque<MapTask> potential = slaveMapTaskList.get(slaveID);
		if (potential != null && potential.size() > 0) {
			for (int i = 0; i < taskNum; i++) {
				MapTask task;
				int jobID = 0;
				HashSet<Task> t = null;
				// check if the task has been assigned to other slaves
				do {
					task = potential.pollFirst();
					if (task == null)
						break;
					jobID = task.getJob().getId();
					t = jobAssignedTask.get(jobID);
				} while (t.contains(task));
				if (task == null) {
					break;
				}
				// add unassigned task to return value
				tasks.add(task);
				// add the task to slave to task running list
				HashSet<MapTask> taskList = slaveRunningTask.get(slaveID);
				taskList.add(task);
				slaveRunningTask.put(slaveID, taskList);
				// add the job and task to assigned list
				t.add(task);
				jobAssignedTask.put(jobID, t);
				// update the slave to map task list
				slaveMapTaskList.put(slaveID, potential);
			}
		}
		return tasks;
	}

	/**
	 * Function to return a list of to do reduce tasks for the slave
	 * 
	 * @param slaveID
	 * @param taskNum
	 * @return to do reduce task
	 */
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
				// update the lists
				tasks.add(task);
				HashSet<Task> t = jobAssignedTask.get(jobID);
				int jobID = task.getJob().getId();
				t.add(task);
				jobAssignedTask.put(jobID, t);
				slaveReduceTaskList.put(slaveID, potential);
			}
		}
		return tasks;
	}

	/**
	 * mark the tasks done and update the lists
	 * 
	 * @param finishedTask
	 */
	public synchronized void markDone(ArrayDeque<Task> finishedTask) {
		for (Task task : finishedTask) {
			int jobID = task.getJob().getId();
			// if the job is in mapping process
			if (task.getType() == 'M') {
				int slaveID = task.getSlaveID();
				// get the running tasks and remove the finished ones
				HashSet<MapTask> running = slaveRunningTask.get(slaveID);
				running.remove(task);
				slaveRunningTask.put(slaveID, running);
				// remove the task from job task list
				HashSet<MapTask> hs = jobMapTask.get(jobID);
				hs.remove(task);
				// check if there is remaining task
				if (hs.isEmpty()) {
					// start reduce for this job
					jobMapTask.remove(jobID);
					System.out.println("Finished mapping job #" + jobID);
					submitReduceJob(task.getJob());
					return;
				}
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
				// add the output path to the list
				curOutput.add(path);
				jobOutput.put(jobID, curOutput);
				System.out.println("Reducing output generated at " + path);
				// if all the reduce jobs finished, report to the client
				if (hs.isEmpty()) {
					jobReduceTask.remove(jobID);
					System.out.println("Job #" + jobID + " finished.");
					String toHost = jobClient.get(jobID);
					StringBuilder sb = new StringBuilder();
					ArrayDeque<String> outputs = jobOutput.get(jobID);
					for (String s : outputs) {
						sb.append(s);
						sb.append("\n\t");
					}
					Message msg = new Message(sb.toString(), toHost,
							Configuration.CLIENT_PORT);
					CommModule.send(msg, toHost, Configuration.CLIENT_PORT);
				}
			} else {
				System.out.println("Invalid task type");
			}
		}
	}

	/**
	 * Function to add slave node to the job tracker
	 * 
	 * @param host
	 * @return slave ID
	 */
	public synchronized int addSlave(String host) {
		// report the dfs node
		dfs.addSlave(getSlaveID(), host);
		// initialize the lists
		ArrayDeque<MapTask> hs = new ArrayDeque<MapTask>();
		slaveMapTaskList.put(slaveID, hs);
		HashSet<MapTask> t = new HashSet<MapTask>();
		slaveRunningTask.put(slaveID, t);
		return slaveID;
	}

	/**
	 * Function to add map tasks to the list
	 * 
	 * @param slave ID
	 * @param task
	 */
	public synchronized void addQueuedMapTask(int slaveID, MapTask task) {
		ArrayDeque<MapTask> taskList = slaveMapTaskList.get(slaveID);
		taskList.push(task);
		slaveMapTaskList.put(slaveID, taskList);
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

	/**
	 * Function to get active slave list
	 * 
	 * @return list of slaves
	 */
	public HashMap<Integer, String> getSlaveList() {
		return dfs.getSlaveList();
	}

	/**
	 * Function to report that a given slave is unreachable
	 * 
	 * @param id
	 */
	public synchronized void loseContact(int id) {
		slaveMapTaskList.remove(id);
		slaveReduceTaskList.remove(id);
		dfs.removeSlave(id);
		dfs.enforceReplication(id, slaveMapTaskList.get(id));
	}

	/**
	 * Function to get the running map tasks for replica enforcement
	 * 
	 * @param slave id
	 * @return task list
	 */
	public synchronized ArrayDeque<MapTask> getRunningMapList(int id) {
		ArrayDeque<MapTask> list = new ArrayDeque<MapTask>();
		HashSet<MapTask> tasks = slaveRunningTask.get(id);
		Iterator<MapTask> it = tasks.iterator();
		while (it.hasNext()) {
			MapTask t = it.next();
			list.add(t);
			// mark the task as unassigned
			jobAssignedTask.get(t.getJob().getId()).remove(t);
		}
		// remove the slave ID from the list
		slaveRunningTask.remove(id, tasks);
		return list;
	}

	@Override
	public String toString() {
		return null;
	}

	/**
	 * Function to generate a new job ID
	 * @return jobID
	 */
	private synchronized int getJobID() {
		jobID++;
		return jobID;
	}

	/**
	 * Function to generate a new slave ID
	 * @return slaveID
	 */
	private synchronized int getSlaveID() {
		slaveID++;
		return slaveID;
	}

	/**
	 * Function to see what jobs are running
	 */
	public void getRunningJobs() {
		for (Entry<Integer, ArrayDeque<MapTask>> t : slaveMapTaskList
				.entrySet()) {
			int slaveID = t.getKey();
			Iterator<MapTask> it = t.getValue().iterator();
			System.out.println("Slave " + slaveID);
			while (it.hasNext()) {
				MapTask task = it.next();
				System.out.println(task.getJob().getId() + "  "
						+ task.getTaskID());
			}
		}

		for (Entry<Integer, HashSet<MapTask>> t : jobMapTask.entrySet()) {
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
