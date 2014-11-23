JFLAGS =
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) */*.java */*/*.java

CLASSES = \
	conf/Configuration.java \
	framework/MapReduceEngine.java \
	example/WordCount.java \
	example/BasketballPlayerSalaryRank.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) */*.class */*/*.class
	rm -rf unix*

startMR:
	java framework.MapReduceEngine

