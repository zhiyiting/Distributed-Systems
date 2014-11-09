package util.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;

public class Context {
	
	private ArrayDeque<String[]> buffer;
	private String path;
	
	public Context(String path) {
		this.path = path;
		buffer = new ArrayDeque<String[]>();
	}
	
	public void write(String key, String val) {
		String [] record = new String[2];
		record[0] = key;
		record[1] = val;
		buffer.add(record);
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
				System.out.println("newline " + sb.toString());
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
