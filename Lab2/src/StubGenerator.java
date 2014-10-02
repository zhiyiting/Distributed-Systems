import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class StubGenerator implements InvocationHandler {

	private String serverHost;
	private int serverPort;

	public StubGenerator(String serverHost, int serverPort) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		System.out.println("invoke function " + method.getName() + " <- name");
		RMIMessage msg = new RMIMessage(method.getName(), args, serverHost,
				serverPort);
		RMIMessage ret = (RMIMessage) CommModule.send(msg);
		return ret.getContent();
	}

}
