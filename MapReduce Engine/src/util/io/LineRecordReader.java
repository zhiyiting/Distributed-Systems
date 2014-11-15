package util.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;

import conf.Configuration;

public class LineRecordReader {

	private String path;
	private int recordNum;
	private long start;
	private long length;

	public LineRecordReader(String path) {
		this.path = path;
		this.recordNum = 0;
		this.start = 0;
		this.length = 0;
	}

	public int getRecordNum() {
		try {
			RandomAccessFile f = new RandomAccessFile(path, "r");
			while (f.readLine() != null) {
				length++;
			}
			recordNum = (int) (length / Configuration.RECORD_SIZE);
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.recordNum;
	}
}
