Usage:

TBD

mvn org.zanata:zanata-maven-plugin:help
  OR
mvn org.zanata:zanata-maven-plugin:help -Ddetail=true


Installation:

To use this plugin, make sure Zanata is in your pluginRepositories.  
(One way of doing this is to compile Zanata from source and run mvn 
install - this will put the  maven plugin into your local repo.) 

Then you can use the plugin this way:
  mvn org.zanata:zanata-maven-plugin:listlocal


If you would prefer the shorter version:
  mvn zanata:listlocal

You can edit your project's pom.xml this way:

<project>
  ...
  <build>
    <plugins>
      ...
      <plugin>
        <groupId>org.zanata</groupId>
        <artifactId>zanata-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
  ...
</project>


Alternatively, for a global setting you can edit ~/.m2/settings.xml to include:

<settings>
  ...
  <pluginGroups>
    <pluginGroup>org.zanata</pluginGroup>
  </pluginGroups>
  ...
</settings>

