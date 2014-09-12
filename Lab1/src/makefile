JFLAGS =
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	ProcessManager.java \
	GrepProcess.java \
	Capitalize.java \
	FilterNumber.java \
	Unzip.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
