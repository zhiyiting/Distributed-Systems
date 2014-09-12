Lab 1: Portable, Migratable Work

One classic problem in distributed systems involves the migration of work. This problem is often referred to as that of Process Migration. But, the vehicle might be processes, objects, threads, or any other unit of active work. The common idea is moving in-progress work with as little disruption and 
wastage as possible. 

To make this possible, the work-in-progress should be agnostic to what node it is running on and should be unaware of whether it has been moved from one node to another. Thus, it can enjoy the illusion that it is running on a single node during its entire execution. 

To achieve this, it is necessary to be able to pause and package up a process, ship it to another node, and unpackage and resume it such that it is running again. The process should not lose it’s location in the program, variables, open files, network connections or any other state.

1. Design and Usage

The framework is designed with Master/Slave design pattern. The main function is in the ProcessManager.java class. Depending on user's input, it will instantiate either a master ProcessManager object or a slave ProcessManager object. The master is responsible for scheduling which slave (node) the process is going to run on. The master also will ping the slave every 5 seconds to see if the slave is still connected; if not, the slave will be removed and following processes won't be sent to the dead slave. The master also contains a terminal, where you can not only launch, suspend and migrate processes, but also view the processes being run, and the processes being suspend.

2. Success and Bugs

Success:
User can type in the processes that he wants to run. By default, the process will be run by the node 0. The user can also specify other nodes by writing additional arguments.
User can see the process number by typing jobs, and he can see the active and suspended processes.
User can see the slave number by typing slave.
User can type in the process number that he wants to suspend.
User can type in the process number that he wants to migrate, with additional arguments of the node number that he wants it to migrate to.
User can exit from the terminal.
Bug:
When writing to same file, there will be concurrency issues which haven't been fixed.
Sometimes there will be Exceptions thrown, due to solo limited development time. If given more time, more testing will be done and they should be fixed.

3. Build, Deploy and Run

To build:
- Go to the source code folder src
make clean
make
Now you can see the classes have been compiled to .class files.
To run:
At the master side, open the listener at your specified port
java ProcessManager.java -p <port number>
At the slave side, connect to the master, and open a local listener
java ProcessManager.java -m <master's host:port> -p <port number>
Now the slave(s) should be connected to the master. You can see a terminal at master side.
????
To see the slaves:
slave
To launch process:
Launch a process at master side (GrepProcess for example)
launch ProcessName querystring <infile> <outfile>

4. Dependency

Developed on a Mac OS X machine with JSE-1.8. No additional libraries required.

5. Testing

(Get process number and node from "jobs" and "slave")
Test 1
launch FilterNumber lol.txt out1
migrate <process number> <node you want>
suspend <process number>
Test 2
launch Capitalize lol.txt out2
migrate <process number> <node you want>
suspend <process number>
