import java.io.RandomAccessFile;
import java.io.IOException;

public class TransactionalFileOutputStream extends java.io.OutputStream
		implements java.io.Serializable {

	private static final long serialVersionUID = -2759612490300531241L;
	private String filename;
	private long position;

	public TransactionalFileOutputStream(String arg, boolean val) {
		this.filename = arg;
		this.position = 0;
	}

	@Override
	public void write(int arg) throws IOException {
		RandomAccessFile f = new RandomAccessFile(filename, "rw");
		f.seek(position);
		f.write(arg);
		f.close();
		position += 1;
	}

	@Override
	public String toString() {
		return this.filename;
	}
}
