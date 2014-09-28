import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class CommModule {

	
	public CommModule() {
		
	}
	
	public void send(RMIMessage msg) {
		try {
			Socket socket = new Socket(msg.getToHost(), msg.getToPort());
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(msg);
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
	
	public Object receive() {
		return null;
	}
}
