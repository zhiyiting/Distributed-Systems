import java.net.InetAddress;
import java.net.UnknownHostException;


public class MapReduceEngine {
	public static void main(String[] args) {
		Configuration conf = new Configuration();
		String masterAdd = (String) conf.get("master address");
		String host = null;
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			System.out.println("Start map reduce engine failed");
			e.printStackTrace();
			return;
		}
		// determine if the node is master/slave
		if (host == masterAdd) {
			System.out.println("this is master");
			Coordinator coord = new Coordinator();
			coord.run();		
		}
		else {
			System.out.println("this is worker");
			Worker worker = new Worker();
			worker.run();
		}
	}
}
