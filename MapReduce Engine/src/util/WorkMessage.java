package util;

import java.util.ArrayDeque;

public class WorkMessage extends Message {

	private static final long serialVersionUID = -8909797683217029704L;

	private int mapSlot;
	private int reduceSlot;
	private int slaveID;
	private ArrayDeque<Task> finishedTask;
	
	public WorkMessage(String content, String toHost, int toPort) {
		super(content, toHost, toPort);
	}
	
	public WorkMessage(String content) {
		super(content);
	}
	
	public int getMapSlot() {
		return this.mapSlot;
	}
	
	public void setMapSlot(int n) {
		this.mapSlot = n;
	}
	
	public int getReduceSlot() {
		return this.reduceSlot;
	}
	
	public void setReduceSlot(int n) {
		this.reduceSlot = n;
	}
	
	public ArrayDeque<Task> getFinishedTask() {
		return finishedTask;
	}
	
	public void setFinishedTask(ArrayDeque<Task> list) {
		finishedTask = list;
	}

	public int getSlaveID() {
		return slaveID;
	}

	public void setSlaveID(int slaveID) {
		this.slaveID = slaveID;
	}

}
