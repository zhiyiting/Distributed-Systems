package util;

import java.util.ArrayDeque;

public class TaskMessage extends Message {

	private static final long serialVersionUID = -7201557988860331942L;
	private ArrayDeque<MapTask> mapTask;
	private ArrayDeque<ReduceTask> reduceTask;

	public TaskMessage(String content) {
		super(content);
		setMapTask(new ArrayDeque<MapTask>());
		setReduceTask(new ArrayDeque<ReduceTask>());
	}

	public ArrayDeque<MapTask> getMapTask() {
		return mapTask;
	}

	public void setMapTask(ArrayDeque<MapTask> mapTask) {
		this.mapTask = mapTask;
	}

	public ArrayDeque<ReduceTask> getReduceTask() {
		return reduceTask;
	}

	public void setReduceTask(ArrayDeque<ReduceTask> reduceTask) {
		this.reduceTask = reduceTask;
	}
}
