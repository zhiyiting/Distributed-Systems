package client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import conf.Configuration;
import util.console.CoordinatorConsole;
import util.console.SlaveConsole;

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
		if (host.equals(Configuration.MASTER_ADDRESS)) {
			System.out.println("Running coordinator on " + host);
			CoordinatorConsole coord = new CoordinatorConsole();
			coord.run();		
		}
		else {
			System.out.println("this is worker");
			SlaveConsole slave = new SlaveConsole();
			slave.run();
		}
	}
}
