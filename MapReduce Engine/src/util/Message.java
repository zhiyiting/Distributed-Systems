package util;

import java.io.Serializable;

public class Message implements Serializable{

	private static final long serialVersionUID = 7264137218310503076L;
	private String method;
	private String content;
	
	public Message(String method, String content) {
		this.method = method;
	}
	
	public Message(Object content) {
		this.content = (String) content;
	}
	
	public String getMethod() {
		return this.method;
	}
	
	public String getContent() {
		return this.content;
	}

}
