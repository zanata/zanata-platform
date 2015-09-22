<h1>Postgresql</h1>

From version 3.8 and onwards, Zanata uses [Postgresql](http://www.postgresql.org) as its back end datastore. In order to migrate a MySQL database for a previous version of Zanata, there are certain steps that need to be followed:

## 1. Make sure you are running Zanata 3.7.1

The database migration can only happen if Zanata 3.7.1 is already installed. So, before attempting a migration please make sure your Zanata installation is at 3.7.1

## 2. Install Postgresql

This guide will not cover the details on how to install Postgresql as it varies from system to system. However, at the end of the installation it is expected that there is a user with the permission to connect to a database and create / alter the database structure.

## 3. Migrate the MySQL database to Postgresql

The Zanata project offers a migration script from MySQL to Postgresql for convenience. It's a [Groovy](http://www.groovy-lang.org) script that will move your existing Zanata 3.7.1 database to Postgresql. The script can be found at:

(https://github.com/zanata/zanata-server/blob/postgresql/zanata-war/etc/scripts/Mysql2Postgresql.groovy)

You can run the following commands to view the help message:

```sh
cd zanata-war/etc/scripts/
groovy MysqlPostgresql -h
```

Here is an example of how to run the script:

```sh
groovy Mysql2Postgresql.groovy -mysqluser mysqlusername -mysqlschema zanata -mysqlport 3306 -psqluser psqlusername -psqlpassword camunoz -psqldb zanata
```

The help menu will show all the possible arguments for the command line.

The script will completely migrate all necessary tables, sequences, triggers, and data from the MySQL database to the new Postgresql one.

## 4. Changes to JBoss (or Wildfly) infrastructure

After the migration, the `standalone.xml` file used to start JBoss (or Wildfly) should be updated to contain the new definition for the Zanata datasource like this:

```xml
<datasource jndi-name="java:jboss/datasources/zanataDatasource"
    pool-name="zanataDatasource" enabled="true" use-ccm="true">
    <connection-url>jdbc:postgresql://localhost:5432/zanata</connection-url>
    <driver-class>org.postgresql.Driver</driver-class>
    <driver>postgresql-jdbc.jar</driver>
    <pool>
        <min-pool-size>0</min-pool-size>
        <max-pool-size>20</max-pool-size>
        <flush-strategy>FailingConnectionOnly</flush-strategy>
    </pool>
    <security>
        <user-name>user</user-name>
        <password>password</password>
    </security>
    <statement>
        <track-statements>NOWARN</track-statements>
    </statement>
</datasource>
```

Additionaly, the Postgresql jdbc driver should be located under Jboss' `standalone/deployments` directory. The name of the jar file must match the `driver` attribute in the above xml snippet.

After these steps are followed, Zanata 3.8 should be able to start using your new Postgresql database.
