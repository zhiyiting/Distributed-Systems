package util.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import util.comm.CommModule;
import util.comm.JobMessage;
import util.comm.Message;
import util.comm.RemoteException;
import util.core.Job;
import conf.Configuration;

public class ClientConsole implements Runnable{

	private Job job;
	private BufferedReader br;
	private CommModule commModule;
	private String toHost;
	private int toPort;
	
	public ClientConsole(Job job) {
		this.job = job;
		this.br = new BufferedReader(new InputStreamReader(System.in));
		this.commModule = new CommModule();
		this.toHost = Configuration.MASTER_ADDRESS;
		this.toPort = Configuration.SERVER_PORT;
		startJob();
	}
	
	private void startJob() {
		JobMessage msg = new JobMessage("start", job, toHost, toPort);
		try {
			Message ret = commModule.send(msg);
			System.out.println(ret.getContent());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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

}
