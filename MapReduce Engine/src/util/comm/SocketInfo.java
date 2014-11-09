package util.comm;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketInfo {
	public Socket socket;
	public ObjectOutputStream out;
	public ObjectInputStream in;

	public SocketInfo(Socket socket, ObjectOutputStream out,
			ObjectInputStream in) {
		this.socket = socket;
		this.out = out;
		this.in = in;
	}

}