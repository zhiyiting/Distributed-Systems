import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;

public class RegistryServer extends Listener {

	private Hashtable<String, RemoteObjectRef> rortbl;

	private static String host;
	private static int port;
	private CommModule commModule;

	public RegistryServer(int port) {
		super(port);
		rortbl = new Hashtable<String, RemoteObjectRef>();
	}

	public void register() {
		commModule = new CommModule();
	}

	@Override
	public void run() {
		while (canRun) {
			try {
				Socket socket = serverSocket.accept();
				ObjectInputStream in = new ObjectInputStream(
						socket.getInputStream());
				Object o = in.readObject();
				if (o != null) {
					RMIMessage msg = (RMIMessage) o;
					String method = msg.getMethod();
					switch (method) {
					case "bind":
					case "rebind": {
						RemoteObjectRef ror = (RemoteObjectRef) msg
								.getContent();
						rortbl.put(ror.getServiceName(), ror);
						System.out.println("Service bound: "
								+ ror.getInterfaceName());
					}
						break;

					case "lookup": {
						String serviceName = (String) msg.getContent();
						Object content = rortbl.get(serviceName);
						RMIMessage m = new RMIMessage("lookup", content,
								msg.getFromHost(), msg.getFromPort(), host,
								port);
						commModule.send(m);
					}
						break;
					case "unbind": {
						String serviceName = (String) msg.getContent();
						rortbl.remove(serviceName);
						System.out.println("Service removed: " + serviceName);
					}
						break;
					default:
						break;
					}

				}
				in.close();
				socket.close();

			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}

	}

	public static void main(String args[]) {
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
		try {
			host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			return;
		}

		RegistryServer registryServer = new RegistryServer(port);
		Thread t = new Thread(registryServer);
		t.start();
	}

	private static void printUsage() {
		System.out.println("Usage:\n RegistryServer <registry port number>");
	}

}