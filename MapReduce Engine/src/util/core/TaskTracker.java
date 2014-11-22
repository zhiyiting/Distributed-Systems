package util.core;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;

import util.dfs.DFSClient;
import conf.Configuration;

/**
 * Task tracker class at the slave side to coordinate the running tasks
 * 
 * @author zhiyiting
 *
 */
public class TaskTracker {

	// the tasks to run
	private ArrayDeque<MapTask> mapTasks;
	private ArrayDeque<ReduceTask> reduceTasks;
	// the finished tasks
	private ArrayDeque<Task> finishedTasks;
	// the job to slave node generated partition
	private HashMap<Integer, HashMap<Integer, ArrayDeque<String[]>>> partition;
	private int maxMapSlot;
	private int maxReduceSlot;
	// other active slaves
	private HashMap<Integer, String> slaveList;
	// dfs client node
	private DFSClient dfs;

	/**
	 * Constructor the initialize the task tracker
	 */
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

	/**
	 * Function to get available map slot
	 * 
	 * @return slot number
	 */
	public synchronized int getIdleMapSlot() {
		return maxMapSlot - mapTasks.size();
	}

	/**
	 * Function to get available reduce slot
	 * 
	 * @return slot number
	 */
	public synchronized int getIdleReduceSlot() {
		return maxReduceSlot - reduceTasks.size();
	}

	/**
	 * Function to start to work on map tasks
	 * 
	 * @param task
	 */
	public synchronized void addMapTask(ArrayDeque<MapTask> task) {
		for (MapTask t : task) {
			MapWorker worker = new MapWorker(t, this, dfs);
			// start a thread to work on task
			Thread thread = new Thread(worker);
			thread.start();
			mapTasks.addLast(t);
		}
	}

	/**
	 * Function to start to work on reduce tasks
	 * 
	 * @param task
	 */
	public synchronized void addReduceTask(ArrayDeque<ReduceTask> task) {
		for (ReduceTask t : task) {
			ReduceWorker worker = new ReduceWorker(t, this, dfs);
			Thread thread = new Thread(worker);
			thread.start();
			reduceTasks.addLast(t);
		}
	}

	/**
	 * Function to mark tasks as finished
	 * 
	 * @param task
	 */
	public synchronized void finishMapTask(MapTask task) {
		mapTasks.remove(task);
		finishedTasks.addLast(task);
	}

	/**
	 * Function to mark tasks as finished
	 * 
	 * @param task
	 */
	public synchronized void finishReduceTask(ReduceTask task) {
		reduceTasks.remove(task);
		finishedTasks.addLast(task);
	}

	/**
	 * Function to get finished tasks
	 * 
	 * @return finished tasks
	 */
	public synchronized ArrayDeque<Task> getFinishedTasks() {
		ArrayDeque<Task> tmp = finishedTasks;
		finishedTasks = new ArrayDeque<Task>();
		return tmp;
	}

	/**
	 * Function to produce partition depending on the map result
	 * 
	 * @param jobID
	 * @param map
	 */
	public synchronized void addPartition(int jobID,
			HashMap<Integer, ArrayDeque<String[]>> map) {
		// get partition for the job
		HashMap<Integer, ArrayDeque<String[]>> temp = partition.get(jobID);
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

	/**
	 * Function to get partition
	 * 
	 * @return partition
	 */
	public synchronized HashMap<Integer, HashMap<Integer, ArrayDeque<String[]>>> getPartition() {
		HashMap<Integer, HashMap<Integer, ArrayDeque<String[]>>> tmp = partition;
		partition = new HashMap<Integer, HashMap<Integer, ArrayDeque<String[]>>>();
		return tmp;
	}

	/**
	 * Function to set the active slaves
	 * 
	 * @param slave list
	 */
	public void setSlaveList(HashMap<Integer, String> list) {
		this.slaveList = list;
	}

	/**
	 * Function to get slave list
	 * 
	 * @return slave list
	 */
	public HashMap<Integer, String> getSlaveList() {
		return slaveList;
	}

	/**
	 * Function to get the running tasks on this slave node
	 */
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
