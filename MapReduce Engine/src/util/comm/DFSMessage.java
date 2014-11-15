package util.comm;

import util.io.FileChunk;

public class DFSMessage extends Message {

	private static final long serialVersionUID = 2017772749305634518L;
	private FileChunk file;

	public DFSMessage(String content, FileChunk file, String toHost, int toPort) {
		super(content, toHost, toPort);
		this.file = file;
	}

	public FileChunk getFile() {
		return file;
	}

}
