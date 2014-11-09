package util.core;

import java.util.ArrayDeque;

import conf.Configuration;

public class TaskTracker {

	private ArrayDeque<MapTask> mapTasks;
	private ArrayDeque<ReduceTask> reduceTasks;
	private ArrayDeque<Task> finishedTasks;
	private int maxMapSlot;
	private int maxReduceSlot;

	public TaskTracker() {
		this.maxMapSlot = Configuration.MAP_PER_NODE;
		this.maxReduceSlot = Configuration.REDUCE_PER_NODE;
		this.mapTasks = new ArrayDeque<MapTask>(maxMapSlot);
		this.reduceTasks = new ArrayDeque<ReduceTask>(maxReduceSlot);
		this.finishedTasks = new ArrayDeque<Task>();
	}

	public synchronized int getIdleMapSlot() {
		return maxMapSlot - mapTasks.size();
	}

	public synchronized int getIdleReduceSlot() {
		return maxReduceSlot - reduceTasks.size();
	}

	public synchronized void addMapTask(ArrayDeque<MapTask> task) {
		for (MapTask t : task) {
			MapWorker worker = new MapWorker(t, this);
			Thread thread = new Thread(worker);
			thread.start();
			mapTasks.addLast(t);
		}
	}

	public synchronized void addReduceTask(ArrayDeque<ReduceTask> task) {
		for (ReduceTask t : task) {
			reduceTasks.addLast(t);
		}
	}

	public synchronized void finishMapTask(MapTask task) {
		mapTasks.remove(task);
		finishedTasks.addLast(task);
	}

	public synchronized void finishReduceTask(ReduceTask task) {
		reduceTasks.remove(task);
		finishedTasks.addLast(task);
	}

	public synchronized ArrayDeque<Task> getFinishedTasks() {
		return finishedTasks;
	}

	public synchronized void list() {
		System.out.println("Current job: ");
		System.out.println("Map tasks");
		if (mapTasks.size() > 0) {
			for (Task task : mapTasks) {
				System.out.println(task.toString());
			}
		} else {
			System.out.println("empty");
		}
		System.out.println("Reduce tasks");
		if (reduceTasks.size() > 0) {
			for (Task task : reduceTasks) {
				System.out.println(task.toString());
			}
		} else {
			System.out.println("empty");
		}

	}
}
