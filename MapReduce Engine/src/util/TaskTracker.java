package util;

import java.util.ArrayDeque;

import conf.Configuration;

public class TaskTracker {

	private ArrayDeque<Task> mapTasks;
	private ArrayDeque<Task> reduceTasks;
	private ArrayDeque<Task> finishedTasks;
	
	public TaskTracker() {
		this.mapTasks = new ArrayDeque<Task>(Configuration.MAP_PER_NODE);
		this.reduceTasks = new ArrayDeque<Task>(Configuration.REDUCE_PER_NODE);
		this.finishedTasks = new ArrayDeque<Task>();
	}
	
	public boolean hasFinishedTasks() {
		return finishedTasks.size() > 0;
	}
	
	public int getIdleMapSlot() {
		return Configuration.MAP_PER_NODE - mapTasks.size();
	}
	
	public int getIdleReduceSlot() {
		return Configuration.REDUCE_PER_NODE - reduceTasks.size();
	}
	
	public ArrayDeque<Integer> getFinishedTaskID() {
		ArrayDeque<Integer> result = new ArrayDeque<Integer>();
		for (Task task: finishedTasks) {
			result.add(task.getID());
		}
		return result;
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
