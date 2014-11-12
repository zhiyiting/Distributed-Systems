package util.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import conf.Configuration;

public class LineRecordWriter {

	private StringBuilder sb;
	private BufferedReader f;
	private int chunkSize;

	public LineRecordWriter(String path) {
		try {
			f = new BufferedReader(new FileReader(path));;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sb = new StringBuilder();
		chunkSize = Configuration.RECORD_SIZE;
	}

	public String write(int idx) {
		sb = new StringBuilder();
		for (int i = 0; i < chunkSize; i++) {
			try {
				sb.append(f.readLine());
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		return sb.toString();
	}
}
