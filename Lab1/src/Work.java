import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Work extends Thread {

	private Socket socket;
	private ProcessManager processManager;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public Work(Socket s, ProcessManager mgr) {
		this.socket = s;
		this.processManager = mgr;
		try {
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Read Error");
			e.printStackTrace();
		}
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Write Error");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			Object o = in.readObject();
			if (o.equals("connect")) {
				processManager.addSlave(socket.getInetAddress().toString()
						+ ":" + socket.getPort());
				out.writeObject("ACK");
				out.flush();
				return;
			}
			else {
				if (processManager.isMaster) {

				} else {

				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
