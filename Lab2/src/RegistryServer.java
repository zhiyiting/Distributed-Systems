import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Set;

public class RegistryServer {

	private Hashtable<String, RemoteObjectRef> rortbl;

	private ServerSocket serverSocket;

	public RegistryServer(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println("Fail to listen on port " + port);
		}
		rortbl = new Hashtable<String, RemoteObjectRef>();
	}

	private boolean bind(RemoteObjectRef ror) {
		if (rortbl.containsKey(ror.getServiceName())) {
			return false;
		}
		rortbl.put(ror.getServiceName(), ror);
		return true;
	}

	private boolean rebind(RemoteObjectRef ror) {
		if (rortbl.containsKey(ror.getServiceName())) {
			rortbl.put(ror.getServiceName(), ror);
			return true;
		}
		return false;
	}

	private void lookup(RMIMessage msg) {
		String name = (String) msg.getContent();
		Object content = rortbl.get(name);
		RMIMessage m = new RMIMessage("lookup", content, msg.getFromHost(),
				msg.getFromPort(), msg.getToHost(), msg.getToPort());
		CommModule.send(m);
		if (content != null) {
			System.out.println("Found");
		} else {
			System.out.println("Not found");
		}
	}

	private boolean unbind(String name) {
		if (rortbl.remove(name) != null) {
			return true;
		}
		return false;
	}

	public static void main(String args[]) {
		int port;
		if (args.length != 1) {
			printUsage();
			return;
		}
		try {
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			printUsage();
			return;
		}

		RegistryServer regServer = new RegistryServer(port);
		while (true) {
			try {
				Socket socket = regServer.serverSocket.accept();
				ObjectInputStream in = new ObjectInputStream(
						socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(
						socket.getOutputStream());
				Object o = in.readObject();
				if (o != null) {
					RMIMessage msg = (RMIMessage) o;
					String method = msg.getMethod();
					RMIMessage ret = null;
					switch (method) {
					case "bind":
						if (regServer.bind((RemoteObjectRef) msg.getContent())) {
							ret = new RMIMessage("Bind Success");
						} else {
							ret = new RMIMessage("Bind Fail: Service name already exist");
						}
						out.writeObject(ret);
						break;
					case "rebind":
						if (regServer.rebind((RemoteObjectRef) msg.getContent())) {
							ret = new RMIMessage("Rebind Success");
						}
						else {
							ret = new RMIMessage("Rebind Fail: Service name doesn't exist");
						}
						out.writeObject(ret);
						break;
					case "lookup":
						regServer.lookup(msg);
						out.writeObject("ACK");
						break;
					case "unbind":
						if (regServer.unbind((String) msg.getContent())) {
							ret = new RMIMessage("Unbind Success");
						} else {
							ret = new RMIMessage("Unbind Fail: No such service name");
						}
						out.writeObject(ret);
						break;
					default:
						break;
					}

				}
				in.close();
				out.close();
				socket.close();
				System.out.println("------------");
				Set<String> keys = regServer.rortbl.keySet();
				for (String key: keys) {
					System.out.println(key + "  " + regServer.rortbl.get(key).getServiceName());
				}

			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private static void printUsage() {
		System.out.println("Usage:\n RegistryServer <registry port number>");
	}

}