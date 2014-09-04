import java.io.IOException;

public class TransactionalFileOutputStream extends java.io.OutputStream
		implements java.io.Serializable {

	private static final long serialVersionUID = -2759612490300531241L;
	private String filename;

	public TransactionalFileOutputStream(String arg, boolean val) {
		this.filename = arg;
	}

	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return this.filename;
	}
}
