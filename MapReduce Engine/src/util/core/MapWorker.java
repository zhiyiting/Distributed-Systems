package util.core;

import java.util.ArrayDeque;

import util.core.Task.Status;
import util.io.Context;
import util.io.FileSplit;
import util.io.RecordReader;

public class MapWorker extends Worker {

	public MapWorker(Task t, TaskTracker trk) {
		super(t, trk);
	}

	@Override
	public void run() {
		Job job = task.getJob();
		task.setStatus(Status.RUNNING);
		Class<? extends Mapper> cls = job.getMapper();
		try {
			Mapper mapper = cls.newInstance();
			FileSplit file = task.getInput();
			RecordReader reader = new RecordReader(file);
			ArrayDeque<String[]> KVPair = reader.getKVPair();
			Context context = new Context(task.getOutputPath());
			for (String[] pair : KVPair) {
				mapper.map(pair[0], pair[1], context);
			}
			context.generateOutput();
			tracker.finishMapTask((MapTask)task);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
