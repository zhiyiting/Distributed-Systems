import java.io.Serializable;

public class RMIMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4061427658346808609L;

	private String method;
	private Object content;
	private String fromHost;
	private int fromPort;
	private String toHost;
	private int toPort;

	public RMIMessage(String method, Object content, String toHost, int toPort,
			String fromHost, int fromPort) {
		this.method = method;
		this.content = content;
		this.toHost = toHost;
		this.toPort = toPort;
		this.fromHost = fromHost;
		this.fromPort = fromPort;
	}
	
	public RMIMessage(Object content) {
		this.content = content;
	}

	public String getMethod() {
		return method;
	}

	public Object getContent() {
		return content;
	}

	public String getFromHost() {
		return fromHost;
	}

	public int getFromPort() {
		return fromPort;
	}

	public String getToHost() {
		return toHost;
	}

	public int getToPort() {
		return toPort;
	}
}
