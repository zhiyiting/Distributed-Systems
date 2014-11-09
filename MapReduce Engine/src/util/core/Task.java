package util.core;

import java.io.Serializable;

import util.io.FileSplit;

public class Task implements Serializable {

	private static final long serialVersionUID = -128303213552988241L;

	enum Status {
		PENDING, RUNNING, FINISHED, STOPPED, FAILED
	}

	private int taskID;
	private Job job;
	private Status status;
	private int slaveID;
	private FileSplit input;
	private String outputPath;

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

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
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
		return ((taskID + 1) * 31) ^ job.getId();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Task) {
			Task o = (Task)obj;
			return taskID == o.getTaskID() && job.getId() == o.getJob().getId();
		}
		return false;
	}
}
