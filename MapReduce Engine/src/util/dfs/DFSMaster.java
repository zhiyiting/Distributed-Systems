package util.dfs;

import java.io.File;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.PriorityQueue;

import conf.Configuration;
import util.comm.CommModule;
import util.comm.DFSMessage;
import util.comm.RemoteException;
import util.core.Job;
import util.core.JobTracker;
import util.core.MapTask;
import util.core.Task.Status;
import util.io.FileSplit;
import util.io.LineRecordReader;
import util.io.LineRecordWriter;

public class DFSMaster {

	private HashMap<Integer, String> slaveList;
	private HashMap<String, ArrayDeque<Integer>> fileToSlave;
	private PriorityQueue<Pair> dfsNode;
	private CommModule commModule;
	private JobTracker tracker;

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

	public DFSMaster(JobTracker tracker) {
		slaveList = new HashMap<Integer, String>();
		fileToSlave = new HashMap<String, ArrayDeque<Integer>>();
		dfsNode = new PriorityQueue<Pair>();
		commModule = new CommModule();
		this.tracker = tracker;
	}

	public synchronized void distributeFile(String in, int replica, Job job) {
		File inputDir = new File(in);
		if (!inputDir.exists()) {
			System.out.println("Input Directory doesn't exist");
			return;
		}
		for (String filename : inputDir.list()) {
			String path = in + "/" + filename;
			LineRecordReader reader = new LineRecordReader(path);
			int recordNum = reader.getRecordNum();
			for (int i = 0; i < recordNum; i++) {
				for (int j = 0; j < replica; j++) {
					Pair node = dfsNode.poll();
					LineRecordWriter writer = new LineRecordWriter(path);
					FileSplit buffer = writer.write(i, filename);
					DFSMessage msg = new DFSMessage("distribute", buffer,
							slaveList.get(node.id), Configuration.SERVER_PORT);
					try {
						commModule.send(msg);
					} catch (RemoteException e) {
						break;
					}
					String fn = filename + "_" + i;
					if (fileToSlave.containsKey(fn)) {
						ArrayDeque<Integer> temp = fileToSlave.get(fn);
						temp.push(node.id);
					}
					else {
						ArrayDeque<Integer> temp = new ArrayDeque<Integer>();
						temp.add(node.id);
						fileToSlave.put(fn, temp);
					}
					MapTask newTask = new MapTask();
					newTask.setInput(buffer);
					newTask.setJob(job);
					newTask.setStatus(Status.PENDING);
					tracker.addQueuedMapTask(node.id, newTask);
					node.fileCount++;
					dfsNode.add(node);
				}
			}
		}
	}

	public void addSlave(int i, String host) {
		slaveList.put(i, host);
		dfsNode.add(new Pair(i, 0));
	}
}
