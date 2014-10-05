import java.io.Serializable;

public class RMIMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4061427658346808609L;

	private String serviceName;
	private Object method;
	private Object parameterTypes;
	private Object content;
	private String fromHost;
	private int fromPort;
	private String toHost;
	private int toPort;

	public RMIMessage(Object method, Object content, String toHost, int toPort) {
		this.method = method;
		this.content = content;
		this.toHost = toHost;
		this.toPort = toPort;
	}
	
	public RMIMessage(String serviceName, String method, Object parameterTypes, Object content, String toHost, int toPort) {
		this.serviceName = serviceName;
		this.method = method;
		this.parameterTypes = parameterTypes;
		this.content = content;
		this.toHost = toHost;
		this.toPort = toPort;
	}

	public RMIMessage(Object content) {
		this.content = content;
	}
	
	public String getService() {
		return serviceName;
	}

	public Object getMethod() {
		return method;
	}
	
	public Object getParameterTypes() {
		return parameterTypes;
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
