package util;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public class FileSplit implements Serializable {

	private static final long serialVersionUID = -4955423900859261947L;
	private String path;
	private long start;
	private int length;

	public FileSplit() {
	}

	public FileSplit(String path, int idx, int length) {
		this.path = path;
		this.start = idx * length;
		this.length = length;
	}

	public void write(ObjectOutput out) {
		// TODO Auto-generated method stub

	}

	public void read(ObjectInput in) {
		// TODO Auto-generated method stub

	}

	public String getPath() {
		return this.path;
	}

	public long getStart() {
		return this.start;
	}

	public int getLength() {
		return this.length;
	}

}
