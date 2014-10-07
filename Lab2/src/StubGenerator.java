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

	/**
	 * Constructor to create a stub
	 * @param server host
	 * @param server port
	 * @param service name
	 */
	public StubGenerator(String serverHost, int serverPort, String sn) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.serviceName = sn;
	}

	/**
	 * Function to invoke method
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		System.out.println("invoke function ");
		Class<?>[] parameterTypes = method.getParameterTypes();
		// compose a message to invoke method from remote object
		RMIMessage msg = new RMIMessage(serviceName, method.getName(),
				parameterTypes, args, serverHost, serverPort);
		RMIMessage ret = (RMIMessage) CommModule.sendStatic(msg);
		return ret.getContent();
	}

}
