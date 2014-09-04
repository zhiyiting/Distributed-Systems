import java.io.FileInputStream;
import java.io.IOException;

public class TransactionalFileInputStream extends java.io.InputStream implements
		java.io.Serializable {

	private static final long serialVersionUID = 5662563434806545535L;
    private static final int MAX_SKIP_BUFFER_SIZE = 2048;
    
	private String filename;
	private long position;

	public TransactionalFileInputStream(String arg) {
		this.filename = arg;
		this.position = 0;
	}

	@Override
	public int read() throws IOException {
		FileInputStream in = new FileInputStream(filename);
		in.skip(position);
		int val = in.read();
		in.close();
		if (val != -1) {
			position++;
		}
		return val;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);	
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
		FileInputStream in = new FileInputStream(filename);
		in.skip(position);
		int val = in.read(b, off, len);
		in.close();
		if (val != -1) {
			position += val;
		}
		return val;
	}
	
	@Override
	public long skip(long n) throws IOException {
		long remaining = n;
		int nr;
		
		if (n <= 0) {
			return 0;
		}
		
		FileInputStream in = new FileInputStream(filename);
		int size = (int)Math.min(MAX_SKIP_BUFFER_SIZE, remaining);
		byte[] skipBuffer = new byte[size];
		while (remaining > 0) {
			nr = in.read(skipBuffer, 0, (int)Math.min(size, remaining));
			if (nr < 0) {
				break;
			}
			remaining -= nr;
		}
		in.close();
		position += n - remaining;
		return n - remaining;
	}

	@Override
	public String toString() {
		return this.filename;
	}

}
