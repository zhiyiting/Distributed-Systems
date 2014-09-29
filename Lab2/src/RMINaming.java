import java.util.Hashtable;

public class RMINaming {
	
	private Hashtable<String, MyRemote> stubtbl;
	private String rHost;
	private int rPort;
	private String host;
	private int port;

	public RMINaming(String host, int port, String rHost, int rPort) {
		stubtbl = new Hashtable<String, MyRemote>();
		this.host = host;
		this.port = port;
		this.rHost = rHost;
		this.rPort = rPort;
	}
	
	public MyRemote lookup(String serviceName) {
		if (stubtbl.contains(serviceName)) {
			return stubtbl.get(serviceName);
		}
		RMIMessage msg = new RMIMessage("lookup", serviceName, rHost, rPort, host, port);
		RemoteObjectRef ror = (RemoteObjectRef) ((RMIMessage) CommModule.send(msg)).getContent();
		MyRemote stub = null;
		if (ror == null) {
			System.out.println("lookup fail");
		}
		else {
			stub = (MyRemote) ror.localise();
			stubtbl.put(serviceName, stub);
		}
		
		return stub;
	}

}
