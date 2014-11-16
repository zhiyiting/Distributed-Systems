package util.dfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import util.comm.SlaveListener;
import util.io.FileChunk;

public class DFSClient {

	private String host;
	private String folderPath;
	private int fileNum;
	private SlaveListener listener;
	private HashMap<Integer, TreeMap<String, ArrayDeque<Integer>>> jobToPartition;

	public DFSClient() {
		fileNum = 0;
		jobToPartition = new HashMap<Integer, TreeMap<String, ArrayDeque<Integer>>>();
		try {
			setHost(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			this.folderPath = InetAddress.getLocalHost().getHostName() + "/";
			File folder = new File(folderPath);
			folder.mkdirs();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		listener = new SlaveListener(this);
		Thread t = new Thread(listener);
		t.setDaemon(false);
		t.start();
	}

	public int getFileNum() {
		return fileNum;
	}

	public void createFile(FileChunk in) {
		String filename = folderPath + in.getFileName();
		try {
			PrintWriter writer = new PrintWriter(filename, "UTF-8");
			writer.println(in.getChunk());
			writer.close();
			System.out.println("DFS: file chunk saved at " + filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void addPartition(HashMap<Integer, ArrayDeque<String[]>> pa) {
		for (Entry<Integer, ArrayDeque<String[]>> cur : pa.entrySet()) {
			int jobID = cur.getKey();
			ArrayDeque<String[]> p = cur.getValue();
			TreeMap<String, ArrayDeque<Integer>> row = jobToPartition
					.get(jobID);
			if (row == null) {
				row = new TreeMap<String, ArrayDeque<Integer>>();
			}
			for (String[] item : p) {
				String key = item[0];
				int val = Integer.parseInt(item[1]);
				// System.out.println("job: " + jobID + " key: " + key +
				// " value: " + val);
				ArrayDeque<Integer> temp = row.get(key);
				if (temp == null) {
					temp = new ArrayDeque<Integer>();
				}
				temp.add(val);
				row.put(key, temp);
			}
			jobToPartition.put(jobID, row);
		}
	}

	public TreeMap<String, ArrayDeque<Integer>> getPartition(int jobID) {
		return jobToPartition.get(jobID);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getFolderPath() {
		return folderPath;
	}
}
