Installing Flies

1) Configure Database settings in
 ./resources/flies-{dev|prod|test}-ds.xml

When using MySQL, make sure you enable utf8 support when creating the database
  CREATE DATABASE flies CHARACTER SET utf8 COLLATE utf8_general_ci default charset utf8

2) cp lib/mysql-connector-java.jar $JBOSS_HOME/server/default/lib/
 
3) Deploy using 'ant deploy'