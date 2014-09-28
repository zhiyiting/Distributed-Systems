import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

	public RMIMessage read() {
		try {
			FileInputStream fileIn = new FileInputStream("communication/1.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			RMIMessage message = (RMIMessage) in.readObject();
			in.close();
			fileIn.close();
			return message;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

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
