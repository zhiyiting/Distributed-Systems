import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

/**
 * Dispatcher to unmarshall the method invocation
 * 
 * @author zhiyiting
 *
 */
public class Dispatcher implements Runnable {
	private ServerSocket serverSocket;
	private boolean canRun;
	// service lookup table
	private Hashtable<String, MyRemote> servicetbl;

	/**
	 * Constructor to create a server socket
	 * 
	 * @param port
	 */
	public Dispatcher(int port) {
		canRun = true;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println("Fail to listen on port " + port);
		}
		servicetbl = new Hashtable<String, MyRemote>();
	}

	/**
	 * unmarshall method name and arguments from message, and start the method
	 * invocation
	 * @param invocation message
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
		MyRemote rm = servicetbl.get(serviceName);
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
	 * function to add remote object to the lookup table
	 * @param name
	 * @param remote object
	 */
	public void add(String name, MyRemote rm) {
		servicetbl.put(name, rm);
	}

	/**
	 * method to start the dispatcher as a thread
	 */
	public void run() {
		while (canRun) {
			try {
				Socket socket = serverSocket.accept();
				ObjectInputStream in = new ObjectInputStream(
						socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(
						socket.getOutputStream());
				// read the incoming message
				RMIMessage o = (RMIMessage) in.readObject();
				// invoke the message locally and get return value
				Object returnVal = dispatch(o);
				// compose the return message
				RMIMessage ret = new RMIMessage(returnVal);
				out.writeObject(ret);
				out.flush();
				out.close();
				in.close();
				socket.close();

			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Stop the thread
	 */
	public void stop() {
		canRun = false;
	}
}
