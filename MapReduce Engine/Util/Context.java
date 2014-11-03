import java.util.LinkedList;

public class Context {
	
	private LinkedList<String[]> buffer;
	
	public Context() {
		buffer = new LinkedList<String[]>();
	}
	
	public void write(String key, String val) {
		String [] record = new String[2];
		record[0] = key;
		record[1] = val;
		buffer.add(record);
	}
}
