package util.comm;

import util.io.FileSplit;

public class DFSMessage extends Message {

	private static final long serialVersionUID = 2017772749305634518L;
	private String content;
	private String filename;

	public DFSMessage(String content, FileSplit file, String toHost, int toPort) {
		super(content, toHost, toPort);
		this.setContent(file.getContent());
		this.setFilename(file.getFilename());
	}

	public String getContent() {
		return content;
	}

	public void setContent(String buffer) {
		this.content = buffer;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
