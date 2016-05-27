## Overview
Zanata will store files for some uploaded documents on the file system in a configured directory. This directory must be configured by using a system property. Zanata will create the directory if it does not yet exist.

Since files were previously stored as BLOBs in the database, there is a migration operation to move the files from the database to the file system. Migration is performed when liquibase is run, which will happen automatically at server startup, or can be initiated from the command line. If liquibase is run from the command line, the directory must be passed as a parameter to liquibase, in addition to the system property.

## Configuring the Directory (JNDI)
The path for document storage should be set using a system property when starting JBoss.

### Detailed Procedure
Open `$JBOSS_HOME/standalone/configuration/standalone.xml` and look for the `system-properties` section. Add the following property declaration:

```xml
<property name="zanata.file.directory" value="/example/path"/>
```

Alternatively, you can pass this and other system properties to JBoss when starting it (see JBoss documentation for details on how to do this).

## Running Liquibase from command line
This section will not explain how to run liquibase from the command line, just how to add the required parameter. The parameter is ```file.directory``` and should be added to the liquibase command in the form ```-Dzanata.file.directory=/example/path```. The value passed to this parameter should exactly match the value bound to the system property above.

## Moving files
The recommended process to change the document storage directory after migration is:
 - shut down Zanata server
 - move the entire contents of the previously configured directory to the new directory
 - change the JNDI binding to point to the new directory
 - re-start Zanata server
The file copying can be performed after changing the JNDI binding while the server is running, but this could cause problems if uploads are performed before or during the copying process.
