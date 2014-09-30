import java.net.InetAddress;
import java.net.UnknownHostException;


public class Client {
		
	
	public static void main (String args[]) {
		RMINaming naming = null;
		// -p <port number> -r <server ip> <dispatcher port> <registry port>
		if (args.length != 5 || !args[0].equals("-p") || !args[2].equals("-r")) {
			printUsage();
			return;
		}
		try {
			int port = Integer.parseInt(args[1]);
			String rHost = args[3];
			int rPort = Integer.parseInt(args[4]);
			naming = new RMINaming(InetAddress.getLocalHost().getHostAddress(), port, rHost, rPort);
		}
		catch (NumberFormatException e) {
			printUsage();
			return;
		} catch (UnknownHostException e) {
			System.out.println("Can't get local host");
			return;
		}

		Test test = null;
		// look up a service name
		test = (Test) naming.lookup("test");
		String result = test.speak();
		System.out.println(result);
		
		
		
	}
	
	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("-p <port number> -r <registry ip> <registry port number>");
	}
}
