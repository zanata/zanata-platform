Usage of files under src/test/resources.
For more detail on why we have these see https://github.com/zanata/zanata-server/wiki/JBoss-AS-7

- conf/standalone.xml
  This is copied from $JBOSS_HOME/standalone/configuration/standalone.xml with added security domain for zanata.
  And socket binding for osgi-http (for cargo to use):
  <socket-binding name="osgi-http" interface="management" port="8090"/>

- as7module/sun/module.xml
  This is copied from $JBOSS7_HOME/modules/system/layers/base/sun/jdk/main/module.xml with added <path name="com/sun/management"/>

- as7module/zanata/module.xml
  This adds a new module zanata settings for zanata.properties

- datasource/zanata-ds.xml
  This is the datasource file we need to deploy into JBoss