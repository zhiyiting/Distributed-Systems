package util.core;

import util.dfs.DFSClient;

/**
 * Base class of the worker
 * 
 * @author zhiyiting
 *
 */
public class Worker implements Runnable {

	protected Task task;
	protected TaskTracker tracker;
	protected DFSClient dfs;

	public Worker(Task t, TaskTracker trk, DFSClient dfs) {
		this.task = t;
		this.tracker = trk;
		this.dfs = dfs;
	}

	@Override
	public void run() {

	}

}
