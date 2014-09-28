import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;


public class Message implements Serializable {
	private String host;
	private int port;
	private String action;
	

	public Message(String host, int port, String action) {
		this.host = host;
		this.port = port;
		this.action = action;
	}
	
	public void send() {
		try {
			Socket socket = new Socket(host, port);
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(this);
			in.readObject();
			in.close();
			out.close();
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
