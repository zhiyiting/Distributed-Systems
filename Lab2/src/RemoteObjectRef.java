import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Remote Object Reference class that store the information to find the object
 * 
 * @author zhiyiting
 *
 */
public class RemoteObjectRef implements Serializable {

	private static final long serialVersionUID = 1631581795692753053L;
	private String host;
	private int port;
	private String serviceName;
	private String remoteInterfaceName;

	/**
	 * Constructor to make a remote object reference
	 * @param ip
	 * @param port
	 * @param service name
	 * @param remote interface name
	 */
	public RemoteObjectRef(String ip, int port, String key, String riname) {
		this.host = ip;
		this.port = port;
		this.serviceName = key;
		this.remoteInterfaceName = riname;
	}

	/**
	 * Stub creator that makes a local stub to deal with method invocation
	 * @return proxy stub
	 */
	public Object localise(CommModule commModule) {

	// use an invocation handler to invoke method
		InvocationHandler handler = new StubGenerator(this.host, this.port,
				this.serviceName, commModule);
		try {
			Class<?> c = Class.forName(remoteInterfaceName);
			// create a proxy for communication to server side
			Object proxy = Proxy.newProxyInstance(c.getClassLoader(),
					new Class[] { c }, handler);
			return proxy;
		} catch (ClassNotFoundException e) {
			System.out.println("Class Not Found Exception");
			e.printStackTrace();
		}
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