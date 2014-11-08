package util;

import java.util.ArrayDeque;

import conf.Configuration;

public class TaskTracker {

	private ArrayDeque<MapTask> mapTasks;
	private ArrayDeque<ReduceTask> reduceTasks;
	private ArrayDeque<Task> finishedTasks;
	
	public TaskTracker() {
		this.mapTasks = new ArrayDeque<MapTask>(Configuration.MAP_PER_NODE);
		this.reduceTasks = new ArrayDeque<ReduceTask>(Configuration.REDUCE_PER_NODE);
		this.finishedTasks = new ArrayDeque<Task>();
	}

	public int getIdleMapSlot() {
		return Configuration.MAP_PER_NODE - mapTasks.size();
	}
	
	public int getIdleReduceSlot() {
		return Configuration.REDUCE_PER_NODE - reduceTasks.size();
	}
	
	public void addMapTask(ArrayDeque<MapTask> task) {
		for (MapTask t: task) {
			startMap(t);
			mapTasks.addLast(t);
		}
	}
	
	private void startMap(Task t) {
		
	}
	
	public void addReduceTask(ArrayDeque<ReduceTask> task) {
		for (ReduceTask t: task) {
			startReduce(t);
			reduceTasks.addLast(t);
		}
	}
	
	private void startReduce(Task t) {
		
	}
	
	public void finishMapTask(MapTask task) {
		mapTasks.remove(task);
		finishedTasks.addLast(task);
	}
	
	public void finishReduceTask(ReduceTask task) {
		reduceTasks.remove(task);
		finishedTasks.addLast(task);
	}
	
	public ArrayDeque<Task> getFinishedTasks() {
		return finishedTasks;
	}
	
	public void list() {
		System.out.println("Current job: ");
		System.out.println("Map tasks");
		if (mapTasks.size() > 0) {
			for (Task task: mapTasks) {
				System.out.println(task.toString());
			}
		}
		else {
			System.out.println("empty");
		}
		System.out.println("Reduce tasks");
		if (reduceTasks.size() > 0) {
			for (Task task: reduceTasks) {
				System.out.println(task.toString());
			}
		}
		else {
			System.out.println("empty");
		}
		
	}
}
