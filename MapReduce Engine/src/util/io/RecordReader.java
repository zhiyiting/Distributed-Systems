/*package util.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;

import conf.Configuration;

public class RecordReader {

	private String path;
	private int recordNum;
	private long start;
	private long length;
	private byte[] data;

	public RecordReader(String path) {
		this.path = path;
		this.recordNum = 0;
		this.setStart(0);
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

	public RecordReader(FileSplit file) {
		this.path = file.getFilename();
		this.start = file.getStart();
		this.length = file.getLength();
		try {
			RandomAccessFile f = new RandomAccessFile(path, "r");
			// modify start and length so that each read is a complete segment
			if (start > 0) {
				f.seek(start - 1);
				if (isChar(f.readByte())) {
					while (isChar(f.readByte())) {
						start++;
						length--;
					}

				}
			}
			data = new byte[(int) length];
			f.seek(start);
			f.readFully(data);
			if (isChar(data[data.length - 1])) {
				ArrayDeque<Byte> newChar = new ArrayDeque<Byte>();
				byte b;
				while (isChar(b = f.readByte())) {
					newChar.add(b);
					length++;
				}
				if (!newChar.isEmpty()) {
					byte[] copy = data;
					data = new byte[(int) length];
					System.arraycopy(copy, 0, data, 0, copy.length);
					System.arraycopy(newChar, 0, data, copy.length,
							newChar.size());
				}
			}
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public ArrayDeque<String[]> getKVPair() {
		String[] pair = new String[2];
		pair[0] = "";
		try {
			pair[1] = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ArrayDeque<String[]> result = new ArrayDeque<String[]>();
		result.add(pair);
		return result;
	}

	public int getRecordNum() {
		return this.recordNum;
	}

	public int getLineNum() {
		recordNum = 0;
		try {
			RandomAccessFile f = new RandomAccessFile(path, "r");
			while (f.readLine() != null) {
				recordNum++;
			}
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return recordNum;
	}

	private boolean isChar(byte b) {
		return b != ' ' && b != '\t' && b != '\n';
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}
}*/
