Ant's classloader setup can be confusing, because the classloader which knows 
how to load a task's classpath/classpathref is not set as the Thread Context 
ClassLoader (TCL).  If the task uses other libraries which try to use the 
TCL, they can't find the resources they want, and much pain ensues...

Naturally, none of this is evident when using Ant's BuildFileTest, only when 
using Ant from the command-line.  Hence the need for a command-line-based test 
of the Ant tasks.

To run the round-trip Ant tests, just go into the flies-ant-demo directory, and run:
    ant

To do this via Maven:
    mvn -Drunant install [-DskipTests=true]

This test converts and sends the properties files in flies-ant-demo/test1 to 
the flies server via the REST api, retrieves them again, and then compares the
resulting properties files with the originals, failing if they don't match.
