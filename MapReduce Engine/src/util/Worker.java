package util;

public class Worker implements Runnable{

	protected Task task;
	protected TaskTracker tracker;
	
	public Worker(Task t, TaskTracker trk) {
		this.task = t;
		this.tracker = trk;
	}
	
	@Override
	public void run() {
		
		
	}

}
