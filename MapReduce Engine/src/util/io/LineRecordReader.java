package util.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import util.core.Job;

/**
 * LineRecordReader class provides functions to read from file
 * 
 * @author zhiyiting
 *
 */
public class LineRecordReader {

	private String path;
	private int recordNum;
	private long length;
	private Job job;

	/**
	 * Constructor to define where to start reading
	 * 
	 * @param path
	 * @param job
	 */
	public LineRecordReader(String path, Job job) {
		this.path = path;
		this.recordNum = 0;
		this.length = 0;
		this.job = job;
	}

	/**
	 * Function to get record number (lines) from the file
	 * 
	 * @return record number
	 */
	public int getRecordNum() {
		try {
			RandomAccessFile f = new RandomAccessFile(path, "r");
			while (f.readLine() != null) {
				length++;
			}
			recordNum = (int) (length / job.conf.RECORD_SIZE + 1);
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
