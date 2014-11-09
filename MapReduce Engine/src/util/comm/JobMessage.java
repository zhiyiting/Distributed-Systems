package util.comm;

import util.core.Job;

public class JobMessage extends Message {
	
	private static final long serialVersionUID = 1419967613654300719L;
	private Job job;

	public JobMessage(String content, Job job, String toHost, int toPort) {
		super(content, toHost, toPort);
		this.job = job;
	}
	
	public Job getJob() {
		return this.job;
	}
}