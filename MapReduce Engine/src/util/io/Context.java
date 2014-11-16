package util.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;

import conf.Configuration;

public class Context {
	
	private ArrayDeque<String[]> buffer;
	private String path;
	private HashMap<Integer, ArrayDeque<String[]>> partition;
	
	
	public Context(String path) {
		this.path = path;
		buffer = new ArrayDeque<String[]>();
		partition = new HashMap<Integer, ArrayDeque<String[]>>();
	}
	
	public void write(String key, String val) {
		String [] record = new String[2];
		record[0] = key;
		record[1] = val;
		buffer.add(record);
	}
	
	public HashMap<Integer, ArrayDeque<String[]>> getPartition() {
		int reduceNum = Configuration.REDUCER_NUM;
		for (int i = 1; i <= reduceNum; i++) {
			ArrayDeque<String[]> temp = new ArrayDeque<String[]>();
			partition.put(i, temp);
		}
		for (String[] item: buffer) {
			int id = (item[0].hashCode() & 0x7FFFFFFF) % reduceNum + 1; // slaveID
			ArrayDeque<String[]> temp = partition.get(id);
			temp.push(item); // KVPair
			partition.put(id, temp);
		}
		return partition;
	}
	
	public void generateOutput() {
		File out = new File(path);
		if (!out.getParentFile().exists()) {
			out.getParentFile().mkdirs();
		}
		try {
			out.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));
			for (String[] entry: buffer) {
				StringBuilder sb = new StringBuilder();
				sb.append(entry[0]);
				sb.append(" ");
				sb.append(entry[1]);
				bw.write(sb.toString());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
