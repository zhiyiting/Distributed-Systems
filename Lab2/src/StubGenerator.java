import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class StubGenerator implements InvocationHandler {

	private String serverHost;
	private int serverPort;
	private String serviceName;

	public StubGenerator(String serverHost, int serverPort, String sn) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.serviceName = sn;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		System.out.println("invoke function ");
		Class<?>[] parameterTypes = method.getParameterTypes();
		RMIMessage msg = new RMIMessage(serviceName, method.getName(),
				parameterTypes, args, serverHost, serverPort);
		RMIMessage ret = (RMIMessage) CommModule.send(msg);
		return ret.getContent();
	}

}
