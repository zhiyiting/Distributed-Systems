import java.util.Hashtable;

public class RMINaming {

	private Hashtable<String, MyRemote> stubtbl;
	private String rHost;
	private int rPort;
	
	public RMINaming(String rHost, int rPort) {
		stubtbl = new Hashtable<String, MyRemote>();
		this.rHost = rHost;
		this.rPort = rPort;
	}

	public MyRemote lookup(String serviceName) {
		if (stubtbl.containsKey(serviceName)) {
			return stubtbl.get(serviceName);
		}
		MyRemote stub = null;
		RMIMessage msg = new RMIMessage("lookup", serviceName, rHost, rPort);
		RMIMessage ret = (RMIMessage) CommModule.send(msg);

		if (ret != null) {
			RemoteObjectRef ror = (RemoteObjectRef) ret.getContent();
			if (ror == null) {
				System.out.println("lookup fail");
			} else {
				stub = (MyRemote) ror.localise();
				stubtbl.put(serviceName, stub);
				System.out.println("stub created");
			}
		}
		return stub;
	}

}
