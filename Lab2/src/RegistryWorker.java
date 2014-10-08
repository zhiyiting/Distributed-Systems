import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RegistryWorker implements Runnable {

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private RegistryServer regServer;

	public RegistryWorker(Socket socket, RegistryServer regServer) {
		try {
			this.in = new ObjectInputStream(socket.getInputStream());
			this.out = new ObjectOutputStream(socket.getOutputStream());
			this.regServer = regServer;
		} catch (IOException e) {
			System.out.println("Registry Worker: fail to create socket");
		}
	}

	@Override
	public void run() {
		while (true) {
			System.out.println("thread running");
			try {
				Object o = in.readObject();
				if (o != null) {
					RMIMessage msg = (RMIMessage) o;
					// get the method type from incoming message
					String method = (String) msg.getMethod();
					System.out.println(method);
					RMIMessage ret = null;
					switch (method) {
					case "bind":
						if (regServer.bind((RemoteObjectRef) msg.getContent())) {
							ret = new RMIMessage("Bind Success");
						} else {
							ret = new RMIMessage(
									"Bind Fail: Service name already exist");
						}
						out.writeObject(ret);
						break;
					case "rebind":
						if (regServer
								.rebind((RemoteObjectRef) msg.getContent())) {
							ret = new RMIMessage("Rebind Success");
						} else {
							ret = new RMIMessage(
									"Rebind Fail: Service name doesn't exist");
						}
						out.writeObject(ret);
						break;
					case "lookup":
						RemoteObjectRef ror = regServer.lookup((String) msg
								.getContent());
						ret = new RMIMessage(ror);
						out.writeObject(ret);
						break;
					case "unbind":
						if (regServer.unbind((String) msg.getContent())) {
							ret = new RMIMessage("Unbind Success");
						} else {
							ret = new RMIMessage(
									"Unbind Fail: No such service name");
						}
						out.writeObject(ret);
						break;
					default:
						break;
					}
				}
			} catch (ClassNotFoundException e) {
				System.out.println("RegistryWorker: Class Not Found Exception" + e.getMessage());
			} catch (IOException e) {
				break;
			}
		}
	}
}
