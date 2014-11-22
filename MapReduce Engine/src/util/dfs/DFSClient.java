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

/**
 * DFS Client class on the slave side
 * 
 * @author zhiyiting
 *
 */
public class DFSClient {

	private String folderPath;
	private SlaveListener listener;
	// jobID to partition
	private ConcurrentHashMap<Integer, TreeMap<String, ArrayDeque<String>>> jobToPartition;

	/**
	 * Constructor to initialize DFS Client
	 */
	public DFSClient() {
		jobToPartition = new ConcurrentHashMap<Integer, TreeMap<String, ArrayDeque<String>>>();
		try {
			this.folderPath = InetAddress.getLocalHost().getHostName() + "/";
			File folder = new File(folderPath);
			folder.mkdirs();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		// start the listener
		listener = new SlaveListener(this);
		Thread t = new Thread(listener);
		t.setDaemon(false);
		t.start();
	}

	/**
	 * Function to create a file
	 * 
	 * @param input file
	 */
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

	/**
	 * Function to add partition to the node
	 * 
	 * @param partition
	 */
	public synchronized void addPartition(
			HashMap<Integer, ArrayDeque<String[]>> par) {
		for (Entry<Integer, ArrayDeque<String[]>> cur : par.entrySet()) {
			int jobID = cur.getKey();
			ArrayDeque<String[]> p = cur.getValue();
			TreeMap<String, ArrayDeque<String>> row = jobToPartition.get(jobID);
			if (row == null) {
				row = new TreeMap<String, ArrayDeque<String>>();
			}
			// add the key value pair to the partition
			for (String[] item : p) {
				String key = item[0];
				String val = item[1];
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

	/**
	 * Function to get the partition for a job
	 * 
	 * @param jobID
	 * @return partition
	 */
	public synchronized TreeMap<String, ArrayDeque<String>> getPartition(
			int jobID) {
		return jobToPartition.get(jobID);
	}

	/**
	 * Function to get folder path
	 * 
	 * @return folder path
	 */
	public String getFolderPath() {
		return folderPath;
	}
}
