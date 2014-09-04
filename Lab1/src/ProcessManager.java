import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class ProcessManager {
	private ArrayList<MigratableProcess> processes = new ArrayList<MigratableProcess>();
	
	public ProcessManager() {
		
	}
	
	public void launch(String processName) {
		try {
			Class<?> myClass = Class.forName(processName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Constructor<?> myCtor = myClass.getConstructor();
	}
	
	public void remove() {
		
	}
	
	public void migrate() {
		
	}
}
