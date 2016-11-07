## What you need

- Make sure JDK is installed, [download it here](www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
- Supported JBoss or Wildfly server.
  - JBoss Enterprise Application Platform 6.4 (EAP). This is the recommended container for Zanata, and it can be downloaded from [jboss.org downloads](http://www.jboss.org/jbossas/downloads/). [Installation instructions](http://www.jboss.org/products/eap/get-started/#!project=eap).
  - Wildfly is much easier to install on Mac. Recommended version is 8.1.x, which can be downloaded from [wildfly.org downloads](http://wildfly.org/downloads/). You can also install with `brew install wildfly-as`.
- A suitable MySQL database. This is NOT included in the Zanata archive. You can [download MySQL here](http://dev.mysql.com/downloads/mysql/). You can also install with `brew install mysql`.
- An email (SMTP) server to perform certain notifications. Mac OSX 10.10 can use [postfix](http://www.developerfiles.com/how-to-send-smtp-mails-with-postfix-mac-os-x-10-8/).
- JDK version 7 or later (7 is recommended for EAP as it is not yet certified to run against Java 8). [OpenJDK](http://openjdk.java.net/install/) is recommended, but you can also download [Oracle's JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- Install maven `brew install maven`.

## Setting up the Zanata Database

1. Make sure you have downloaded MYSQL. Start it one the command line with `mysql -u root`.

2. Start MySQL service and create a database schema for Zanata.
`CREATE DATABASE zanata /**!40100 DEFAULT CHARACTER SET utf8 **/;`

## Installing from source

1. Run `git clone https://github.com/zanata/zanata-server.git`
2. Run `mvn clean package -Dchromefirefox -DskipTests -Dfunctional-test -Pwildfly8`
3. Wait for the build and test to complete. This takes about 60 mins on our machine. You may want to go grab some coffee.
4. Run `brew install gradle`
5. Run `cd zanata-overlay && gradle`
6. `unzip target/zanata-*-wildfly*.zip -d $JBOSS_HOME`
7. `cd $JBOSS_HOME/bin/zanata-installer`
8. `chmod +x install.sh`
9. `./install.sh` This will prompt you for database configuration details
10. `cp -r <zanata-server-repo-dir>/zanata-war/target/zanata-<version>.war $JBOSS_HOME/standalone/deployments/zanata.war`

## Run

1. `$JBOSS_HOME/bin/standalone.sh`
2. go to `localhost:8080/zanata`
3. Create an account
4. Run `UPDATE HAccount SET enabled = true WHERE username = 'myusername';` in mysql 
5. Then run ` insert into HAccountMembership(accountId, memberOf) values((select id from HAccount where username = 'myusername'), (select id from HAccountRole where name = 'admin'));` in mysql.

## After pulling new changes to zanata-server

## Running zanata-spa

zanata-spa is the javascript module for the new translation editor, that will replace the older editor in the near future.

1. `unzip $JBOSS_HOME/standalone/deployments/zanata.war -d zanatawar && rm zanata.war && mv zanatawar zanata.war` Turn zanata.war into a directory
2. `npm run build && cp -r <spa-directory>/build/* $JBOSS_HOME/standalone/deployments/zanata.war/app`
