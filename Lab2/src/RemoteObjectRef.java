import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class RemoteObjectRef implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1631581795692753053L;
	private String host;
	private int port;
	private String serviceName;
	private String remoteInterfaceName;

	public RemoteObjectRef(String ip, int port, String key, String riname) {
		this.host = ip;
		this.port = port;
		this.serviceName = key;
		this.remoteInterfaceName = riname;
	}

	// this method is important, since it is a stub creator.
	//
	public Object localise() {
		/*
		MyRemote stub = null;
		String stubName = remoteInterfaceName + "_stub";
		Class<?> c = null;
		try {
			c = Class.forName(stubName);
		} catch (ClassNotFoundException e) {
			System.out.println("Cannot find stub class");
			// Invocation handler
			
		}*/
		System.out.println("Start to localise");
		InvocationHandler handler = new StubGenerator(this.host, this.port, this.serviceName);
		System.out.println("Handler created");

		try {
			Class<?> c = Class.forName(remoteInterfaceName);
			Object proxy = Proxy.newProxyInstance(c.getClassLoader(), new Class[] {c}, handler);
			System.out.println("Proxy created");
			return proxy;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		try {
			//stub = (MyRemote) c.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

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

	public String getServiceName() {
		return serviceName;
	}

	public String getInterfaceName() {
		return remoteInterfaceName;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
}