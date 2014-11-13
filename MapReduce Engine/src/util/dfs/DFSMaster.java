package util.dfs;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import conf.Configuration;
import util.comm.CommModule;
import util.comm.DFSMessage;
import util.comm.RemoteException;
import util.io.FileSplit;
import util.io.LineRecordReader;
import util.io.LineRecordWriter;

public class DFSMaster {

	private HashMap<Integer, String> slavePath;
	private HashMap<Integer, ArrayDeque<String>> fileDirectory;
	private PriorityQueue<DFSClient> dfsNode;
	private CommModule commModule;

	public DFSMaster() {
		slavePath = new HashMap<Integer, String>();
		fileDirectory = new HashMap<Integer, ArrayDeque<String>>();
		Comparator<DFSClient> comparator = new Comparator<DFSClient>() {
			@Override
			public int compare(DFSClient o1, DFSClient o2) {
				return o1.getFileNum() - o2.getFileNum();
			}
		};
		dfsNode = new PriorityQueue<DFSClient>(comparator);
		commModule = new CommModule();
	}

	public synchronized void distributeFile(String in, int replica) {
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
					DFSClient node = dfsNode.poll();
					LineRecordWriter writer = new LineRecordWriter(path);
					FileSplit buffer = writer.write(i, filename);
					DFSMessage msg = new DFSMessage("distribute", buffer, node.getHost(),
							Configuration.SERVER_PORT);
					try {
						commModule.send(msg);
					} catch (RemoteException e) {
						break;
					}
					node.incFileNum();
					dfsNode.add(node);
				}
			}
		}
	}

	public void addSlave(int i, String host) {
		slavePath.put(i, host + "/");
		ArrayDeque<String> file = new ArrayDeque<String>();
		fileDirectory.put(i, file);
	}

}
