package util.core;

import java.io.File;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import util.comm.CoordListener;
import util.core.Task.Status;
import util.io.FileSplit;
import util.io.RecordReader;
import conf.Configuration;

public class JobTracker {

	private Map<Integer, Job> jobList;

	private Map<Integer, String> slaveList;
	private Map<Integer, HashSet<Task>> slaveTask;

	private Map<Integer, HashSet<MapTask>> mapTask;
	private Map<Integer, HashSet<ReduceTask>> reduceTask;
	private ArrayDeque<MapTask> queuedMapTask;
	private ArrayDeque<ReduceTask> queuedReduceTask;

	private int slaveID = 0;
	private int jobID = 0;

	public JobTracker() {
		CoordListener listener = new CoordListener(Configuration.SERVER_PORT,
				this);
		Thread t = new Thread(listener);
		t.setDaemon(false);
		t.start();
		jobList = new ConcurrentHashMap<Integer, Job>();
		slaveList = new HashMap<Integer, String>();
		slaveTask = new ConcurrentHashMap<Integer, HashSet<Task>>();
		mapTask = new ConcurrentHashMap<Integer, HashSet<MapTask>>();
		reduceTask = new ConcurrentHashMap<Integer, HashSet<ReduceTask>>();
		queuedMapTask = new ArrayDeque<MapTask>();
		queuedReduceTask = new ArrayDeque<ReduceTask>();
	}

	public void startMap(Job job) {
		job.setID(getJobID());
		synchronized (jobList) {
			jobList.put(jobID, job);
		}
		int taskID = 0;
		ArrayDeque<MapTask> curMapTasks = new ArrayDeque<MapTask>();
		File inputDir = new File(Configuration.INPUT_DIR);
		if (!inputDir.exists()) {
			System.out.println("Input Directory doesn't exist");
			return;
		}
		for (String filename : inputDir.list()) {
			String path = Configuration.INPUT_DIR + "/" + filename;
			RecordReader reader = new RecordReader(path);
			int recordNum = reader.getRecordNum();
			for (int i = 0; i < recordNum; i++) {
				MapTask t = new MapTask();
				t.setJob(job);
				t.setStatus(Status.PENDING);
				t.setInput(new FileSplit(path, i, Configuration.RECORD_SIZE));
				t.setOutputPath("Map_" + job.getId() + "/" + taskID + ".mapout");
				t.setTaskID(taskID);
				curMapTasks.add(t);
				taskID++;
			}
		}
		synchronized (queuedMapTask) {
			queuedMapTask.addAll(curMapTasks);
		}
		synchronized (mapTask) {
			HashSet<MapTask> hs = new HashSet<MapTask>();
			hs.addAll(curMapTasks);
			mapTask.put(jobID, hs);
		}
	}
	
	private void startReduce(Job job) {
		System.out.println("reduce started...");
	}

	public synchronized ArrayDeque<MapTask> assignMapTask(int slaveID, int taskNum) {
		ArrayDeque<MapTask> tasks = new ArrayDeque<MapTask>();
		for (int i = 0; i < taskNum; i++) {
			MapTask task = queuedMapTask.pollFirst();
			if (task == null) break;
			tasks.add(task);
			HashSet<Task> t = slaveTask.get(slaveID);
			t.add(task);
			slaveTask.put(slaveID, t);
		}
		return tasks;
	}
	
	public synchronized ArrayDeque<ReduceTask> assignReduceTask(int slaveID, int taskNum) {
		ArrayDeque<ReduceTask> tasks = new ArrayDeque<ReduceTask>();
		for (int i = 0; i < taskNum; i++) {
			ReduceTask task = queuedReduceTask.pollFirst();
			if (task == null) break;
			tasks.add(task);
			HashSet<Task> t = slaveTask.get(slaveID);
			t.add(task);
			slaveTask.put(slaveID, t);	
		}
		return tasks;
	}
	
	public synchronized void markDone(int slaveID, ArrayDeque<Task> finishedTask) {
		for (Task task: finishedTask) {
			System.out.println("mark done");
			HashSet<Task> slaveTaskList = slaveTask.get(slaveID);
			slaveTaskList.remove(task);
			System.out.println("after mark done size " + slaveTaskList.size());
			int jobID = task.getJob().getId();
			// if the job is in mapping process
			if (mapTask.containsKey(jobID)) {
				HashSet<MapTask> hs = mapTask.get(jobID);
				System.out.println("hash set object: ");
				Task ttt = hs.iterator().next();
				System.out.println(ttt.getJob().getId() +  "  " + ttt.getTaskID() + ttt.hashCode());
				System.out.println(task.getJob().getId() + "  " + task.getTaskID() + task.hashCode());
				hs.remove(task);
				System.out.println("job to task after remove size " + hs.size());
				if (hs.isEmpty()) {
					// start reduce for this job
					mapTask.remove(jobID);
					startReduce(task.getJob());
				}
			}
			// if the job is in reducing process
			else if (reduceTask.containsKey(jobID)){
				HashSet<ReduceTask> hs = reduceTask.get(jobID);
				hs.remove(task);
				if (hs.isEmpty()) {
					// current job is officially finished
					reduceTask.remove(jobID);
					// report finish~~
				}
			}
		}

	}

	public int addSlave(String s) {
		slaveList.put(getSlaveID(), s);
		HashSet<Task> hs = new HashSet<Task>();
		slaveTask.put(slaveID, hs);
		return slaveID;
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
