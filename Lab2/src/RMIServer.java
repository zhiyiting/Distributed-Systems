import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * RMIServer class that serves remote objects
 * 
 * @author zhiyiting
 *
 */
public class RMIServer {
	private static CommModule commModule;
	private static String host;
	private static int port;
	private static int rPort;
	static int count;

	public RMIServer() {
	}

	/**
	 * main method to start a server
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		// argument format:
		// <server port number> -r <registry port number>
		if (args.length != 3 || !args[1].equals("-r")) {
			printUsage();
			return;
		}
		try {
			port = Integer.parseInt(args[0]);
			rPort = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			printUsage();
			return;
		}
		// get local ip address for reference
		try {
			host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			return;
		}

		commModule = new CommModule();

		// start a dispatcher thread that listens to client messages
		ServerListener sl = new ServerListener(port);
		Thread t = new Thread(sl);
		t.start();

		// create a shell that provides bind, unbind and rebind methods
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				String in = br.readLine();
				String arg[] = in.split(" ");
				if (arg.length <= 0) {
					continue;
				}
				RMIMessage ret = null;

				switch (arg[0]) {
				// register and bind a service name with the server and send it
				// to the registry
				// format: bind <class name> <service name>
				case "bind": {
					if (arg.length != 3) {
						printShellUsage();
						continue;
					}
					MyRemote rm;
					// get the remote object by name
					Class<?> c = Class.forName(arg[1]);
					Constructor<?> ctor = c.getConstructor();
					rm = (MyRemote) ctor.newInstance();
					String riname = rm.getClass().getInterfaces()[0].getName();
					String serviceName = arg[2];
					// put the object to dispatcher for reference
					sl.add(serviceName, rm);
					// get the remote object reference
					RemoteObjectRef ror = new RemoteObjectRef(host, port,
							serviceName, riname);
					// compose a bind request
					// send it through communication module
					RMIMessage msg = new RMIMessage("bind", ror, host, rPort);
					ret = (RMIMessage) commModule.send(msg);
					System.out.println(ret.getContent());
				}
					break;
				// update a service name with the server and send it to
				// the registry
				// format: rebind <class name> <service name>
				case "rebind": {
					if (arg.length != 3) {
						printShellUsage();
						continue;
					}
					MyRemote rm;
					// get the remote object by name
					Class<?> c = Class.forName(arg[1]);
					Constructor<?> ctor = c.getConstructor();
					rm = (MyRemote) ctor.newInstance();
					String riname = rm.getClass().getInterfaces()[0].getName();
					// get the remote object reference
					RemoteObjectRef ror = new RemoteObjectRef(host, port,
							arg[2], riname);
					RMIMessage msg = new RMIMessage("rebind", ror, host, rPort);
					ret = (RMIMessage) commModule.send(msg);
					System.out.println(ret.getContent());
				}
					break;
				// Unbind a service from the registry
				// format: unbind <service name>
				case "unbind": {
					if (arg.length != 2) {
						printShellUsage();
						continue;
					}
					RMIMessage msg = new RMIMessage("unbind", arg[1], host,
							rPort);
					ret = (RMIMessage) commModule.send(msg);
					System.out.println(ret.getContent());
				}
					break;
				default:
					printShellUsage();
					break;

				}
			} catch (IOException e) {
				System.out.println("IO Exception");
				e.printStackTrace();
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
		}

	}

	/**
	 * function to print the usage of server shell
	 */
	private static void printShellUsage() {
		System.out.println("Usage:");
		System.out.println("bind <class name> <service name>");
		System.out.println("rebind <class name> <service name>");
		System.out.println("unbind <service name>");
	}

	/**
	 * function to print usage of rmi server
	 */
	private static void printUsage() {
		System.out.println("Usage:\nRMIserver <dispatcher port number>");
	}
}
