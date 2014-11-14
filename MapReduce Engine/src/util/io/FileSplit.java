package util.io;

import java.io.Serializable;

public class FileSplit implements Serializable {

	private static final long serialVersionUID = -4955423900859261947L;
	private String filename;
	private int index;
	private long start;
	private int length;
	private String content;

	public FileSplit(String filename, int index, long start, int length) {
		this.filename = filename;
		this.index = index;
		this.start = start;
		this.length = length;
	}

	public String getFilename() {
		return this.filename;
	}

	public long getStart() {
		return this.start;
	}

	public int getLength() {
		return this.length;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
