package util.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
public class ClientConsole implements Runnable {

	private Job job;
	private BufferedReader br;
	private CommModule commModule;
	private String toHost;
	private int toPort;
	private boolean fin = false;

	/**
	 * Start the listener at the client node
	 * 
	 * @param job
	 */
	public ClientConsole(Job job) {
		this.job = job;
		this.br = new BufferedReader(new InputStreamReader(System.in));
		this.commModule = new CommModule();
		this.toHost = Configuration.MASTER_ADDRESS;
		this.toPort = Configuration.SERVER_PORT;
		ClientListener listener = new ClientListener(Configuration.CLIENT_PORT);
		Thread thread = new Thread(listener);
		thread.start();
		startJob();
	}

	/**
	 * Start the job
	 */
	private void startJob() {
		JobMessage msg = new JobMessage("start", job, toHost, toPort);
		System.out.println("Distributing file on DFS...");
		try {
			Message ret = commModule.send(msg);
			System.out.println(ret.getContent());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Client shell to print current status of jobs
	 */
	@Override
	public void run() {
		while (true) {
			try {
				String in = br.readLine();
				switch (in) {
				case "list": {
					Message msg = new Message("list", toHost, toPort);
					Message ret = commModule.send(msg);
					System.out.println(ret.getContent());
					break;
				}
				// stop the node
				case "stop": {
					Message msg = new Message("stop", toHost, toPort);
					Message ret = commModule.send(msg);
					System.out.println(ret.getContent());
					break;
				}
				default:
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public boolean isFinished() {
		return fin;
	}

	public void downloadOutput(String path) {

	}

}
