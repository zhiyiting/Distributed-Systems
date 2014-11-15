JFLAGS =
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) */*.java */*/*.java

CLASSES = \
	conf/Configuration.java \
	client/MapReduceEngine.java \
	example/WordCount.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) */*.class */*/*.class
	rm -rf unix*


