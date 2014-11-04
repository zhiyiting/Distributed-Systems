package util;

import java.net.Socket;

public class CoordDispatcher extends Dispatcher {

	public CoordDispatcher(Socket socket) {
		super(socket);
	}
	
	protected Object dispatch(Message m) {
		Object ret = null;
		String method = m.getContent();
		switch (method) {
		// from client
		case "start":
			break;
		case "list":
			break;
		// from slaves
		case "ping":
			break;
		case "done":
			WorkMessage msg = (WorkMessage)m;
			
			break;
		default:
			break;	
		}
		return ret;	
	}


}
