package util.comm;

public class DFSMessage extends Message {

	private static final long serialVersionUID = 2017772749305634518L;
	private String buffer;
	private String filename;
	
	public DFSMessage(String content, String filename, String file, String toHost, int toPort) {
		super(content, toHost, toPort);
		this.setBuffer(file);
		this.setFilename(filename);
	}

	public String getBuffer() {
		return buffer;
	}

	public void setBuffer(String buffer) {
		this.buffer = buffer;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
