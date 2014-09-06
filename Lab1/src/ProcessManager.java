import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class ProcessManager {
	private ArrayList<MigratableProcess> processes;

	public ProcessManager() {
		processes = new ArrayList<MigratableProcess>();
	}

	public void launch(String arg) {
		String[] cmds = arg.split(" ");
		String processName = cmds[0];
		Object[] args = null;
		System.arraycopy(cmds, 1, args, 0, cmds.length - 1);
		try {
			Class<?> myClass = Class.forName(processName);
			Constructor<?> myCtor;
			myCtor = myClass.getConstructor();
			MigratableProcess process = (MigratableProcess) myCtor
					.newInstance(args);
			Thread t = new Thread(process);
			processes.add(process);
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			processes.remove(process);

		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void remove() {

	}

	public void migrate() {

	}
}
