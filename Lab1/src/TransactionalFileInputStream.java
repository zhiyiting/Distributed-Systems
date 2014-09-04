import java.io.IOException;

public class TransactionalFileInputStream extends java.io.InputStream implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5662563434806545535L;
	private String filename;
	private long location;
	
	public TransactionalFileInputStream(String arg) {
		this.filename = arg;
	}

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String toString() {
		return this.filename;
	}

}
