== Installing Flies ==

I'll refer to $FLIES as the directory where your flies root directory.


Pre-reqs
========
- Java 1.6.x (openjdk is ok)
- Mysql 5.x
- Maven 2.x
- Ant 1.7.x
- JBoss 4.2.3.GA (the jdk6 version)

Running Integration Tests in Debug mode
=======================================

mvn -Dmaven.failsafe.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE" verify