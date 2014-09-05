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
	public void write(int b) throws IOException {
		RandomAccessFile out = new RandomAccessFile(filename, "rws");
		out.seek(position);
		out.write(b);
		out.close();
		position++;
	}

	@Override
	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0)
				|| ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		RandomAccessFile out = new RandomAccessFile(filename, "rws");
		out.seek(position);
		out.write(b, off, len);
		out.close();
		position += len;
	}

	@Override
	public String toString() {
		return this.filename;
	}
}
