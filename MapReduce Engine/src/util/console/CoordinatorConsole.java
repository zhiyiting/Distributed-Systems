package util.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import util.core.JobTracker;

public class CoordinatorConsole implements Runnable {
	private boolean canRun;
	private BufferedReader br;
	private JobTracker tracker;
	
	public CoordinatorConsole() {
		this.canRun = true;
		this.br = new BufferedReader(new InputStreamReader(System.in));
		this.tracker = new JobTracker();
	}

	@Override
	public void run() {
		while (canRun) {
			try {
				String in = br.readLine();
				switch (in) {
				// print all the jobs and associated mappers
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
