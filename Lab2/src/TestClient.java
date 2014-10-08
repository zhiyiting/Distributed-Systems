public class TestClient {
		
	public static void main (String args[]) {
		String rHost;
		int rPort;
		// <server ip> <registry port>
		if (args.length != 2) {
			printUsage();
			return;
		}
		try {
			rHost = args[0];
			rPort = Integer.parseInt(args[1]);
		}
		catch (NumberFormatException e) {
			printUsage();
			return;
		}
		// look up a service name
		RMINaming naming = new RMINaming(rHost, rPort);
		Test test = (Test) naming.lookup("obj");
		String result = test.speak();
		System.out.println(result);
		
		
	}
	
	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("-p <port number> -r <registry ip> <registry port number>");
	}
}
