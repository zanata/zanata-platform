== Installing Flies ==

I'll refer to $FLIES as the directory where your flies root directory.


Pre-reqs
========
- Java 1.6.x (openjdk is ok)
- Mysql 5.x
- Maven 2.x
- Ant 1.7.x
- JBoss 4.2.3.GA (the jdk6 version)

Build configuration
===================
$ cd $FLIES/flies-web/
$ cp build.properties.sample build.properties

Now Edit the values in build.properties to point to your JBoss installation directory.
(Note that we have to be able to write to that directory)

Database Configuration
======================

The database is by default using a mysql database called 'flies' under the 
'root' user, with a blank password. You can change this by editing
 $FLIES/flies-web/resources/flies-{dev|prod|test}-ds.xml

You first need to create the databse in the mysql console:
$ mysql -u root

When using MySQL, make sure you enable utf8 support when creating the database
  CREATE DATABASE flies CHARACTER SET utf8 COLLATE utf8_general_ci default charset utf8;

Install maven dependencies
=============================

In $FLIES/
$ mvn install

In $FLIES/shotoku
$ mvn install
 
Install JBoss AS dependencies
================================

In $FLIES/flies-web
$ ant app-update-deps
 
Deploy
=========

In $FLIES/flies-web
$ ant clean app-reexplode 


