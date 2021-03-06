import java.io.DataInputStream;
import java.io.PrintStream;

public class FilterNumber implements MigratableProcess {

	private static final long serialVersionUID = -4055250229478589844L;
	private TransactionalFileInputStream inFile;
	private TransactionalFileOutputStream outFile;

	private volatile boolean suspending;

	public FilterNumber() {

	}

	public FilterNumber(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("usage: Capitalize <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}

		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOutputStream(args[1], false);
	}

	@Override
	public void run() {
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);
		try {
			while (!suspending) {
				char i = (char) in.read();
				if (i == -1) {
					break;
				}
				if (i >= '0' && i <= '9') {
					out.write(i);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (Exception e) {
			
		} 

		suspending = false;

	}

	@Override
	public void suspend() {
		suspending = true;
		while (suspending)
			;

	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Filter Number: ");
		sb.append(inFile.toString());
		sb.append(" ");
		sb.append(outFile.toString());
		return sb.toString();
	}
}
