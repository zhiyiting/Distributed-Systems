import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

//http://compression.ca/act/act-files.html

public class ProcessManager {
	public Boolean isMaster;
	public String masterAddr;
	public int port;

	private List<SlaveWorker> slaveList;
	private ConcurrentHashMap<Integer, Thread> slaveThread;

	private List<Integer> activeProcess;
	private List<Integer> suspendedProcess;

	private HashMap<Integer, MigratableProcess> processList;
	private HashMap<Integer, SlaveWorker> processWorker;

	private int processCount;

	public ProcessManager(String addr, int port) {
		processCount = 0;

		slaveList = new ArrayList<SlaveWorker>();
		slaveThread = new ConcurrentHashMap<Integer, Thread>();

		activeProcess = new ArrayList<Integer>();
		suspendedProcess = new ArrayList<Integer>();

		// <processID>, <process>
		processList = new HashMap<Integer, MigratableProcess>();

		processWorker = new HashMap<Integer, SlaveWorker>();

		// Start it as a master process manager
		if (addr == null) {
			this.isMaster = true;
			this.port = port;
		}
		// Start it as a slave process manager
		else {
			this.isMaster = false;
			this.masterAddr = addr;
			this.port = port;
		}
	}

	private void startMaster() {
		// Open a port to listen to slave connections
		Listener t = new Listener(port, this);
		t.start();
		Heartbeat heart = new Heartbeat(this);
		Thread heartbeat = new Thread(heart);
		heartbeat.start();
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
				case "slave":
					printSlaves();
					break;
				case "launch":
					launch(args);
					break;
				case "suspend":
					suspend(Integer.parseInt(args[1]));
					break;
				case "migrate":
					migrate(args);
					break;
				case "ping":
					pingSlave();
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

	public void launch(String[] args) {
		int result = 1;
		// launch -n <node> <name> <args>
		if (args[1].equals("-n")) {
			String[] cmds = new String[args.length - 4];
			System.arraycopy(args, 4, cmds, 0, args.length - 4);
			if (startProcess(args[3], cmds, args[2]) != 1) {
				result = -1;
			}
		}
		// launch <name> <args>
		else {
			String[] cmds = new String[args.length - 2];
			System.arraycopy(args, 2, cmds, 0, args.length - 2);
			if (startProcess(args[1], cmds, null) != 1) {
				result = -1;
			}
		}
		if (result != 1) {
			System.out.println("Fail to launch process");
		} else {
			System.out.println("Launch process Success");
		}
	}

	public int startProcess(String processName, String[] args, String addr) {
		MigratableProcess process;
		int processID;
		// Instantiate a process object
		try {
			Class<?> c = Class.forName(processName);
			Constructor<?> myCtor = c.getConstructor(String[].class);
			process = (MigratableProcess) myCtor
					.newInstance(new Object[] { args });
			processID = getProcessID();
			if (processID <= 0) {
				System.out.println("Invalid process id!");
				return -1;
			}
			serialize(process, processID);
		} catch (ClassNotFoundException e1) {
			System.out.println("Class Not Found Exception!" + e1.getMessage());
			return -1;
		} catch (NoSuchMethodException e1) {
			System.out.println("No Such Method Exception!" + e1.getMessage());
			return -1;
		} catch (SecurityException e1) {
			System.out.println("Security Exception!");
			return -1;
		} catch (InstantiationException e1) {
			System.out.println("Instantiation Exception!");
			return -1;
		} catch (IllegalAccessException e1) {
			System.out.println("Illegal Access Exception!");
			return -1;
		} catch (IllegalArgumentException e1) {
			System.out.println("Illegal Argument Exception!");
			return -1;
		} catch (InvocationTargetException e1) {
			System.out.println("Invocation Target Exception!");
			return -1;
		}

		SlaveWorker slave = pickSlave(addr);
		sendTo("run " + processID, slave.ip, slave.port);
		processList.put(processID, process);
		activeProcess.add(processID);
		processWorker.put(processID, slave);
		return 1;
	}

	public void migrate(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: migrate <Process ID> <Slave Node>");
			return;
		}
		int processID = Integer.parseInt(args[1]);
		SlaveWorker w = processWorker.get(processID);
		if (sendTo("suspend " + processID, w.ip, w.port).equals("ok")) {
			SlaveWorker slave = pickSlave(args[2]);
			processWorker.remove(processID);
			suspendedProcess.remove((Integer)processID);
			processWorker.put(processID, slave);
			activeProcess.add(processID);
			sendTo("run " + processID, slave.ip, slave.port);
		}

	}

	public void suspend(int pid) {
		SlaveWorker w = processWorker.get(pid);
		sendTo("suspend " + pid, w.ip, w.port);
		activeProcess.remove((Integer)pid);
		suspendedProcess.add(pid);
	}

	private void startSlave() {
		Listener t = new Listener(port, this);
		t.start();
		System.out.println("Connecting to server " + masterAddr + "...");
		try {
			String[] str = masterAddr.split(":");
			sendTo("connect " + InetAddress.getLocalHost().getHostName() + " "
					+ t.serverSocket.getLocalPort(), str[0],
					Integer.parseInt(str[1]));
		} catch (UnknownHostException e2) {
			System.exit(-1);
		}
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				System.out.println("Thread sleep interrupted");
			}

			// Concurrent issues not solved
			for (Integer processID : slaveThread.keySet()) {
				Thread thread = slaveThread.get(processID);
				try {
					thread.join(10);
				} catch (InterruptedException e) {
					System.out.println("Thread join interrupted");
				}
				if (!thread.isAlive()) {
					System.out.println("thread dies");
					processList.remove((Integer) processID);
					processWorker.remove((Integer) processID);
					activeProcess.remove((Integer) processID);
					String[] str = masterAddr.split(":");
					sendTo("die " + processID, str[0], Integer.parseInt(str[1]));
					slaveThread.remove(processID);
				}
			}
		}
	}

	public void dieProcess(int pid) {
		activeProcess.remove((Integer) pid);
		suspendedProcess.remove((Integer) pid);
	}

	private String sendTo(String s, String ip, int port) {
		String str = null;
		try {
			Socket socket = new Socket(ip, port);
			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());
			out.writeObject(s);
			out.flush();
			str = in.readObject().toString();
			in.close();
			out.close();
			socket.close();
		} catch (NumberFormatException e) {
			removeSlave(ip, port);
		} catch (UnknownHostException e) {
			removeSlave(ip, port);
		} catch (IOException e) {
			removeSlave(ip, port);
		} catch (ClassNotFoundException e) {
		}
		return str;
	}

	public int doLaunch(int pid) {
		try {
			MigratableProcess p = deSerialize(pid);
			Thread t = new Thread(p);
			t.start();
			processList.put(pid, p);
			activeProcess.add(pid);
			slaveThread.put(pid, t);
			return 1;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return -1;
		}
	}

	private void removeSlave(String ip, int port) {
		for (int i = 0; i < slaveList.size(); i++) {
			SlaveWorker w = slaveList.get(i);
			if (w.ip.equals(ip) && w.port == port) {
				slaveList.remove(w);
				System.out.print("Slave " + w.ip + ":" + w.port
						+ " disconnected\n>> ");
			}
		}
	}

	public int doSuspend(int pid) {
		try {
			MigratableProcess p = processList.get(pid);
			p.suspend();
			serialize(p, pid);
			activeProcess.remove((Integer) pid);
			suspendedProcess.add(pid);
			slaveThread.remove(pid);

			return 1;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return -1;
		}
	}
	
	public int addSlave(String addr) {
		SlaveWorker w = new SlaveWorker(addr);
		try {
			slaveList.add(w);
			return 1;
		} catch (Exception e) {
			System.out.println("Unable to add slave " + addr);
			return -1;
		}
	}

	private SlaveWorker pickSlave(String addr) {
		if (slaveList.size() <= 0) {
			System.out.println("No connected slaves!");
			System.exit(-1);
		}
		if (addr != null) {
			SlaveWorker w = slaveList.get(Integer.parseInt(addr));
			return w;
		}

		SlaveWorker w = slaveList.get(0);
		return w;
	}

	public void serialize(MigratableProcess p, int i) {
		TransactionalFileOutputStream file = new TransactionalFileOutputStream(
				"ser/" + i + ".ser", false);
		try {
			ObjectOutputStream out = new ObjectOutputStream(file);
			out.writeObject(p);
			out.close();
			file.close();
		} catch (IOException e) {
			System.out.println("IOException\n" + e.getMessage());
		}
	}

	public MigratableProcess deSerialize(int i) {
		TransactionalFileInputStream file = new TransactionalFileInputStream(
				"ser/" + i + ".ser");
		MigratableProcess p;
		try {
			ObjectInputStream in = new ObjectInputStream(file);
			p = (MigratableProcess) in.readObject();
			in.close();
			file.close();
			return p;
		} catch (IOException e) {
			System.out.println("IOException" + e.getMessage());
			return null;
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException");
			try {
				file.close();
			} catch (IOException e1) {
				System.out.println("IOException" + e1.getMessage());
			}
			return null;
		}
	}

	private int getProcessID() {

		processCount++;
		return processCount;
	}

	private void printJob() {
		System.out.println("Active Process:");
		if (activeProcess.size() == 0) {
			System.out.println("empty");
		}
		for (int i = 0; i < activeProcess.size(); i++) {
			System.out.println(activeProcess.get(i) + "  "
					+ processList.get(activeProcess.get(i)).toString());
		}
		System.out.println("\nSuspended Process:");
		if (suspendedProcess.size() == 0) {
			System.out.println("empty");
		}
		for (int i = 0; i < suspendedProcess.size(); i++) {
			System.out.println(suspendedProcess.get(i) + "  "
					+ processList.get(suspendedProcess.get(i)).toString());
		}
	}

	private void printSlaves() {
		for (int i = 0; i < slaveList.size(); i++) {
			System.out.println(slaveList.get(i).ip + ":"
					+ slaveList.get(i).port);
		}
	}

	private static void printUsage() {
		System.out
				.println("Usage: \n"
						+ "Start master Process Manager: -p <port number> \n"
						+ "Start worker(slave) Process Manager: -m <master ip:port> -p <slave port> \n"
						+ "Help: -h \n");
		System.exit(-1);
	}

	public void pingSlave() {
		for (int i = 0; i < slaveList.size(); i++) {
			SlaveWorker w = slaveList.get(i);

			if (!sendTo("check", w.ip, w.port).equals("hi")) {
				System.out.println("Slave " + w.ip + ":" + w.port
						+ " disconnected");
				// migrate work to another node

				slaveList.remove(i);
				i--;
			}
		}
	}

	public static void main(String[] args) {
		ProcessManager pm;
		if (args.length == 2 && args[0].equals("-p")) {
			pm = new ProcessManager(null, Integer.parseInt(args[1]));
			pm.startMaster();
			System.out.println(pm.port);
		} else if (args.length == 4 && args[0].equals("-m")
				&& args[2].equals("-p")) {
			pm = new ProcessManager(args[1], Integer.parseInt(args[3]));
			pm.startSlave();
			System.out.println(pm.port);
		} else {
			printUsage();
		}
	}
}
