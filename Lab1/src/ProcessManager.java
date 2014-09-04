import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class ProcessManager {
	private ArrayList<MigratableProcess> processes = new ArrayList<MigratableProcess>();
	
	public ProcessManager() {
		
	}
	
	public void launch(String processName) {
		Class<?> myClass = Class.forName(processName);
		//Constructor<?> myCtor = myClass.getConstructor();
	}
	
	public void remove() {
		
	}
	
	public void migrate() {
		
	}
}
