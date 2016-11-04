To use the Maven Plugin, some Zanata configuration files and an installation of Apache Maven are needed. Some additional configuration will let you use a shorter command to run the Maven Plugin.

For Apache Maven installation, see [Installing the Maven Plugin](/maven-plugin/installation).


## Zanata Project Configuration

The Zanata Maven plugin uses the general configuration files `zanata.ini` and `zanata.xml` in the same way as zanata-cli. For instructions on setting up these files, see [Configuring the Client](/configuration).

In addition, parameters such as source directory can be specified in `pom.xml` so that they are not needed on the command line.


## Command Configuration

When Maven is installed, a verbose command can be used to run the Zanata Maven Plugin:

```bash
mvn org.zanata:zanata-maven-plugin:<PLUGIN_VERSION>:help
```

The following instructions will allow the concise form of the command to be used instead:

```bash
mvn zanata:help
```


### Global Command Configuration

To use the concise form of commands for any project, open `~/.m2/settings.xml` and ensure that pluginGroup `org.zanata` is present in pluginGroups:

```xml
<settings ...>
  ...
  <pluginGroups>
    <pluginGroup>org.zanata</pluginGroup>
    ...
  </pluginGroups>
  ...
</settings>
```

This allows maven to use the latest version of the Zanata plugin when any `mvn zanata:*` command is run.


### Per-Project Command Configuration

The plugin can be added to a single project by adding some elements to the project's `pom.xml` file. 

The following shows a build entry in `pom.xml` that will use version 3.4.0 of the Zanata plugin, and specifies the current directory (`.`) for source documents. This is equivalent to specifying the source directory on the command line as `-Dzanata.srcDir="."`.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.zanata</groupId>
      <artifactId>zanata-maven-plugin</artifactId>
      <version>3.4.0</version>
      <configuration>
        <srcDir>.</srcDir>
      </configuration>
    </plugin>
  </plugins>
</build>
```
