package util.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import util.core.Job;

/**
 * LineRecordWriter class to write record into chunks
 * 
 * @author zhiyiting
 *
 */
public class LineRecordWriter {

	private StringBuilder sb;
	private RandomAccessFile f;
	private int chunkSize;

	/**
	 * Constructor to read from beginning
	 * 
	 * @param path
	 * @param job
	 */
	public LineRecordWriter(String path, Job job) {
		try {
			f = new RandomAccessFile(path, "r");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sb = new StringBuilder();
		chunkSize = job.conf.RECORD_SIZE;
	}

	/**
	 * Constructor to read from a specific start point
	 * 
	 * @param path
	 * @param start
	 * @param job
	 */
	public LineRecordWriter(String path, long start, Job job) {
		try {
			f = new RandomAccessFile(path, "r");
			// go to the start position
			f.seek(start);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sb = new StringBuilder();
		chunkSize = job.conf.RECORD_SIZE;
	}

	/**
	 * Create a file split with a pre-defined chunk size
	 * 
	 * @param index
	 * @param filename
	 * @return file split
	 */
	public FileSplit createSplit(int index, String filename) {
		FileSplit file = null;
		try {
			file = new FileSplit(filename, index, f.getFilePointer(), chunkSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	/**
	 * Create a file chunk with a specific filename
	 * 
	 * @param filename
	 * @return file chunk
	 */
	public synchronized FileChunk write(String filename) {
		sb = new StringBuilder();
		for (int i = 0; i < chunkSize; i++) {
			try {
				String line;
				if ((line = f.readLine()) == null) {
					break;
				}
				sb.append(line);
				sb.append('\n');
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		FileChunk chunk = new FileChunk(filename, sb.toString());
		return chunk;
	}
}
