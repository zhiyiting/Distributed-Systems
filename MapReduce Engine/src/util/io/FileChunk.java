package util.io;

import java.io.Serializable;

public class FileChunk implements Serializable {

	private static final long serialVersionUID = -4763608406708076892L;
	
	private String filename;
	private String chunk;
	
	public FileChunk(String fn, String s) {
		filename = fn;
		chunk = s;
	}
	
	public String getChunk() {
		return chunk;
	}
	
	public String getFileName() {
		return filename;
	}

}
