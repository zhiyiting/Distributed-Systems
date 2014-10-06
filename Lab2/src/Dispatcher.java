import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Hashtable;


public class Dispatcher extends Listener {
	private Hashtable<String, MyRemote> servicetbl;	
	
	public Dispatcher(int port) {
		super(port);
		servicetbl = new Hashtable<String, MyRemote>();
	}
	
	private Object dispatch(RMIMessage m) {
		Object ret = null;
		if (m == null) {
			System.out.println("Message is empty");
			return ret;
		}
		String serviceName = m.getService();
		MyRemote rm = servicetbl.get(serviceName);

		String methodName = (String) m.getMethod();

		Object[] args = (Object[])m.getContent();
		
		Class<?>[] parameterTypes = (Class<?>[]) m.getParameterTypes();
		
		try {

			System.out.println("About to invoke...");
			Method method = rm.getClass().getMethod(methodName, parameterTypes);
			ret = method.invoke(rm, args);
		}catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
		
	}
	
	public void add(String name, MyRemote rm) {
		servicetbl.put(name, rm);
	}
	
	public void run() {
		while (canRun) {
			try {
				Socket socket = serverSocket.accept();
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				RMIMessage o = (RMIMessage) in.readObject();
				Object returnVal = dispatch(o);
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
}
