import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * StubGenerator class to handle method invocation
 * 
 * @author zhiyiting
 *
 */
public class StubGenerator implements InvocationHandler {

	private String serverHost;
	private int serverPort;
	private String serviceName;
	private CommModule commModule;

	/**
	 * Constructor to create a stub
	 * 
	 * @param server host
	 * @param server port
	 * @param service name
	 * 
	 */
	public StubGenerator(String serverHost, int serverPort, String sn,
			CommModule commModule) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.serviceName = sn;
		this.commModule = commModule;
	}

	/**
	 * Function to invoke method
	 * @throws IOException 
	 * @throws RemoteException 
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws RemoteException {
		try {
			Class<?>[] parameterTypes = method.getParameterTypes();
			// compose a message to invoke method from remote object
			RMIMessage msg = new RMIMessage(serviceName, method.getName(),
					parameterTypes, args, serverHost, serverPort);
			// send it over socket and get return value
			RMIMessage ret = (RMIMessage) commModule.send(msg);
			return ret.getContent();
		} catch (RemoteException e) {
			throw new RemoteException("Can't invoke method");
		}
	}
}