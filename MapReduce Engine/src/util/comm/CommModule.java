package util.comm;

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

	/**
	 * function to send and receive message
	 * 
	 * @param message
	 * @return return message
	 * @throws RemoteException 
	 */
	public Message send(Message msg) throws RemoteException {
		try {
			ObjectOutputStream out;
			ObjectInputStream in;
			if (socketCache.contains(msg.getToHost(), msg.getToPort())) {
				SocketInfo socketObj = socketCache.get(msg.getToHost(),
						msg.getToPort());
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
			Message o = (Message)in.readObject();
			if(o == null) {
				throw new RemoteException("can't connect to server");
			}
			return o;
		} catch (UnknownHostException e) {
			throw new RemoteException("Unknown Host");
		} catch (IOException e) {
			throw new RemoteException("IO Exception");
		} catch (ClassNotFoundException e) {
			System.out.println("Class Not Found Exception: " + e.getMessage());
		}
		return null;
	}
	
	public static void send(Message m, String toHost, int toPort) {
		try {
			Socket socket = new Socket(toHost, toPort);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			out.writeObject(m);
			out.flush();
			in.readObject();
			socket.close();
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
	}
}