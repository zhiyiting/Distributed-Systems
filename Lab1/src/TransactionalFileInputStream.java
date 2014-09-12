import java.io.IOException;
import java.io.RandomAccessFile;

public class TransactionalFileInputStream extends java.io.InputStream implements
		java.io.Serializable {

	private static final long serialVersionUID = 5662563434806545535L;
    
	private String filename;
	private long position;

	public TransactionalFileInputStream(String arg) {
		this.filename = arg;
		this.position = 0;
	}

	@Override
	public int read() throws IOException {
		RandomAccessFile f = new RandomAccessFile(filename, "r");
		f.seek(position);
		int byteRead = f.read();
		f.close();
		position += 1;
		return byteRead;
	}

	@Override
	public String toString() {
		return this.filename;
	}

}
