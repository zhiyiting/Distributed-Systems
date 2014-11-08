package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

import conf.Configuration;

public class RecordReader {

	private String path;
	private int recordNum;
	private long start;
	private long length;

	public RecordReader(String path) {
		this.path = path;
		this.recordNum = 0;
		this.start = 0;
		try {
			RandomAccessFile f = new RandomAccessFile(path, "r");
			length = f.length();
			recordNum = (int) (length / Configuration.RECORD_SIZE);
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public String[][] getKVPair(int partitionIdx, int partitionSize) {
		String[][] kvPair = new String[partitionSize][2];
		try {
			RandomAccessFile f = new RandomAccessFile(path, "r");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < partitionSize; i++) {
			
		}
		return kvPair;
	}

	public int getRecordNum() {
		return this.recordNum;
	}
}
