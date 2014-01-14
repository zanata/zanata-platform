Usage of files under src/test/resources.
For more detail on why we have these see https://github.com/zanata/zanata-server/wiki/JBoss-AS-7

- conf/standalone.xml
  This is copied from $JBOSS_HOME/standalone/configuration/standalone.xml with added security domain for zanata.

- datasource/zanata-ds.xml
  This is the datasource file we need to deploy into JBoss
