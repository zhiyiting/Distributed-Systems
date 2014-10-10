import java.lang.reflect.UndeclaredThrowableException;

public class TestClient {

	public static void main(String args[]) {
		String rHost;
		int rPort;
		String serviceName;
		String parameter = null;
		// <server ip> <registry port> <service name> (<argument>)
		if (args.length != 3 && args.length != 4) {
			printUsage();
			return;
		}
		try {
			rHost = args[0];
			rPort = Integer.parseInt(args[1]);
			serviceName = args[2];
			if (args.length == 4) {
				parameter = args[3];
			}
		} catch (NumberFormatException e) {
			printUsage();
			return;
		}
		// look up a service name
		RMINaming naming = new RMINaming(rHost, rPort);
		try {
			Test test = (Test) naming.lookup(serviceName);
			String result;
			if (parameter == null) {
				result = test.speak();
			} else {
				result = test.speak(parameter);
			}
			System.out.println(result);

		} catch (UndeclaredThrowableException e) {
			System.out.println(e.getCause().getMessage());
			return;
		} catch (RemoteException e) {
			System.out.println(e.getMessage());
			return;
		}
	}

	private static void printUsage() {
		System.out.println("Usage:");
		System.out
				.println("<registry ip> <registry port number> <service name> (<argument>)");
	}
}
