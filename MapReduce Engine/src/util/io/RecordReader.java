package util.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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

	public RecordReader(FileSplit file) {
		this.path = file.getPath();
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
		String [] pair = new String[2];
		pair[0] = "";
		pair[1] = data.toString();
		ArrayDeque<String[]> result = new ArrayDeque<String[]>();
		result.add(pair);
		return result;
	}

	public int getRecordNum() {
		return this.recordNum;
	}

	private boolean isChar(byte b) {
		return b != ' ' && b != '\t' && b != '\n';
	}
}
