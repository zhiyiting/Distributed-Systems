package util.comm;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * SocketInfo class to store the information about socket
 * 
 * @author zhiyiting
 *
 */
public class SocketInfo {
	public Socket socket;
	public ObjectOutputStream out;
	public ObjectInputStream in;

	/**
	 * Set the information about the socket
	 * 
	 * @param socket
	 * @param out
	 * @param in
	 */
	public SocketInfo(Socket socket, ObjectOutputStream out,
			ObjectInputStream in) {
		this.socket = socket;
		this.out = out;
		this.in = in;
	}
}