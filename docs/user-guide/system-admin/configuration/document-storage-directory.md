## Overview
Zanata will store files for some uploaded documents on the file system in a configured directory. This directory must be configured by binding to a JNDI name. Zanata will create the directory if it does not yet exist.

Since files were previously stored as BLOBs in the database, there is a migration operation to move the files from the database to the file system. Migration is performed when liquibase is run, which will happen automatically at server startup, or can be initiated from the command line. If liquibase is run from the command line, the directory must be passed as a parameter to liquibase, in addition to the JNDI name binding.

## Configuring the Directory (JNDI)
The path for document storage should be bound to JNDI name ```java:global/zanata/files/document-storage-directory```

### Detailed Procedure
Open `$JBOSS_HOME/standalone/configuration/standalone.xml` and look for the `naming` subsystem. Add the following section to the `bindings` section:

```xml
<subsystem xmlns="urn:jboss:domain:naming:1.3">
    ...
    <bindings>
        <simple name="java:global/zanata/files/document-storage-directory" value="/example/path"/>
    </bindings>
    ...
</subsystem>
```

## Running Liquibase from command line
This section will not explain how to run liquibase from the command line, just how to add the required parameter. The parameter is ```document.storage.directory``` and should be added to the liquibase command in the form ```-Ddocument.storage.directory=/example/path```. The value passed to this parameter should exactly match the value bound to the above JNDI name.

## Moving files
The recommended process to change the document storage directory after migration is:
 - shut down Zanata server
 - move the entire contents of the previously configured directory to the new directory
 - change the JNDI binding to point to the new directory
 - re-start Zanata server
The file copying can be performed after changing the JNDI binding while the server is running, but this could cause problems if uploads are performed before or during the copying process.