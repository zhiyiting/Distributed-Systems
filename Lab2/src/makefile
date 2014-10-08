JFLAGS =
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	RegistryServer.java \
	RMIServer.java \
	ZipCodeRListImpl.java \
	ZipCodeRListClient.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class


