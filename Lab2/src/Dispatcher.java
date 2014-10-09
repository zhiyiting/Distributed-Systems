import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * Dispatcher class instantiates threads for dispatching functions
 * 
 * @author zhiyiting
 *
 */
public class Dispatcher implements Runnable {

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private ServerListener serverListener;
	private boolean canRun;

	/**
	 * Constructor that keeps track of input/output stream 
	 * @param socket
	 * @param server listener
	 */
	public Dispatcher(Socket socket, ServerListener sl) {
		try {
			this.in = new ObjectInputStream(socket.getInputStream());
			this.out = new ObjectOutputStream(socket.getOutputStream());
			this.serverListener = sl;
		} catch (IOException e) {
			System.out.println("Dispatcher Worker: fail to create socket");

		}
		canRun = true;
	}

	/**
	 * unmarshall method name and arguments from message, and start the method
	 * invocation
	 * 
	 * @param invocation
	 *            message
	 * @return return value
	 */
	private Object dispatch(RMIMessage m) {
		Object ret = null;
		if (m == null) {
			System.out.println("Message is empty");
			return ret;
		}
		// get service name from message and get corresponding remote object
		String serviceName = m.getService();
		MyRemote rm = serverListener.servicetbl.get(serviceName);
		// get method name from message
		String methodName = (String) m.getMethod();
		// get arguments from message
		Object[] args = (Object[]) m.getContent();
		// get argument type from message
		Class<?>[] parameterTypes = (Class<?>[]) m.getParameterTypes();

		try {
			Method method = rm.getClass().getMethod(methodName, parameterTypes);
			ret = method.invoke(rm, args);
		} catch (NoSuchMethodException e) {
			System.out.println("No Such Method Exception");
			e.printStackTrace();
		} catch (SecurityException e) {
			System.out.println("Security Exception");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("Illegal Acess Exception");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println("Illegal Argument Exception");
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			System.out.println("Invocation Target Exception");
			e.printStackTrace();
		}
		return ret;

	}

	/**
	 * Start a thread and reuse current input/output stream
	 */
	@Override
	public void run() {
		while (canRun) {
			try {
				// read the incoming message
				RMIMessage o = (RMIMessage) in.readObject();
				// invoke the message locally and get return value
				Object returnVal = dispatch(o);
				// compose the return message
				RMIMessage ret = new RMIMessage(returnVal);
				out.writeObject(ret);
				out.flush();
			} catch (IOException e) {
				break;
			} catch (ClassNotFoundException e) {
				System.out.println("RegistryWorker: Class Not Found Exception"
						+ e.getMessage());

			}
		}
	}
	
	public void stop() {
		canRun = false;
	}

}
