import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Communication Module
 * 
 * @author zhiyiting
 *
 */
public class CommModule {

	SocketCache socketCache;
	
	public CommModule() {
		socketCache = new SocketCache();
	}
	
	public static Object sendStatic(RMIMessage msg) {
		try {
			Socket socket = new Socket(msg.getToHost(), msg.getToPort());
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			out.writeObject(msg);
			out.flush();
			// get the return message from the socket
			Object o = in.readObject();
			out.close();
			in.close();
			socket.close();
			return o;

		} catch (UnknownHostException e) {
			System.out.println("Unknown Host Exception");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO Exception");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Class Not Found Exception");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * function to send and receive message
	 * 
	 * @param message
	 * @return return message
	 */
	public Object send(RMIMessage msg) {
		try {
			ObjectOutputStream out;
			ObjectInputStream in;
			if (socketCache.contains(msg.getToHost(), msg.getToPort())) {
				SocketInfo socketObj = socketCache.get(msg.getToHost(), msg.getToPort());
				out = socketObj.out;
				in = socketObj.in;
			} else {
				// establish a new socket
				Socket socket = new Socket(msg.getToHost(), msg.getToPort());
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
				SocketInfo socketInfo = new SocketInfo(socket, out, in);
				socketCache.put(msg.getToHost(), msg.getToPort(), socketInfo);
			}

			out.writeObject(msg);
			out.flush();
			// get the return message from the socket
			Object o = in.readObject();
			return o;

		} catch (UnknownHostException e) {
			System.out.println("Unknown Host Exception");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO Exception");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Class Not Found Exception");
			e.printStackTrace();
		}
		return null;
	}
}
