import java.util.ArrayList;
import java.util.List;


public class SlaveWorker {
	public String ip;
	public int port;
	private List<Integer> activeProcessList;
	private List<Integer> suspendedProcessList;
	
	public SlaveWorker(String addr) {
		ip = addr.split(":")[0];
		port = Integer.parseInt(addr.split(":")[1]);
		activeProcessList = new ArrayList<Integer>();
		suspendedProcessList = new ArrayList<Integer>();
	}
	
	public int addProcess(int i) {
		if (activeProcessList.contains(i))  {
			return -1;
		}
		try {
			if (suspendedProcessList.contains(i)) {
				suspendedProcessList.remove(i);
			}
			activeProcessList.add(i);
			return 1;
		}
		catch (Exception e) {
			return -1;
		}
	}
	
	public int suspendProcess(int i) {
		if (!activeProcessList.contains(i)) {
			return -1;
		}
		if (suspendedProcessList.contains(i)) {
			return -2;
		}
		try {
			activeProcessList.remove(i);
			suspendedProcessList.add(i);
			return 1;
		}
		catch (Exception e) {
			return -3;
		}
	}
	
	public int removeProcess(int i) {
		try {
			if (activeProcessList.contains(i)) {
				activeProcessList.remove(i);
			}
			else if (suspendedProcessList.contains(i)) {
				suspendedProcessList.remove(i);
			}
			return 1;
		}
		catch (Exception e) {
			return -1;
		}
		
	}
	
	public int getLoad() {
		return activeProcessList.size();
	}
	
	public List<Integer> getActiveJob() {
		return activeProcessList;
	}
	
	public List<Integer> getSuspendedJob() {
		return suspendedProcessList;
	}

}
