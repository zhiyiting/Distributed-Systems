import java.util.Hashtable;

/**
 * RMINaming class to provide remote naming functionalities
 * 
 * @author zhiyiting
 *
 */
public class RMINaming {

	// hashtable to store remote objects by service name
	private Hashtable<String, MyRemote> stubtbl;
	// registry server IP
	private String rHost;
	// registry server port
	private int rPort;

	/**
	 * Constructor to get server information to connect to
	 * 
	 * @param registry IP
	 * @param registry port
	 * 
	 */
	public RMINaming(String rHost, int rPort) {
		stubtbl = new Hashtable<String, MyRemote>();
		this.rHost = rHost;
		this.rPort = rPort;
	}

	/**
	 * Look up if the registry contains such service
	 * and instantiate a local stub to handle the function call
	 * @param serviceName
	 * @return local stub
	 */
	public MyRemote lookup(String serviceName) {
		// if the stub exists locally, return the local stub
		if (stubtbl.containsKey(serviceName)) {
			return stubtbl.get(serviceName);
		}
		MyRemote stub = null;
		// send a lookup message to the registry server
		RMIMessage msg = new RMIMessage("lookup", serviceName, rHost, rPort);
		RMIMessage ret = (RMIMessage) CommModule.sendStatic(msg);

		if (ret != null) {
			// get the ror back from registry server
			RemoteObjectRef ror = (RemoteObjectRef) ret.getContent();
			if (ror == null) {
				System.out.println("lookup fail");
			} else {
				// instantiate a local stub
				stub = (MyRemote) ror.localise();
				stubtbl.put(serviceName, stub);
			}
		}
		return stub;
	}

}
