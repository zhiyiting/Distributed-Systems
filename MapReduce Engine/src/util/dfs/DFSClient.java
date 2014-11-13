package util.dfs;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import util.comm.SlaveListener;

public class DFSClient {

	private String host;
	private String folderPath;
	private int fileNum;
	private SlaveListener listener;

	public DFSClient() {
		fileNum = 0;
		try {
			setHost(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			this.folderPath = InetAddress.getLocalHost().getHostName() + "/";
			File folder = new File(folderPath);
			folder.mkdirs();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		listener = new SlaveListener(this);
		Thread t = new Thread(listener);
		t.setDaemon(false);
		t.start();
	}

	public int getFileNum() {
		return fileNum;
	}

	public void incFileNum() {
		fileNum++;
	}

	public void createFile(String buffer, String filename) {
		File file = new File(folderPath + filename);
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
}
