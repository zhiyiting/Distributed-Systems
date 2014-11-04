package util;

import java.io.Serializable;

public class Message implements Serializable{

	private static final long serialVersionUID = 7264137218310503076L;
	private String content;
	private String toHost;
	private int toPort;
	
	public Message(String content, String toHost, int toPort) {
		this.content = content;
		this.toHost = toHost;
		this.toPort = toPort;
	}
	
	public Message(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return this.content;
	}
	
	public String getToHost() {
		return this.toHost;
	}
	
	public int getToPort() {
		return this.toPort;
	}

}
