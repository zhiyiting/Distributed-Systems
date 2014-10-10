
public class TestImpl implements Test {
	public String speak() {
		return "I speak without argument!";
	}
	
	public String speak(String s) {
		return "I'll repeat what you said   " + s;
	}
}
