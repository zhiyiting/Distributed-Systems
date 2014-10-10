
public class HeartBeat implements Runnable {

	private CommModule comm;
	private String host;
	private int port;
	
	public HeartBeat(CommModule comm, String host, int port) {
		this.comm = comm;
		this.host = host;
		this.port = port;
	}
	
	@Override
	public void run() {
		while (true) {
			RMIMessage pingMessage = new RMIMessage("ping", "hi", host, port);
			try {
				comm.send(pingMessage);
			} catch (RemoteException e) {
				break;
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				break;
			}
		}
	}

}
