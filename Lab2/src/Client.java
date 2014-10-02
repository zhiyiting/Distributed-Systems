import java.net.InetAddress;
import java.net.UnknownHostException;


public class Client {
		
	public static void main (String args[]) {
		RMINaming naming = null;
		// <server ip> <registry port>
		if (args.length != 2) {
			printUsage();
			return;
		}
		try {
			String rHost = args[0];
			int rPort = Integer.parseInt(args[1]);
			naming = new RMINaming(rHost, rPort);
		}
		catch (NumberFormatException e) {
			printUsage();
			return;
		}
		Test test = null;
		// look up a service name
		test = (Test) naming.lookup("obj");
		String result = "";
		if (test != null) {
			System.out.println("Run function");
			result = test.speak();
			System.out.println(result);
		}		
		
		
	}
	
	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("-p <port number> -r <registry ip> <registry port number>");
	}
}
