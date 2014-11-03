package client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import conf.Configuration;
import util.CoordinatorConsole;
import util.Worker;

public class MapReduceEngine {
	public static void main(String[] args) {
		String host = null;
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			System.out.println("Start map reduce engine failed");
			e.printStackTrace();
			return;
		}
		// determine if the node is master/slave
		if (host.equals(Configuration.MASTERADDRESS)) {
			System.out.println("Running coordinator on " + host);
			CoordinatorConsole coord = new CoordinatorConsole();
			coord.run();		
		}
		else {
			System.out.println("this is worker");
			Worker worker = new Worker();
			worker.run();
		}
	}
}
