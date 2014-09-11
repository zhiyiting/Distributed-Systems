public class SlaveWorker {
	public String ip;
	public int port;
	
	public SlaveWorker(String addr) {
		String [] a = addr.split(":");
		ip = a[0];
		port = Integer.parseInt(a[1]);
	}

}
