package util.console;

import util.comm.ClientHeartBeat;
import util.comm.ClientListener;
import util.comm.CommModule;
import util.comm.JobMessage;
import util.comm.Message;
import util.comm.RemoteException;
import util.core.Job;
import conf.Configuration;

/**
 * Client node
 * 
 * @author zhiyiting
 *
 */
public class JobClient {

	private Job job;
	private CommModule commModule;
	private String toHost;
	private int toPort;

	/**
	 * Start the listener at the client node
	 * 
	 * @param job
	 */
	public JobClient(Job job) {
		this.job = job;
		this.commModule = new CommModule();
		this.toHost = Configuration.MASTER_ADDRESS;
		this.toPort = Configuration.SERVER_PORT;
		ClientListener listener = new ClientListener(Configuration.CLIENT_PORT);
		Thread thread = new Thread(listener);
		thread.start();
	}

	/**
	 * Start the job
	 */
	public void startJob() {
		JobMessage msg = new JobMessage("start", job, toHost, toPort);
		System.out.println("Distributing files on DFS...");
		try {
			Message ret = commModule.send(msg);
			job.setId(Integer.parseInt(ret.getContent()));
			System.out.println("Job #" + job.getId() + " " + job.getName()
					+ " started");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("Status:\n0%");
		ClientHeartBeat heartbeat = new ClientHeartBeat(job);
		Thread t = new Thread(heartbeat);
		t.start();
	}
	
}
