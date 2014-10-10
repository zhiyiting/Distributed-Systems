Lab 2: Design and Implementation of an RMI Facility for Java

Provide a mechanism by which objects within one Java Virtual Machine (JVM) can invoke methods on objects within another JVM, even if the target object resides within a JVM hosted by a different, but network accessible, machine. 

1. Implementation
1) Remote Object Reference
RemoteObjectRef is a serializable class so that it can be sent over the network. It contains the following information that is needed to find an object: host and port that the object resides on, service name, and remote interface name. It also implements a localize function to create local stubs for remote objects.
2) Registry
The Registry maintains a hash table with the service names and remote object references. Servers can register, update and unregister remote objects by using bind, rebind and unbind methods. The Registry will be the first contact point for the client by using lookup method to get a remote object reference.
The registry server is a multithreaded server. When the server registers object to the registry, the registry will start a new thread and implement the corresponding action.
3) Server
The server is the place where the service object resides. It provides a shell interface, where server administrator can register, update and unregister objects to the registry.
It listens to incoming messages on a single thread called ServerListener. When a message arrives, a new dispatcher thread will be created to unmarshall and process the message.
4) Server-side Dispatcher
The dispatcher is responsible for unmarshaling the method invocation. When an invocation request is received by the server, a new Dispatcher thread will be created. The dispatcher will then unmarshall the message and invokes a local function call.
5) Client
The client looks up remote object from the server by RMINaming object. RMINaming provides the lookup method and encapsulates the communication between the client and registry. The client will use the stub to invoke methods.
6) Client-side Stub
The stub is responsible for marshaling method invocations. After the client looks up the server and gets a remote object reference, an invocation handler is used to generate a proxy and save it as a stub. It is required that the user has the interface .class file for generating the local stub. The stub will be maintained in a hash table with its service name so that it can be reused.
2. Feature
1) Communication Module and Socket Cache
The Communication Module is a class that can handle communications for both client and server sides.
SocketCache is a class that used a hash table to store sockets in order to reuse them in the future. It is a LRU cache that has a capacity of 10 sockets by default. The cache is stored in a form of hash table defined by the host and port string.
When one side wants to talk to the other side, it will first search the cache if the socket already exists, and use the existed input/output stream. If not, a new socket will be made and cached. SocketInfo contains information about a socket with socket, inputstream and outputstream.

2) Message
The Message class defines a general format for representing messages between classes. The RMIMessage class is serializable in order to be sent over network.
There are two types of messages. Both types contain destination host and port. The first type is a one-sentence simple message that requires method (title) and content. For example, lookup, bind messages are composed using this format. The second type is invocation message. It consists of a service name as title, and method name, parameter types, and parameters.
3) Remote Exception
Remote object calls can fail. The RemoteException class defines a kind of exception that will be thrown on server side. There are several scenarios that a remote exception will be thrown:
- Registry server is down / Server is down. A heartbeat thread will be implemented to detect
if the connection persists.
- Service name doesn’t exist.
- Remote method invocation failure.
3. Bugs and Unimplemented Pieces
1) Stub complier
Stub complier is not implemented, and invocation handler is used instead. The complier will be easy
to add in the StubGenerator class.
2) Mechanism for downloading .class
Since I’m working on AFS, I’m assuming that I have the class file.
3) Garbage Collection
4) Synchronization (thread safe) issues for the remote objects
4. Build, Run and Test
4.1. Build
1) cd into src/ folder
2) make clean
3) make
4.2. Run
1) Start Registry Server
java RegistryServer <registry port number>
2) Start RMI Server
java RMIServer <rmi port number> -r <registry port number>
3) Run client program
java <client program> <registry port> <registry host> <service name> <args>
4.3. Example Server and Client for Testing
1) Start Registry Server
- Open a terminal window
- ssh andrewId@unix2.andrew.cmu.edu
- cd to the src folder
- make clean, make
- java RegistryServer 15410
- See the message: Registry server started, listening on port 15410
2) Start RMI Server
- Open a new terminal window
- ssh andrewId@unix2.andrew.cmu.edu
- cd to the src folder
- java RMIServer 15440 –r 15410
- See the message: Server started, listening on port 15440.
3) Run Client Program
- Open a new terminal window
- If the project file exists locally, you can cd to the src folder locally, make clean, make
- If the project file is on AFS, ssh andrewId@unix.andrew.cmu.edu, cd to the src folder
a. Example 1: TestClient (without argument)
- Go to RMIServer’s terminal
- bind TestImpl test1
- See the message: Bind Success
- Go back to Client terminal
- java TestClient 128.2.13.134 15410 test1
b. Example2: TestClient (with argument)
- Go to RMIServer’s terminal
- unbind test1
- bind TestImpl test1
- Go back to Client terminal
- java TestClient 128.2.13.134 15410 test1 echo
c. Example 3: ZipCodeClient
- RMIServer’s terminal: bind ZipCodeServer Impl zip
- Client’s terminal: java ZipCodeClient 128.2.13.134 15410 zip ExampleData
d. Example 4: ZipCodeRListClient
- RMIServer’s terminal: rebind ZipCodeRListImpl zip
- Client’s terminal: java ZipCodeRListClient 128.2.13.134 15410 zip ExampleData