JFLAGS =
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $.*/*.java

CLASSES = \
	conf/Configuration.java \
	util/CoordinatorConsole.java \
	util/SlaveConsole.java \
	util/Worker.java \
	client/MapReduceEngine.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) ./*/*.class


