package util.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import conf.Configuration;

public class LineRecordWriter {

	private StringBuilder sb;
	private RandomAccessFile f;
	private int chunkSize;

	public LineRecordWriter(String path) {
		try {
			f = new RandomAccessFile(path, "r");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sb = new StringBuilder();
		chunkSize = Configuration.RECORD_SIZE;
	}

	public FileSplit createSplit(int index, String filename) {
		FileSplit file = null;
		try {
			file = new FileSplit(filename, index, f.getFilePointer(), chunkSize);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return file;
	}
	
	public FileChunk write(String filename) {
		sb = new StringBuilder();
		for (int i = 0; i < chunkSize; i++) {
			try {
				sb.append(f.readLine());
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
