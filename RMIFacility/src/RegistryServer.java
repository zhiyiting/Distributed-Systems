import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

/**
 * Registry Server that keeps reference of remote objects
 * It listens and accepts requests to bind, unbind and lookup
 * 
 * @author zhiyiting
 *
 */
public class RegistryServer {

	// hashtable store remote object references by interface name
	private Hashtable<String, RemoteObjectRef> rortbl;
	// server socket for listening requests
	private ServerSocket serverSocket;

	/**
	 * Constructor for the registry server
	 * @param port
	 */
	public RegistryServer(int port) {
		try {
			// listen at a given port
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println("Fail to listen on port " + port);
		}
		System.out.println("Registry server started, listening on port " + port);
		rortbl = new Hashtable<String, RemoteObjectRef>();
	}

	/**
	 * Insert a remote object reference into table
	 * @param ror
	 * @return success (true), fail (false)
	 */
	public boolean bind(RemoteObjectRef ror) {
		// if the ror already exists, don't bind
		if (rortbl.containsKey(ror.getServiceName())) {
			return false;
		}
		rortbl.put(ror.getServiceName(), ror);
		return true;
	}

	/**
	 * Assign new value to the existing ror
	 * @param ror
	 * @return sucess (true), fail (false)
	 */
	public boolean rebind(RemoteObjectRef ror) {
		// if the table doesn't contain the service name
		// do not bind a new one
		if (rortbl.containsKey(ror.getServiceName())) {
			rortbl.put(ror.getServiceName(), ror);
			return true;
		}
		return false;
	}

	/**
	 * Look up if it contains the service name
	 * @param serviceName
	 * @return remote object reference of service name
	 */
	public RemoteObjectRef lookup(String serviceName) {
		RemoteObjectRef content = rortbl.get(serviceName);
		return content;
	}

	/**
	 * Remove the service name from the table
	 * @param name
	 * @return success (true), fail (false)
	 */
	public boolean unbind(String name) {
		// if the table doesn't contain the service name
		// return false and let user know
		if (rortbl.remove(name) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Main function
	 * @param args format: <port number>
	 */
	public static void main(String args[]) {
		int port;
		if (args.length != 1) {
			printUsage();
			return;
		}
		try {
			// parse the port number from input argument
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			printUsage();
			return;
		}

		// instantiate a new registry server object
		RegistryServer regServer = new RegistryServer(port);
		// registry start to listen to input connections
		// the incoming messages should only be the following types
		// from client: lookup
		// from service server: bind, unbind, rebind
		while (true) {
			try {
				Socket socket = regServer.serverSocket.accept();
				new Thread(new RegistryWorker(socket, regServer)).start();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	/**
	 * Print the usage of registry server
	 */
	private static void printUsage() {
		System.out.println("Usage:\n RegistryServer <registry port number>");
	}

}