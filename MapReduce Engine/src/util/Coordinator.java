package util;

import java.io.IOException;
import java.net.Socket;

public class Coordinator extends Listener {

	public Coordinator(int port) {
		super(port);
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				Thread t = new Thread(new CoordDispatcher(socket));
				t.setDaemon(true);
				t.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
