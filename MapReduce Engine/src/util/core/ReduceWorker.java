package util.core;

import java.util.ArrayDeque;
import java.util.Map.Entry;
import java.util.TreeMap;

import util.api.Reducer;
import util.core.Task.Status;
import util.dfs.DFSClient;
import util.io.Context;

public class ReduceWorker extends Worker {

	public ReduceWorker(Task t, TaskTracker trk, DFSClient dfs) {
		super(t, trk, dfs);
	}

	@Override
	public void run() {
		Job job = task.getJob();
		System.out.println("running reduce task for job#" + job.getId());
		task.setStatus(Status.RUNNING);
		try {
			Class<? extends Reducer> reducecls = job.getReducer();
			int jobID = job.getId();
			Reducer reducer = reducecls.newInstance();
			String path = dfs.getFolderPath() + job.conf.OUTPUT_DIR
					+ "job" + jobID + "_part" + task.getSlaveID();
			((ReduceTask) task).setOutput(path);
			Context context = new Context(path, job);
			TreeMap<String, ArrayDeque<String>> partition = dfs
					.getPartition(jobID);
			for (Entry<String, ArrayDeque<String>> item : partition.entrySet()) {
				String key = item.getKey();
				ArrayDeque<String> values = item.getValue();
				reducer.reduce(key, values, context);
			}
			context.generateOutput();
			tracker.finishReduceTask((ReduceTask) task);
			System.out.println("Reduce #" + task.getJob().getId()
					+ " finished.");
			System.out.println("Output location: " + path);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
