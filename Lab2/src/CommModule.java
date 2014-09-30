import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class CommModule {

	public static Object send(RMIMessage msg) {
		try {
			Socket socket = new Socket(msg.getToHost(), msg.getToPort());

			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());

			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());

			out.writeObject(msg);
			out.flush();
			Object o = in.readObject();
			out.close();
			socket.close();
			return o;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void sendACK(String toHost, int toPort) {

		try {
			Socket socket = new Socket(toHost, toPort);
			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());
			out.writeObject("ACK");
			out.flush();
			out.close();
			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
