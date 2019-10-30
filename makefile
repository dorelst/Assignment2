JFLAGS = -g
JC = javac
JVM= java 
FILE=
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        TCPServer.java \
        Connection.java \
        TCPClient.java

MAIN = TCPServer

default: classes

classes: $(CLASSES:.java=.class)

run: classes
	$(JVM) $(MAIN)

clean:
	$(RM) *.class
