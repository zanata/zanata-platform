Zanata Overlay
==============

This modules builds an overlay zip for distribution. The produced artifact is a
zip file which can be extracted on top of an EAP or wildfly distribution and will
contain all the necessary artifacts for Zanata to run.

It's built using gradle, which is assumed to be present in the environment.

To build the overlay simply run:

```sh
gradle
```

Optional parameters:

| `zanata.war.location` | Location of the war file to be packaged                                                          |
|-----------------------|--------------------------------------------------------------------------------------------------|
| `zanata.version`      | Version of the package being build. By default the build will look at the top-level pom.xml file |

Example:

```sh
gradle -Dzanata.war.location=<WAR FILE LOCATION> -Dzanata.version=<ZANATA VERSION>
```

Repo Contents
-------------

_common_

Contains all the common artifacts to be packaged for all distributions.

_config_

Contains all configuration files for the build.

_distros_

Folders for each different distribution to be built. The contents of each 
distribution will be packaged exclusively for that distribution. If there is a
need for another distribution, simply add a new folder here and it will be
detected automatically.

_build.gradle_

The gradle build file.