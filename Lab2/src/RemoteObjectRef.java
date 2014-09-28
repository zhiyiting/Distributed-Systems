import java.io.Serializable;

public class RemoteObjectRef implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1631581795692753053L;
	String host;
	int port;
	int key;
	String remoteInterfaceName;
	String serviceName;

	public RemoteObjectRef(String ip, int port, int obj_key, String riname, String sname) {
		this.host = ip;
		this.port = port;
		this.key = obj_key;
		this.remoteInterfaceName = riname;
		this.serviceName = sname;
	}

	// this method is important, since it is a stub creator.
	//
	Object localise() {
		// Implement this as you like: essentially you should
		// create a new stub object and returns it.
		// Assume the stub class has the name e.g.
		//
		// Remote_Interface_Name + "_stub".
		//
		// Then you can create a new stub as follows:
		//
		// Class c = Class.forName(Remote_Interface_Name + "_stub");
		// Object o = c.newinstance()
		//
		// For this to work, your stub should have a constructor without
		// arguments.
		// You know what it does when it is called: it gives communication
		// module
		// all what it got (use CM's static methods), including its method name,
		// arguments etc., in a marshalled form, and CM (yourRMI) sends it out
		// to
		// another place.
		// Here let it return null.
		return null;
	}
	
	public int getKey() {
		return key;
	}
	
	public String getInterfaceName() {
		return remoteInterfaceName;
	}
	
	public String getServiceName() {
		return serviceName;
	}
}