package util;

public class JobMessage extends Message {

	private Job job;

	public JobMessage(String content, Job job, String toHost, int toPort) {
		super(content);
		this.job = job;
	}
	
	public Job getJob() {
		return this.job;
	}
}
