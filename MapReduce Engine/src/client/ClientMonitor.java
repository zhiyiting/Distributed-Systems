package client;

import util.Job;

public class ClientMonitor implements Runnable{

	private Job job;
	
	public ClientMonitor(Job job) {
		this.job = job;
	}
	
	@Override
	public void run() {
		while (true) {
			
		}
		
	}

}
