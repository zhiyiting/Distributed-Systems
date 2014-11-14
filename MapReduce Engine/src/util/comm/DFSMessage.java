package util.comm;

import util.io.FileSplit;

public class DFSMessage extends Message {

	private static final long serialVersionUID = 2017772749305634518L;
	private FileSplit file;

	public DFSMessage(String content, FileSplit file, String toHost, int toPort) {
		super(content, toHost, toPort);
		this.setFile(file);
	}

	public FileSplit getFile() {
		return file;
	}

	public void setFile(FileSplit file) {
		this.file = file;
	}

}
