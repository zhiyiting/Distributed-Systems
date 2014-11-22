package util.comm;

import util.io.FileChunk;

/**
 * Message to be transmitted for DFS purpose
 * 
 * @author zhiyiting
 *
 */
public class DFSMessage extends Message {

	private static final long serialVersionUID = 2017772749305634518L;
	private FileChunk file;

	/**
	 * Construct a DFS Message
	 * 
	 * @param content
	 * @param file
	 * @param toHost
	 * @param toPort
	 */
	public DFSMessage(String content, FileChunk file, String toHost, int toPort) {
		super(content, toHost, toPort);
		this.file = file;
	}

	/**
	 * Get the file information from the message
	 * 
	 * @return
	 */
	public FileChunk getFile() {
		return file;
	}
}
