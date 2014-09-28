import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

public class RMIServer {
	private static String host;
	private static int port;
	private static String rHost;
	private static int rPort;
	private static Hashtable<Integer, MyRemote> registry = new Hashtable<Integer, MyRemote>();
	static int count;

	public RMIServer() {
	}

	public static void main(String args[]) {
		count = 0;
		// port -r rHost rPort
		if (args.length != 4 || !args[1].equals("-r")) {
			printUsage();
			return;
		}
		try {
			port = Integer.parseInt(args[0]);
			rHost = args[2];
			rPort = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			printUsage();
			return;
		}
		try {
			host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			return;
		}

		Dispatcher dispatcher = new Dispatcher(port);
		Thread t = new Thread(dispatcher);
		t.start();

		CommModule commModule = new CommModule();
		// test if the registry server is there

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				System.out.print(">> ");
				String in = br.readLine();
				String arg[] = in.split(" ");
				if (arg.length <= 0) {
					continue;
				}
				switch (arg[0]) {
				// register and bind a service name with the server and send it
				// to the registry
				// format: register <class name> <service name>
				case "register":
					if (arg.length != 3) {
						printShellUsage();
						continue;
					}
					MyRemote rm;

					try {
						// get the remote object by name
						Class<?> c = Class.forName(arg[1]);
						Constructor<?> ctor = c.getConstructor();
						rm = (MyRemote) ctor.newInstance();
						int oid = getObjectId();
						String riname = rm.getClass().getInterfaces()[0]
								.getName();
						registry.put(oid, rm);
						// get the remote object reference
						RemoteObjectRef ror = new RemoteObjectRef(host, port,
								oid, riname, args[2]);
						RMIMessage msg = new RMIMessage("register", ror, rHost,
								rPort, host, port);
						commModule.send(msg);

					} catch (ClassNotFoundException e) {
						System.out.println("Class Not Found Exception");
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						System.out.println("No Such Method Exception");
						e.printStackTrace();
					} catch (SecurityException e) {
						System.out.println("Security Exception");
						e.printStackTrace();
					} catch (InstantiationException e) {
						System.out.println("Instantiation Exception");
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						System.out.println("Illegal Access Exception");
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						System.out.println("Illegal Argument Exception");
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						System.out.println("Invocation Target Exception");
						e.printStackTrace();
					}

					break;
				case "exit":
					dispatcher.stop();
					return;
				default:
					printShellUsage();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private static int getObjectId() {
		count++;
		return count;
	}

	private static void printShellUsage() {

	}

	private static void printUsage() {
		System.out.println("Usage:\n RMIserver <dispatcher port number>");
	}
}
