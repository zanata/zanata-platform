Usage:

TBD

mvn org.fedorahosted.flies:flies-maven-plugin:help
  OR
mvn org.fedorahosted.flies:flies-maven-plugin:help -Ddetail=true


Installation:

To use this plugin, make sure Flies is in your pluginRepositories.  
(One way of doing this is to compile Flies from source and run mvn 
install - this will put the  maven plugin into your local repo.) 

Then you can use the plugin this way:
  mvn org.fedorahosted.flies:flies-maven-plugin:listlocal


If you would prefer the shorter version:
  mvn flies:listlocal

You can edit your project's pom.xml this way:

<project>
  ...
  <build>
    <plugins>
      ...
      <plugin>
        <groupId>org.fedorahosted.flies</groupId>
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
    <pluginGroup>org.fedorahosted.flies</pluginGroup>
  </pluginGroups>
  ...
</settings>

