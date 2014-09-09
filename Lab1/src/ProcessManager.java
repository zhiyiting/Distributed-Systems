import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

public class ProcessManager {
	private Boolean isMaster;
	private String masterAddr;
	private int port;

	private List<String> slaveList;
	private List<Integer> slaveProcessCount;

	private List<Integer> activeProcess;
	private List<Integer> suspendedProcess;
	private List<Integer> finishedProcess;

	private Hashtable<Integer, MigratableProcess> processList;
	private Stack<Integer> idPool;
	private int processCount;

	public ProcessManager(String addr, int port) {
		processCount = 0;

		idPool = new Stack<Integer>();
		slaveList = new ArrayList<String>();
		slaveProcessCount = new ArrayList<Integer>();
		activeProcess = new ArrayList<Integer>();
		suspendedProcess = new ArrayList<Integer>();
		finishedProcess = new ArrayList<Integer>();

		// <processID>, <process>
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
				System.out.print(">> ");
				String in = br.readLine();
				String[] args = in.split(" ");
				if (args.length <= 0) {
					continue;
				}
				switch (args[0]) {
				case "jobs":
					printJob();
					break;
				case "launch":
					launch(args);
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
							+ "Print process: jobs \n" + "Exit: exit/quit \n");
					break;
				}

			} catch (IOException e) {
				System.out.println("IOException");
				System.exit(-1);
			}

		}
	}

	private void printJob() {
		System.out.println("Active Process:");
		if (activeProcess.size() == 0) {
			System.out.println("empty");
		}
		for (int i : activeProcess) {
			System.out.println(processList.get(i).toString());
		}
		System.out.println("Suspended Process:");
		if (suspendedProcess.size() == 0) {
			System.out.println("empty");
		}
		for (int i : suspendedProcess) {
			System.out.println(processList.get(i).toString());
		}
		System.out.println("Finished Process:");
		if (finishedProcess.size() == 0) {
			System.out.println("empty");
		}
		for (int i : finishedProcess) {
			System.out.println(processList.get(i).toString());
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

	public void launch(String[] args) {
		Object[] cmds = null;
		// launch -n <node> <name> <args>
		if (args[1].equals("-n") && args.length == 5) {
			System.arraycopy(args, 4, cmds, 0, args.length - 4);
			startProcess(args[3], cmds, args[2]);
		}
		// launch <name> <args>
		else if (args.length == 3) {
			System.arraycopy(args, 2, cmds, 0, args.length - 2);
			startProcess(args[1], cmds, null);
		} else {
			System.out.println("Usage:\n"
					+ "launch -n <node> <process name> <args>\n"
					+ "launch <process name> <args>");
		}

	}

	public void startProcess(String processName, Object[] args, String addr) {
		MigratableProcess process;
		// Instantiate a process object
		try {
			Class<?> c = Class.forName(processName);
			Constructor<?> myCtor = c.getConstructor();
			process = (MigratableProcess) myCtor.newInstance(args);
			int processID = addProcess(process);
			if (processID <= 0) {
				System.out.println("Invalid process id!");
				continue;
			}
		} catch (ClassNotFoundException e1) {
			System.out.println("Class Not Found Exception!");
			continue;
		} catch (NoSuchMethodException e1) {
			System.out.println("No Such Method Exception!");
			continue;
		} catch (SecurityException e1) {
			System.out.println("Security Exception!");
			continue;
		} catch (InstantiationException e1) {
			System.out.println("Instantiation Exception!");
			continue;
		} catch (IllegalAccessException e1) {
			System.out.println("Illegal Access Exception!");
			continue;
		} catch (IllegalArgumentException e1) {
			System.out.println("Illegal Argument Exception!");
			continue;
		} catch (InvocationTargetException e1) {
			System.out.println("Invocation Target Exception!");
			continue;
		}
		String slave = pickSlave(addr);
		String[] slaveAddr = slave.split(":");
		// send it to slave
		try {
			Socket socket = new Socket(slaveAddr[0],
					Integer.parseInt(slaveAddr[1]));
			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());
			out.writeObject(obj);
			out.flush();
			in.readObject();
			in.close();
			out.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String pickSlave(String addr) {
		if (slaveList.size() <= 0) {
			System.out.println("No connected slaves!");
			System.exit(-1);
		}
		if (addr != null) {
			if (addr.charAt(0) >= '0' && addr.charAt(0) <= '9') {
				return slaveList.get(Integer.parseInt(addr));
			}
			return addr;
		}
		int minCount = slaveProcessCount.get(0);
		int index = 0;
		for (int i = 1; i < slaveProcessCount.size(); i++) {
			int cur = slaveProcessCount.get(i);
			if (minCount > cur) {
				minCount = cur;
				index = i;
			}
		}
		return slaveList.get(index);
	}

	private int addProcess(MigratableProcess p) {
		int id = getProcessID();
		processList.put(id, p);
		return id;
	}

	private int getProcessID() {
		if (idPool.isEmpty()) {
			processCount++;
			return processCount;
		}
		return idPool.pop();
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
