Make sure your jboss standalone.xml has management native socket binding enabled.
You should see an entry like this:

    <socket-binding name="management-native" interface="management"
      port="${jboss.management.native.port,env.JBOSS_MANAGEMENT_NATIVE_PORT:9999}" />

Once you have management socking binding open, download the [messaging config file](https://raw.githubusercontent.com/zanata/zanata-server/master/etc/scripts/standalone.cli.messaging.config).

> This config file has been tested with EAP6 and Wildfly8.1.0-FINAL

Run 

    jboss-cli.sh --file=path/to/standalone.cli.messaging.config

Verify the result is successful. You should have the configuration file looks like below (**NOTE:** xmlns will vary depending on which version of EAP or wildfly you are using.):

```xml
<server xmlns="urn:jboss:domain:1.4">
  <extensions>
    <!-- omit other things -->
    <extension module="org.jboss.as.ejb3" />
    <extension module="org.jboss.as.messaging"/>
    <!-- omit other things -->
  </extensions>
  <profile>
    <subsystem xmlns="urn:jboss:domain:ejb3:1.4">
      <!-- omit other things -->
      <mdb>
          <resource-adapter-ref resource-adapter-name="hornetq-ra"/>
          <bean-instance-pool-ref pool-name="mdb-strict-max-pool"/>
      </mdb>
    <!-- omit other things -->
    </subsystem>
    <!-- omit other things -->
    <subsystem xmlns="urn:jboss:domain:messaging:1.2">
      <hornetq-server>
        <persistence-enabled>true</persistence-enabled>
        <security-enabled>false</security-enabled>
        <journal-type>NIO</journal-type>
        <journal-file-size>102400</journal-file-size>
        <journal-min-files>2</journal-min-files>
        <connectors>
          <in-vm-connector name="in-vm" server-id="0"/>
        </connectors>
        <acceptors>
          <in-vm-acceptor name="in-vm" server-id="0"/>
        </acceptors>
        <security-settings>
          <security-setting match="#">
            <permission type="send" roles="guest"/>
            <permission type="consume" roles="guest"/>
            <permission type="createNonDurableQueue" roles="guest"/>
            <permission type="deleteNonDurableQueue" roles="guest"/>
          </security-setting>
        </security-settings>
        <address-settings>
          <!--default for catch all-->
          <address-setting match="#">
            <dead-letter-address>jms.queue.DLQ</dead-letter-address>
            <expiry-address>jms.queue.ExpiryQueue</expiry-address>
            <redelivery-delay>5000</redelivery-delay>
            <max-delivery-attempts>2</max-delivery-attempts>
            <max-size-bytes>10485760</max-size-bytes>
            <address-full-policy>BLOCK</address-full-policy>
            <message-counter-history-day-limit>10</message-counter-history-day-limit>
          </address-setting>
        </address-settings>
        <jms-connection-factories>
          <connection-factory name="InVmConnectionFactory">
            <connectors>
              <connector-ref connector-name="in-vm"/>
            </connectors>
            <entries>
              <entry name="java:/ConnectionFactory"/>
            </entries>
          </connection-factory>
          <pooled-connection-factory name="hornetq-ra">
            <transaction mode="xa"/>
            <connectors>
              <connector-ref connector-name="in-vm"/>
            </connectors>
            <entries>
              <entry name="java:/JmsXA"/>
            </entries>
          </pooled-connection-factory>
        </jms-connection-factories>

        <jms-destinations>
          <jms-queue name="MailsQueue">
            <entry name="jms/queue/MailsQueue"/>
            <durable>true</durable>
          </jms-queue>
          <jms-queue name="ExpiryQueue">
            <entry name="jms/queue/ExpiryQueue"/>
            <durable>true</durable>
          </jms-queue>
          <jms-queue name="DLQ">
            <entry name="jms/queue/DLQ"/>
            <durable>true</durable>
          </jms-queue>
        </jms-destinations>
      </hornetq-server>
    </subsystem>
```

Restart your server.