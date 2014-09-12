import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Listener extends Thread {
	private int port;
	public ServerSocket serverSocket;
	private ProcessManager processManager;

	public Listener(int p, ProcessManager mgr) {
		this.port = p;
		this.processManager = mgr;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not listen on port " + port);
			System.exit(-1);
		}
	}

	public void run() {
		if (processManager.isMaster) {
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					ObjectInputStream in = new ObjectInputStream(
							socket.getInputStream());
					ObjectOutputStream out = new ObjectOutputStream(
							socket.getOutputStream());
					Object o = in.readObject();
					if (o != null) {
						String message = o.toString();
						if (message.contains("connect")) {
							String[] ip = message.split(" ");
							processManager.addSlave(ip[1] + ":" + ip[2]);
							out.writeObject("connected");
							out.flush();
						}
						if (message.contains("die")) {
							String [] str = message.split(" ");
							processManager.dieProcess(Integer.parseInt(str[1]));
							out.writeObject("ok");
							out.flush();
						}
					}
					in.close();
					out.close();
				} catch (IOException e) {
					System.out.println("IOException");
				} catch (ClassNotFoundException e) {
					System.out.println("Class Not Found");
				}
			}
		} else {
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					ObjectInputStream in = new ObjectInputStream(
							socket.getInputStream());
					ObjectOutputStream out = new ObjectOutputStream(
							socket.getOutputStream());
					Object o = in.readObject();
					if (o != null) {
						String message = o.toString();
						String[] cmd = message.split(" ");
						switch (cmd[0]) {
						case "run":
							if (cmd.length != 2) {
								System.out.println("deserialize error");
							} else {
								if (processManager.doLaunch(Integer.parseInt(cmd[1])) == 1) {
									out.writeObject("ok");
								} else {
									System.out
											.println("Fail to work on process "
													+ cmd[1]);
									out.writeObject("bad");
								}
								out.flush();
							}
							break;
						case "suspend":
							if (cmd.length != 2) {
								System.out.println("cmd length error");
								return;
							}
							if (processManager.doSuspend(Integer.parseInt(cmd[1])) == 1) {
								System.out.println("suspend ok");
								out.writeObject("ok");
							}
							else {
								System.out.println("suspend fail");
								out.writeObject("bad");
							}
							break;
						
						case "check":
							out.writeObject("hi");
							out.flush();
							break;
						default:
							break;
						}
					}
					in.close();
					out.close();
				} catch (IOException e) {
					System.out.println("IOException");
				} catch (ClassNotFoundException e) {
					System.out.println("Class Not Found");
				}
			}
		}

	}
}
