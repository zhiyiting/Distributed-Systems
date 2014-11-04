package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SlaveConsole implements Runnable{

	private boolean canRun;
	private BufferedReader br;
	private TaskTracker tracker;
	private SlaveMessenger messenger;
	
	public SlaveConsole() {
		this.canRun = true;
		this.br = new BufferedReader(new InputStreamReader(System.in));
		this.tracker = new TaskTracker();
		this.messenger = new SlaveMessenger(tracker);
		Thread t = new Thread(messenger);
		t.setDaemon(false);
		t.start();	
	}

	@Override
	public void run() {
		while (canRun) {
			try {
				String in = br.readLine();
				switch (in) {
				// print all the jobs
				case "list":
					tracker.list();
					break;
				// stop the node
				case "quit":
					canRun = false;
					break;			
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
