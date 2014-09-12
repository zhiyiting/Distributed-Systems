public class Heartbeat extends Thread {
	ProcessManager mgr;

	public Heartbeat(ProcessManager m) {
		this.mgr = m;
	}

	public void run() {
		while (true) {
			try {

				mgr.pingSlave();
				Thread.sleep(5000);
			} catch (Exception e) {
				continue;
			}
		}

	}
}
