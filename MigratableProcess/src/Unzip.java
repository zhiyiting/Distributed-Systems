import java.io.File;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

public class Unzip implements MigratableProcess{

	private static final long serialVersionUID = 5715096136819221283L;
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream  outFile;
	private String outDirectory;

	private volatile boolean suspending;

	public Unzip (String args[]) throws Exception {
		if (args.length != 2) {
			System.out.println("usage: Unzip <inputFile> <outDirectory>");
			throw new Exception("Invalid Arguments");
		}
		inFile = new TransactionalFileInputStream(args[0]);
		outDirectory = args[1];
	}

	public void run() {
		ZipInputStream in = new ZipInputStream(inFile);
		ZipEntry entry;

		try {
			while (!suspending) {
			    while ((entry = in.getNextEntry()) != null) {
	                String fileName = entry.getName();
					File newFile = new File(outDirectory + File.separator + fileName);
	                new File(newFile.getParent()).mkdirs();
	                int size;
	                byte[] buffer = new byte[2048];
	                while ((size = in.read(buffer, 0, buffer.length)) != -1) {
	                	if (!newFile.exists()) {
	                		newFile.createNewFile();
	                	}
	                    outFile = new TransactionalFileOutputStream(newFile.getName(), false);                	
	                    outFile.write(buffer, 0, size);
	                }
			    }
			    in.closeEntry();
			    in.close();
			    System.out.println("Done");
			    break;
			}
		} catch (Error e) {
			System.out.println ("Unzip: Error: " + e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		suspending = false;
	}

	public void suspend() {
		suspending = true;
		while (suspending)
			;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Unzip: ");
		sb.append(inFile.toString());
		sb.append(" ");
		sb.append(outDirectory);
		return sb.toString();
	}
}