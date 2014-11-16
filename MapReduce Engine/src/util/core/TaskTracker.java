package util.core;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;

import util.dfs.DFSClient;
import conf.Configuration;

public class TaskTracker {

	private ArrayDeque<MapTask> mapTasks;
	private ArrayDeque<ReduceTask> reduceTasks;
	private ArrayDeque<Task> finishedTasks;
	private HashMap<Integer, HashMap<Integer, ArrayDeque<String[]>>> partition;
	private int maxMapSlot;
	private int maxReduceSlot;
	private HashMap<Integer, String> slaveList;
	private DFSClient dfs;

	public TaskTracker() {
		this.maxMapSlot = Configuration.MAP_PER_NODE;
		this.maxReduceSlot = Configuration.REDUCE_PER_NODE;
		this.mapTasks = new ArrayDeque<MapTask>(maxMapSlot);
		this.reduceTasks = new ArrayDeque<ReduceTask>(maxReduceSlot);
		this.finishedTasks = new ArrayDeque<Task>();
		this.partition = new HashMap<Integer, HashMap<Integer, ArrayDeque<String[]>>>();
		this.slaveList = new HashMap<Integer, String>();
		this.dfs = new DFSClient();
	}

	public synchronized int getIdleMapSlot() {
		return maxMapSlot - mapTasks.size();
	}

	public synchronized int getIdleReduceSlot() {
		return maxReduceSlot - reduceTasks.size();
	}

	public synchronized void addMapTask(ArrayDeque<MapTask> task) {
		for (MapTask t : task) {
			MapWorker worker = new MapWorker(t, this, dfs);
			Thread thread = new Thread(worker);
			thread.start();
			mapTasks.addLast(t);
		}
	}

	public synchronized void addReduceTask(ArrayDeque<ReduceTask> task) {
		System.out.println("reduce task size: " + task.size());
		for (ReduceTask t : task) {
			ReduceWorker worker = new ReduceWorker(t, this, dfs);
			Thread thread = new Thread(worker);
			thread.start();
			reduceTasks.addLast(t);
		}
	}

	public synchronized void finishMapTask(MapTask task) {
		mapTasks.remove(task);
		finishedTasks.addLast(task);
	}

	public synchronized void finishReduceTask(ReduceTask task) {
		reduceTasks.remove(task);
		finishedTasks.addLast(task);
	}

	public synchronized ArrayDeque<Task> getFinishedTasks() {
		ArrayDeque<Task> tmp = finishedTasks;
		finishedTasks = new ArrayDeque<Task>();
		return tmp;
	}

	public synchronized void addPartition(int jobID,
			HashMap<Integer, ArrayDeque<String[]>> map) {
		HashMap<Integer, ArrayDeque<String[]>> temp = partition
				.get(jobID);
		if (temp == null) {
			temp = new HashMap<Integer, ArrayDeque<String[]>>();
		}
		for (Entry<Integer, ArrayDeque<String[]>> entry : map.entrySet()) {
			int key = entry.getKey(); // slaveID
			ArrayDeque<String[]> value = entry.getValue(); // partitions
			ArrayDeque<String[]> cur = temp.get(key);
			if (cur == null) {
				cur = new ArrayDeque<String[]>();
			}
			cur.addAll(value);
			temp.put(key, cur);
			partition.put(jobID, temp);
		}
	}

	public synchronized HashMap<Integer, HashMap<Integer, ArrayDeque<String[]>>> getPartition() {
		HashMap<Integer, HashMap<Integer, ArrayDeque<String[]>>> tmp = partition;
		partition = new HashMap<Integer, HashMap<Integer, ArrayDeque<String[]>>>();
		return tmp;
	}

	public void setSlaveList(HashMap<Integer, String> list) {
		this.slaveList = list;
	}

	public HashMap<Integer, String> getSlaveList() {
		return slaveList;
	}

	public synchronized void list() {
		System.out.println("Current job: ");
		System.out.println("Map tasks");
		if (mapTasks.size() > 0) {
			for (Task task : mapTasks) {
				System.out.println(task.toString());
			}
		} else {
			System.out.println("empty");
		}
		System.out.println("Reduce tasks");
		if (reduceTasks.size() > 0) {
			for (Task task : reduceTasks) {
				System.out.println(task.toString());
			}
		} else {
			System.out.println("empty");
		}

	}
}
