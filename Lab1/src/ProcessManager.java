import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ProcessManager {
	private Boolean isMaster;
	private String masterAddr;
	private int port;
	
	private List<String> slaveList;
	
	private List<Integer> activeProcess; 
	private List<Integer> suspendedProcess;
	private List<Integer> finishedProcess;
	
	private Hashtable<Integer, MigratableProcess> processList;

	public ProcessManager(String addr, int port) {
		slaveList = new ArrayList<String>();
		
		activeProcess = new ArrayList<Integer>();
		suspendedProcess = new ArrayList<Integer>();
		finishedProcess = new ArrayList<Integer>();
		
		processList = new Hashtable<Integer, MigratableProcess>();
		
		// Start it as a master process manager
		if (addr == null) {
			this.isMaster = true;
			startMaster();
		}
		// Start it as a slave process manager
		else {
			this.isMaster = false;
			this.masterAddr = addr;
			this.port = port;
			startSlave();
		}
	}

	private void startMaster() {
		// Open a port to listen to slave connections
		Server server = new Server(port, this);
		server.start();
		System.out.println("Start Process Manager (Master) : port " + port);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				String in = br.readLine();
				String[] args = in.split(" ");
				if (args.length <= 0) {
					continue;
				}
				switch (args[0]) {
				case "jobs":
					break;
				case "launch":
					break;
				case "migrate":
					break;
				case "suspend":
					break;
				case "exit":
				case "quit":
					System.out.println("Master Process Manager terminated...");
					System.exit(0);
					break;
				default:
					System.out.println("Usage: \n"
							+ "Launch process: launch -n\n"
							+ "Suspend process: suspend \n"
							+ "Migrate process: migrate \n"
							+ "Print active process: \n"
							+ "Print finished process: \n"
							+ "Print suspended process: \n"
							+ "Exit: exit/quit \n");
					break;
				}

			} catch (IOException e) {
				System.out.println("IOException");
				System.exit(-1);
			}

		}
	}

	private void startSlave() {
		// Connect it to the server
		try {
			Socket socket = new Socket(masterAddr, port);
			System.out.println("Start Process Manager (Slave). Master at "
					+ masterAddr + ":" + port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void launch(String arg) {
		String[] cmds = arg.split(" ");
		Object[] args = null;
		String processName;
		if (cmds[0] == "-n") {
			// InetAddress addr = cmds[1];
			processName = cmds[2];
			System.arraycopy(cmds, 3, args, 0, cmds.length - 3);
		} else {
			processName = cmds[0];
			System.arraycopy(cmds, 1, args, 0, cmds.length - 1);
		}
		try {
			Class<?> myClass = Class.forName(processName);
			Constructor<?> myCtor;
			myCtor = myClass.getConstructor();
			MigratableProcess process = (MigratableProcess) myCtor
					.newInstance(args);

			Thread t = new Thread(process);

		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		}
	}

	public void remove() {

	}

	public void migrate() {

	}

	public static void main(String[] args) {
		if (args.length == 2 && args[0].equals("-p")) {
			ProcessManager pm = new ProcessManager(null,
					Integer.parseInt(args[1]));
		} else if (args.length == 4 && args[0].equals("-m")
				&& args[2].equals("-p")) {
			ProcessManager pm = new ProcessManager(args[1],
					Integer.parseInt(args[3]));
		} else {
			printUsage();
		}

	}

	private static void printUsage() {
		System.out
				.println("Usage: \n"
						+ "Start master Process Manager: -p <port number> \n"
						+ "Start worker(slave) Process Manager: -m <master ip> -p <master port> \n"
						+ "Help: -h \n");
		System.exit(-1);
	}
}
