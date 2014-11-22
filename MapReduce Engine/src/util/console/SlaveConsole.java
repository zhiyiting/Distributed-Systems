package util.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import util.comm.SlaveMessenger;
import util.core.TaskTracker;

/**
 * Slave node
 * 
 * @author zhiyiting
 *
 */
public class SlaveConsole implements Runnable {

	private boolean canRun;
	private BufferedReader br;
	private TaskTracker tracker;
	private SlaveMessenger messenger;

	/**
	 * Start a heart-beat messenger and task tracker
	 */
	public SlaveConsole() {
		this.canRun = true;
		this.br = new BufferedReader(new InputStreamReader(System.in));
		this.tracker = new TaskTracker();
		this.messenger = new SlaveMessenger(tracker);
		Thread t = new Thread(messenger);
		t.setDaemon(false);
		t.start();
	}

	/**
	 * Slave shell
	 */
	@Override
	public void run() {
		while (canRun) {
			try {
				String[] in = br.readLine().split(" ");
				switch (in[0]) {
				// print all the jobs
				case "list":
					tracker.list();
					break;
				case "kill":
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
