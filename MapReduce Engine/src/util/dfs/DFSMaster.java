package util.dfs;

import java.io.File;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import conf.Configuration;
import util.comm.CommModule;
import util.comm.DFSMessage;
import util.comm.RemoteException;
import util.core.Job;
import util.core.JobTracker;
import util.core.MapTask;
import util.core.Task;
import util.core.Task.Status;
import util.io.FileChunk;
import util.io.FileSplit;
import util.io.LineRecordReader;
import util.io.LineRecordWriter;

/**
 * DFS Master class at the master side
 * 
 * @author zhiyiting
 *
 */
public class DFSMaster {

	private HashMap<Integer, String> slaveList;
	private HashMap<Integer, HashMap<Integer, HashSet<FileSplit>>> jobToSlaveFile;
	private PriorityQueue<Pair> dfsNode;
	private CommModule commModule;
	private JobTracker tracker;

	// Type for the priority queue
	class Pair implements Comparable<Pair> {
		public int id;
		public int fileCount;

		public Pair(int x, int y) {
			this.id = x;
			this.fileCount = y;
		}

		@Override
		public int compareTo(Pair o) {
			return this.fileCount - o.fileCount;
		}
	}

	/**
	 * Constructor to initialize the DFS master
	 * 
	 * @param tracker
	 */
	public DFSMaster(JobTracker tracker) {
		slaveList = new HashMap<Integer, String>();
		jobToSlaveFile = new HashMap<Integer, HashMap<Integer, HashSet<FileSplit>>>();
		dfsNode = new PriorityQueue<Pair>();
		commModule = new CommModule();
		this.tracker = tracker;
	}

	/**
	 * Function to distribute file with replication at the beginning of a job
	 * 
	 * @param input file
	 * @param replica
	 * @param job
	 */
	public synchronized void distributeFile(String in, int replica, Job job) {
		System.out.println("Distributing file on DFS...");
		File inputDir = new File(in);
		if (!inputDir.exists()) {
			System.out.println("Input Directory doesn't exist");
			return;
		}
		int jobID = job.getId();
		HashMap<Integer, HashSet<FileSplit>> slaveToFile = jobToSlaveFile
				.get(jobID);
		if (slaveToFile == null) {
			slaveToFile = new HashMap<Integer, HashSet<FileSplit>>();
		}
		int taskID = 0;
		// iterate through the input files to generate file split
		for (String filename : inputDir.list()) {
			String path = in + filename;
			// create reader and writer for the file
			LineRecordReader reader = new LineRecordReader(path, job);
			int recordNum = reader.getRecordNum();
			LineRecordWriter writer = new LineRecordWriter(path, job);
			// generate file splits
			for (int i = 0; i < recordNum; i++) {
				FileSplit split = writer.createSplit(i, filename);
				String fn = split.getFilename();
				// write the data into a file chunk
				FileChunk chunk = writer.write(fn);
				// create a new map task accordingly
				MapTask newTask = new MapTask();
				newTask.setInput(split);
				newTask.setJob(job);
				newTask.setStatus(Status.PENDING);
				newTask.setTaskID(taskID);
				// choose a node with least files and replicate the file chunk
				for (int j = 0; j < replica; j++) {
					Pair node = dfsNode.poll();
					if (node == null) {
						break;
					}
					DFSMessage msg = new DFSMessage("distribute", chunk,
							slaveList.get(node.id), Configuration.SERVER_PORT);
					try {
						commModule.send(msg);
					} catch (RemoteException e) {
						break;
					}
					node.fileCount++;
					dfsNode.add(node);
					HashSet<FileSplit> fileList = slaveToFile.get(node.id);
					if (fileList == null) {
						fileList = new HashSet<FileSplit>();
					}
					fileList.add(split);
					slaveToFile.put(node.id, fileList);
					newTask.setSlaveID(node.id);
					// let tracker know the node can work on a new task
					tracker.addQueuedMapTask(node.id, newTask);
				}
				taskID++;
			}
		}
		jobToSlaveFile.put(jobID, slaveToFile);
	}

	/**
	 * Function to add a new slave
	 * @param id
	 * @param host
	 */
	public void addSlave(int i, String host) {
		slaveList.put(i, host);
		dfsNode.add(new Pair(i, 0));
	}

	/**
	 * Function to remove slave
	 * 
	 * @param id
	 */
	public synchronized void removeSlave(int id) {
		slaveList.remove(id);
	}

	/**
	 * Function to enforce replication
	 * 
	 * @param id
	 * @param tasks
	 */
	public synchronized void enforceReplication(int id,
			ArrayDeque<MapTask> tasks) {
		if (tasks == null) {
			tasks = new ArrayDeque<MapTask>();
		}
		// get running task for that job id
		ArrayDeque<MapTask> runningTask = tracker.getRunningMapList(id);
		tasks.addAll(runningTask);
		for (MapTask task : tasks) {
			FileSplit split = task.getInput();
			// regenerate file split accordingly
			LineRecordWriter writer = new LineRecordWriter(
					task.getJob().conf.INPUT_DIR + split.getOriginalName(),
					split.getStart(), task.getJob());
			Pair node = dfsNode.poll();
			if (node != null && node.id == id) {
				node = dfsNode.poll();
			}
			if (node == null) {
				return;
			}
			int jobID = task.getJob().getId();
			HashMap<Integer, HashSet<FileSplit>> slaveToFile = jobToSlaveFile
					.get(jobID);
			HashSet<FileSplit> files = slaveToFile.get(node.id);
			// get the node with least files
			if (files == null) {
				files = new HashSet<FileSplit>();
				files.add(split);
				slaveToFile.put(node.id, files);
				node.fileCount++;
				dfsNode.add(node);
			} else if (!files.contains(split)) {
				files.add(split);
				slaveToFile.put(node.id, files);
				node.fileCount++;
				dfsNode.add(node);
			} else {
				// if the node already contains that file chunk
				// randomly select another node to put the chunk
				dfsNode.add(node);
				Iterator<Pair> itr = dfsNode.iterator();
				boolean found = false;
				while (itr.hasNext()) {
					node = itr.next();
					files = slaveToFile.get(node.id);
					if (files == null) {
						files = new HashSet<FileSplit>();
						files.add(split);
						slaveToFile.put(node.id, files);
						node.fileCount++;
						found = true;
						break;
					} else if (!files.contains(task)) {
						files.add(split);
						slaveToFile.put(node.id, files);
						node.fileCount++;
						found = true;
						break;
					}
				}
				if (!found) {
					System.out.println("Failed to enforce replication for "
							+ split.getFilename());
					continue;
				}
			}
			FileChunk chunk = writer.write(split.getFilename());
			DFSMessage msg = new DFSMessage("distribute", chunk,
					slaveList.get(node.id), Configuration.SERVER_PORT);
			try {
				commModule.send(msg);
			} catch (RemoteException e) {
				break;
			}
			task.setSlaveID(node.id);
			HashSet<Task> ft = new HashSet<Task>();
			ft = tracker.getFinishedTaskList(jobID);
			if (!ft.contains(task)) {
				tracker.addQueuedMapTask(node.id, task);
			}
		}
	}

	/**
	 * Function to get the  active slave list
	 * 
	 * @return slave list
	 */
	public HashMap<Integer, String> getSlaveList() {
		return slaveList;
	}
}
