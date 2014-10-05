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
	
	private void dispatch(RMIMessage m) {
		if (m == null) {
			System.out.println("Message is empty");
			return;
		}
		String serviceName = m.getService();
		MyRemote rm = servicetbl.get(serviceName);
		System.out.println("Get obj done...");

		String methodName = (String) m.getMethod();
		System.out.println("Get method  done...");

		Object[] args = (Object[])m.getContent();
		System.out.println("Get args  done...");
		
		Class<?>[] parameterTypes = (Class<?>[]) m.getParameterTypes();
		
		try {

			System.out.println("About to invoke...");
			Method method = rm.getClass().getMethod(methodName, parameterTypes);
			Object ret = method.invoke(rm, args);
			System.out.println(ret);
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
		
	}
	
	public void add(String name, MyRemote rm) {
		servicetbl.put(name, rm);
	}
	
	public void run() {
		while (canRun) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println("Got message!");
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				RMIMessage o = (RMIMessage) in.readObject();
				dispatch(o);
				
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
