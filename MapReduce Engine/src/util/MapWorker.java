package util;

import util.Task.Status;

public class MapWorker extends Worker {

	public MapWorker(Task t, TaskTracker trk) {
		super(t, trk);
	}

	@Override
	public void run() {
		Job job = task.getJob();
		task.setStatus(Status.RUNNING);
		Class<? extends Mapper> cls = job.getMapper();
		Mapper mapper = cls.newInstance();
		FileSplit file = task.getInput();
		RecordReader reader = new RecordReader(file);
		String[][] KVPair = reader.getKVPair();
		Context context = new Context();
		for (String[] pair : KVPair) {
			mapper.map(pair[0], pair[1], context);
		}
		tracker.finishMapTask((MapTask)task);
	}
}
