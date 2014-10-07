import java.io.Serializable;

/**
 * RMIMessage class that define message format
 * 
 * @author zhiyiting
 *
 */
public class RMIMessage implements Serializable {

	private static final long serialVersionUID = 4061427658346808609L;

	// message components
	private String serviceName;
	private Object method;
	private Object parameterTypes;
	private Object content;
	private String toHost;
	private int toPort;

	/**
	 * Constructor for one-sentence message
	 * @param method
	 * @param content
	 * @param toHost
	 * @param toPort
	 */
	public RMIMessage(Object method, Object content, String toHost, int toPort) {
		this.method = method;
		this.content = content;
		this.toHost = toHost;
		this.toPort = toPort;
	}

	/**
	 * Constructor for service invoke message
	 * @param serviceName
	 * @param method
	 * @param parameterTypes
	 * @param content
	 * @param toHost
	 * @param toPort
	 */
	public RMIMessage(String serviceName, String method, Object parameterTypes,
			Object content, String toHost, int toPort) {
		this.serviceName = serviceName;
		this.method = method;
		this.parameterTypes = parameterTypes;
		this.content = content;
		this.toHost = toHost;
		this.toPort = toPort;
	}

	/**
	 * Constructor for simple message
	 * @param content
	 */
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

	public String getToHost() {
		return toHost;
	}

	public int getToPort() {
		return toPort;
	}
}
