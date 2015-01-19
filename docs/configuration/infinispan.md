# Infinispan

_This section is still under review and is about features that have not been released yet_

Zanata uses Infinispan to manage its internal data caches and search indexes. Configuration for these caches happens in JBoss' `standalone/configuration/standalone.xml`. There are two different caches that need to be configured for Zanata:

1. Hibernate search Indexes
1. Other internal data caches

The Infinispan configuration will be located inside the following module in `standalone.xml`:

```xml
<subsystem xmlns="urn:jboss:domain:infinispan:1.4">
...
</subsystem>
```

Keep in mind that the module version may vary depending on your JBoss version.

### Hibernate Cache

The following is the recommended configuration for the Hibernate cache:

```xml
...
<cache-container name="hibernate" default-cache="local-query" jndi-name="java:jboss/infinispan/container/hibernate" start="EAGER" module="org.jboss.as.jpa.hibernate:4">
    <local-cache name="entity">
        <transaction mode="NON_XA"/>
        <eviction strategy="LRU" max-entries="10000"/>
        <expiration max-idle="100000"/>
   	</local-cache>
    <local-cache name="local-query">
        <transaction mode="NONE"/>
        <eviction strategy="LRU" max-entries="10000"/>
        <expiration max-idle="100000"/>
    </local-cache>
    <local-cache name="timestamps">
        <transaction mode="NONE"/>
        <eviction strategy="NONE"/>
    </local-cache>
</cache-container>
...
```

Depending on your JBoss installation, the hibernate cache might already be present in the configuration, in which case there is no need to create another one, but just modify it.

### Other internal data caches

```xml
...
<cache-container name="zanata" default-cache="default" jndi-name="java:jboss/infinispan/container/zanata" 
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
