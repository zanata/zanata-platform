<h1>JMS Messaging</h1>

Make sure your jboss standalone.xml has management native socket binding enabled.
You should see an entry like this:

    <socket-binding name="management-native" interface="management"
      port="${jboss.management.native.port,env.JBOSS_MANAGEMENT_NATIVE_PORT:9999}" />

Once you have management socket binding open, download the [messaging config file](https://raw.githubusercontent.com/zanata/zanata-server/master/etc/scripts/standalone.cli.messaging.config).

> This config file has been tested with EAP6 and WildFly 10.0.0.Final.

Run

    jboss-cli.sh --file=path/to/standalone.cli.messaging.config

Verify that the result is successful.

If it says something like this:

> Unexpected command 'jms-queue add --queue-address=MailsQueue --durable=true
> --entries=["java:/jms/queue/MailsQueue"]'. Type 'help --commands' for the
> list of supported commands.

then please make sure you are using standalone-full.xml, not standalone.xml (which doesn't include the messaging subsystem). (You will need to restart the app server if you have to change this.)

You should end up with a configuration file which looks like one of these examples (**NOTE:** xmlns may vary depending on which version of EAP or WildFly you are using.):

EAP 6:

```xml
<server xmlns="urn:jboss:domain:1.7">
  <extensions>
    <!-- omit other things -->
    <extension module="org.jboss.as.messaging"/>
    <!-- omit other things -->
  </extensions>
  <profile>
    <!-- omit other things -->
    <subsystem xmlns="urn:jboss:domain:ejb3:1.5">
      <!-- omit other things -->
      <mdb>
          <resource-adapter-ref resource-adapter-name="hornetq-ra"/>
          <bean-instance-pool-ref pool-name="mdb-strict-max-pool"/>
      </mdb>
      <!-- omit other things -->
    </subsystem>
    <!-- omit other things -->
    <subsystem xmlns="urn:jboss:domain:messaging:1.4">
      <hornetq-server>
          <!-- omit other things -->
          <jms-queue name="MailsQueue">
            <entry name="jms/queue/MailsQueue"/>
            <durable>true</durable>
          </jms-queue>
          <!-- omit other things -->
        </jms-destinations>
      </hornetq-server>
    </subsystem>
```

or (Wildfly 10, EAP 7):

```xml
<server xmlns="urn:jboss:domain:4.0">
  <extensions>
    <!-- omit other things -->
    <extension module="org.wildfly.extension.messaging-activemq"/>
    <!-- omit other things -->
  </extensions>
  <profile>
    <!-- omit other things -->
    <subsystem xmlns="urn:jboss:domain:ejb3:4.0">
      <!-- omit other things -->
        <mdb>
          <resource-adapter-ref resource-adapter-name="${ejb.resource-adapter-name:activemq-ra.rar}"/>
          <bean-instance-pool-ref pool-name="mdb-strict-max-pool"/>
        </mdb>
      <!-- omit other things -->
    </subsystem>
    <!-- omit other things -->
    <subsystem xmlns="urn:jboss:domain:messaging-activemq:1.0">
      <server name="default">
        <!-- omit other things -->
          <jms-queue name="MailsQueue" entries="java:/jms/queue/MailsQueue"/>
        <!-- omit other things -->
      </server>
    </subsystem>
```

The only Zanata-specific change here is the addition of the JMS queue "MailsQueue" - the mdb configuration is simply copied from `standalone-full.xml`.

Restart your server to ensure changes are applied.
