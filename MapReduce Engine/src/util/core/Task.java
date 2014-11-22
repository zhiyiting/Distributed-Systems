package util.core;

import java.io.Serializable;

import util.io.FileSplit;

/**
 * Base class for the task
 * 
 * @author zhiyiting
 *
 */
public class Task implements Serializable {

	private static final long serialVersionUID = -128303213552988241L;

	public enum Status {
		PENDING, RUNNING, FINISHED, STOPPED, FAILED
	}

	private int taskID;
	private Job job;
	private Status status;
	private int slaveID;
	private FileSplit input;
	private char type;

	public int getTaskID() {
		return taskID;
	}

	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getSlaveID() {
		return slaveID;
	}

	public void setSlaveID(int slaveID) {
		this.slaveID = slaveID;
	}

	public FileSplit getInput() {
		return input;
	}

	public void setInput(FileSplit input) {
		this.input = input;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(job.toString());
		return sb.toString();
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	@Override
	public int hashCode() {
		return ((taskID + 1) * 31 + type) ^ job.getId();
	}

	/**
	 * Override the equal function
	 * Determine equality only on taskID, jobID and type
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Task) {
			Task o = (Task) obj;
			return taskID == o.getTaskID() && job.getId() == o.getJob().getId()
					&& type == o.getType();
		}
		return false;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}
}
