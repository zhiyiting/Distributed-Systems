import java.util.HashMap;
import java.util.Iterator;

public class Test {
	public static void main(String[] args) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("test1", "test1");
		map.put("test", "test123");
		map.put("test", "test123");
		map.put("test2", "test456");
		

		for (Iterator<HashMap.Entry<String, String>> it = map.entrySet()
				.iterator(); it.hasNext();) {
			HashMap.Entry<String, String> entry = it.next();
			if (entry.getKey().equals("test")) {
				it.remove();
			}
			else {
				map.put(entry.getKey(), entry.getValue() + "bbb");
			}
		}
		
		for (HashMap.Entry<String, String> en: map.entrySet()) {
			System.out.println(en.getValue());
		}
	}
}