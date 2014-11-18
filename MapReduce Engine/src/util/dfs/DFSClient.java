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
import java.util.concurrent.ConcurrentHashMap;

import util.comm.SlaveListener;
import util.io.FileChunk;

public class DFSClient {

	private String host;
	private String folderPath;
	private int fileNum;
	private SlaveListener listener;
	private ConcurrentHashMap<Integer, TreeMap<String, ArrayDeque<String>>> jobToPartition;

	public DFSClient() {
		fileNum = 0;
		jobToPartition = new ConcurrentHashMap<Integer, TreeMap<String, ArrayDeque<String>>>();
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

	public synchronized void addPartition(HashMap<Integer, ArrayDeque<String[]>> pa) {
		for (Entry<Integer, ArrayDeque<String[]>> cur : pa.entrySet()) {
			int jobID = cur.getKey();
			ArrayDeque<String[]> p = cur.getValue();
			TreeMap<String, ArrayDeque<String>> row = jobToPartition
					.get(jobID);
			if (row == null) {
				row = new TreeMap<String, ArrayDeque<String>>();
			}
			for (String[] item : p) {
				String key = item[0];
				String val = item[1];
				// System.out.println("job: " + jobID + " key: " + key +
				// " value: " + val);
				ArrayDeque<String> temp = row.get(key);
				if (temp == null) {
					temp = new ArrayDeque<String>();
				}
				temp.add(val);
				row.put(key, temp);
			}
			jobToPartition.put(jobID, row);
		}
	}

	public synchronized TreeMap<String, ArrayDeque<String>> getPartition(int jobID) {
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
