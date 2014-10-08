JFLAGS =
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	RegistryServer.java \
	RMIServer.java \
	ZipCodeServerImpl.java \
	ZipCodeClient.java \
	ZipCodeRListImpl.java \
	ZipCodeRListClient.java \
	NameServerImpl.java \
	NameClient.java \
	TestImpl.java \
	TestClient.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class


