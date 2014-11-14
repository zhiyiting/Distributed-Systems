package util.comm;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import util.dfs.DFSClient;
import conf.Configuration;

public class SlaveListener implements Runnable{

	private ServerSocket serverSocket;
	private DFSClient dfs;
	
	public SlaveListener(DFSClient dfs) {
		int port = Configuration.SERVER_PORT;
		try {
			this.serverSocket = new ServerSocket(port);
			System.out.println("DFS listening on port " + port);

		} catch (IOException e) {
			System.out.println("Cannot listen on port " + port);
			e.printStackTrace();
		}
		this.dfs = dfs;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				Thread t = new Thread(new SlaveDispatcher(socket, dfs));
				t.setDaemon(false);
				t.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
