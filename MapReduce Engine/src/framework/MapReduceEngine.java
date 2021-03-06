package framework;

import java.net.InetAddress;
import java.net.UnknownHostException;

import conf.Configuration;
import util.console.CoordinatorConsole;
import util.console.SlaveConsole;

/**
 * The Map Reduce framework
 * 
 * @author zhiyiting
 *
 */
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
		// the master host is configured in the configuration file
		if (host.equals(Configuration.MASTER_ADDRESS)) {
			System.out.println("Running coordinator on " + host);
			CoordinatorConsole coord = new CoordinatorConsole();
			coord.run();
		} else {
			SlaveConsole slave = new SlaveConsole();
			try {
				System.out.println("Created local file system at "
						+ InetAddress.getLocalHost().getHostName() + "/");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			slave.run();
		}
	}
}
