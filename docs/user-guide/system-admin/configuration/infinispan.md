# Infinispan for caching

Zanata uses Infinispan to manage some of its internal data caches. Configuration for these caches happens in JBoss' `standalone/configuration/standalone.xml`.

The Infinispan configuration will be located inside the following module in `standalone.xml`:

```xml
<subsystem xmlns="urn:jboss:domain:infinispan:1.4">
...
</subsystem>
```

Keep in mind that the module version may vary depending on your JBoss version.

### Configuration for Internal data caches

```xml
...
<cache-container name="zanata" default-cache="default"
    jndi-name="java:jboss/infinispan/container/zanata"
    start="EAGER"
    module="org.jboss.as.clustering.web.infinispan">
    <local-cache name="default">
        <transaction mode="NON_XA"/>
        <eviction strategy="LRU" max-entries="10000"/>
        <expiration max-idle="100000"/>
    </local-cache>
</cache-container>
...
```

*Please see the JBoss EAP or Wildfly documentation for more options on cache configuration.*
