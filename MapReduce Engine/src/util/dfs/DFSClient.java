package util.dfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import util.comm.SlaveListener;
import util.io.FileChunk;
import util.io.FileSplit;

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

	public void createFile(FileChunk in) {
		String filename = folderPath + in.getFileName();
		try {
			PrintWriter writer = new PrintWriter(filename, "UTF-8");
			writer.println(in.getChunk());
			writer.close();
			System.out.println("DFS: file chunk saved at " + filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public String getFolderPath() {
		return folderPath;
	}
}
