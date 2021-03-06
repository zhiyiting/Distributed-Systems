package util.core;

import java.util.ArrayDeque;

import util.api.Mapper;
import util.api.OutputFormat;
import util.core.Task.Status;
import util.dfs.DFSClient;
import util.io.Context;
import util.io.FileSplit;

/**
 * MapWorker class do the map task
 * 
 * @author zhiyiting
 *
 */
public class MapWorker extends Worker {

	public MapWorker(Task t, TaskTracker trk, DFSClient dfs) {
		super(t, trk, dfs);
	}

	@Override
	public void run() {
		Job job = task.getJob();
		System.out.println("Running map task: Job " + job.getId() + " Task "
				+ task.getTaskID());
		task.setStatus(Status.RUNNING);
		try {
			// get new instance of the map and output format
			Class<? extends Mapper> mapcls = job.getMapper();
			Mapper mapper = mapcls.newInstance();
			FileSplit file = task.getInput();
			Class<? extends OutputFormat> outcls = job.getOutputFormat();
			OutputFormat outfm;
			// get the default output format if not defined
			if (outcls == null) {
				outfm = new OutputFormat();
			} else {
				outfm = outcls.newInstance();
			}
			String path = dfs.getFolderPath() + file.getFilename();
			ArrayDeque<String[]> KVPair = outfm.getKVPair(path);
			Context context = new Context(path + "_out", job);
			for (String[] pair : KVPair) {
				mapper.map(pair[0], pair[1], context);
			}
			context.generateOutput();
			// add partition
			tracker.addPartition(job.getId(), context.getPartition());
			tracker.finishMapTask((MapTask) task);
			System.out.println("Job " + job.getId() + " Task "
					+ task.getTaskID() + " finished");
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
