# Infinispan for caching

Zanata uses Infinispan to manage some of its internal data caches, eg for statistics calculations.

Zanata has built-in default configuration for Infinispan caching (in `zanata-infinispan.xml`), but if you wish to change the cache size, or whether statistics *about* the caches should be collected, you may wish to modify this file.

A reference copy can be viewed here (although you may wish to view a different branch or tag): https://github.com/zanata/zanata-server/blob/release/zanata-war/src/main/resources/zanata-infinispan.xml

To modify cache configuration, you should copy the file `zanata-infinispan.xml` somewhere, then set the system property `zanata.infinispan.cfg` in `standalone.xml` to the full filename of your file.
