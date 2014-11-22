package util.comm;

import util.core.Job;

/**
 * Message to be transmitted for job purpose
 * 
 * @author zhiyiting
 *
 */
public class JobMessage extends Message {

	private static final long serialVersionUID = 1419967613654300719L;
	private Job job;

	/**
	 * Function to construct a job message
	 * 
	 * @param content
	 * @param job
	 * @param toHost
	 * @param toPort
	 */
	public JobMessage(String content, Job job, String toHost, int toPort) {
		super(content, toHost, toPort);
		this.job = job;
	}

	/**
	 * Function to get the job information
	 * 
	 * @return job
	 */
	public Job getJob() {
		return this.job;
	}
}