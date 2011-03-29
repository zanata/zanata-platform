Usage:

TBD

mvn net.openl10n.flies:flies-maven-plugin:help
  OR
mvn net.openl10n.flies:flies-maven-plugin:help -Ddetail=true


Installation:

To use this plugin, make sure Flies is in your pluginRepositories.  
(One way of doing this is to compile Flies from source and run mvn 
install - this will put the  maven plugin into your local repo.) 

Then you can use the plugin this way:
  mvn net.openl10n.flies:flies-maven-plugin:listlocal


If you would prefer the shorter version:
  mvn flies:listlocal

You can edit your project's pom.xml this way:

<project>
  ...
  <build>
    <plugins>
      ...
      <plugin>
        <groupId>net.openl10n.flies</groupId>
        <artifactId>flies-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
  ...
</project>


Alternatively, for a global setting you can edit ~/.m2/settings.xml to include:

<settings>
  ...
  <pluginGroups>
    <pluginGroup>net.openl10n.flies</pluginGroup>
  </pluginGroups>
  ...
</settings>

