import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Dispatcher extends Listener {
	
	public Dispatcher(int port) {
		super(port);
	}
	
	private void dispatch(Object o) {
		
	}
	
	public void run() {
		while (canRun) {
			try {
				Socket socket = serverSocket.accept();
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				Object o = in.readObject();
				if (o != null) {
					dispatch(o);
				}
				
			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
