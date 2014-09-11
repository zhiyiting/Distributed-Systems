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
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

public class ProcessManager {
	public Boolean isMaster;
	public String masterAddr;
	public int port;

	private List<SlaveWorker> slaveList;
	private List<Thread> slaveThread;

	private List<Integer> activeProcess;
	private List<Integer> suspendedProcess;
	private List<Integer> finishedProcess;

	private Hashtable<Integer, MigratableProcess> processList;
	private Stack<Integer> idPool;
	private int processCount;

	public ProcessManager(String addr, int port) {
		processCount = 0;

		idPool = new Stack<Integer>();
		slaveList = new ArrayList<SlaveWorker>();
		slaveThread = new ArrayList<Thread>();

		activeProcess = new ArrayList<Integer>();
		suspendedProcess = new ArrayList<Integer>();
		finishedProcess = new ArrayList<Integer>();

		// <processID>, <process>
		processList = new Hashtable<Integer, MigratableProcess>();

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
		System.out.println("Start Process Manager (Master) : port " + port);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				System.out.print(port + ">> ");
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
				case "migrate":
					break;
				case "suspend":
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
		Object[] cmds = null;
		int result = 1;
		// launch -n <node> <name> <args>
		if (args[1].equals("-n") && args.length == 5) {
			System.arraycopy(args, 4, cmds, 0, args.length - 4);
			if (startProcess(args[3], cmds, args[2]) != 1) {
				result = -1;
			}
		}
		// launch <name> <args>
		else if (args.length == 3) {
			System.arraycopy(args, 2, cmds, 0, args.length - 2);
			if (startProcess(args[1], cmds, null) != 1) {
				result = -1;
			}
		} else {
			System.out.println("Usage:\n"
					+ "launch -n <node> <process name> <args>\n"
					+ "launch <process name> <args>");
		}
		if (result != 1) {
			System.out.println("Failure to launch process");
		}
	}

	public int startProcess(String processName, Object[] args, String addr) {
		MigratableProcess process;
		int processID;
		// Instantiate a process object
		try {
			Class<?> c = Class.forName(processName);
			Constructor<?> myCtor = c.getConstructor();
			process = (MigratableProcess) myCtor.newInstance(args);
			processID = getProcessID();
			if (processID <= 0) {
				System.out.println("Invalid process id!");
				return -1;
			}
			serialize(process, processID);
		} catch (ClassNotFoundException e1) {
			System.out.println("Class Not Found Exception!");
			return -1;
		} catch (NoSuchMethodException e1) {
			System.out.println("No Such Method Exception!");
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

		String slave = pickSlave(addr);
		String[] slaveAddr = slave.split(":");
		// send it to slave
		try {

			Socket socket = new Socket(slaveAddr[0],
					Integer.parseInt(slaveAddr[1]));
			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());
			out.writeObject(processID);
			out.flush();
			out.close();
			socket.close();
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException");
			return -1;
		} catch (IOException e) {
			System.out.println("IOException");
			return -1;
		}
		processList.put(processID, process);
		return 1;
	}

	private void startSlave() {
		String[] ip = masterAddr.split(":");
		try {
			Socket socket = new Socket(ip[0], Integer.parseInt(ip[1]));
			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());
			Listener t = new Listener(port, this);
			t.start();
			System.out.println("Connecting to server " + masterAddr + "...");
			out.writeObject("connect:" + InetAddress.getLocalHost().getHostName() + ":"
					+ t.serverSocket.getLocalPort());
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());
			try {
				System.out.println(in.readObject());
			} catch (ClassNotFoundException e) {
				System.out.println("Couldn't connect to server!");
				System.exit(-1);
			}
			out.flush();
			out.close();
			in.close();
			socket.close();
		} catch (NumberFormatException e) {
			System.out.println("NumberFormatException");
			System.exit(-1);
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException");
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("IOException");
			System.exit(-1);
		}
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				System.out.println("Thread sleep interrupted");
			}
			for (int i = 0; i < slaveThread.size(); i++) {
				Thread thread = slaveThread.get(i);
				try {
					thread.join();
				} catch (InterruptedException e) {
					System.out.println("Thread join interrupted");
				}
				if (!thread.isAlive()) {
					slaveThread.remove(i);
					i--;
				}
			}
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

	private String pickSlave(String addr) {
		if (slaveList.size() <= 0) {
			System.out.println("No connected slaves!");
			System.exit(-1);
		}
		if (addr != null) {
			if (addr.charAt(0) >= '0' && addr.charAt(0) <= '9') {
				SlaveWorker w = slaveList.get(Integer.parseInt(addr));
				return w.ip + ":" + w.port;
			}
			return addr;
		}
		int minCount = slaveList.get(0).getLoad();
		int index = 0;
		for (int i = 1; i < slaveList.size(); i++) {
			int cur = slaveList.get(i).getLoad();
			if (minCount > cur) {
				minCount = cur;
				index = i;
			}
		}
		SlaveWorker w = slaveList.get(index);
		return w.ip + ":" + w.port;
	}

	public void serialize(MigratableProcess p, int i) {
		TransactionalFileOutputStream file = new TransactionalFileOutputStream(
				"ser/" + i + ".ser", false);
		try {
			ObjectOutputStream out = new ObjectOutputStream(file);
			p.suspend();
			out.writeObject(p);
			out.close();
			file.close();
		} catch (IOException e) {
			System.out.println("IOException");
		}
	}

	public MigratableProcess deSerialize(int i) {
		TransactionalFileInputStream file = new TransactionalFileInputStream(
				"ser/" + i + ".ser");
		MigratableProcess p = null;
		try {
			ObjectInputStream in = new ObjectInputStream(file);
			p = (MigratableProcess) in.readObject();
			in.close();
			file.close();
		} catch (IOException e) {
			System.out.println("IOException");
			return p;
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException");
			try {
				file.close();
			} catch (IOException e1) {
				System.out.println("IOException");
			}
			return p;
		}
		return p;
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

	private void pingSlave() {
		for (int i = 0; i < slaveList.size(); i++) {
			SlaveWorker w = slaveList.get(i);
			try {
				Socket s = new Socket(w.ip, w.port);
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(s.getInputStream());
				out.writeObject("Are you there?");
				out.flush();
				try {
					System.out.println(in.readObject());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				in.close();
				out.close();
				s.close();
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
