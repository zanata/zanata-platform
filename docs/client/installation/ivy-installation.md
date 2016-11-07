### RHEL/Fedora

For RHEL 6, you will need to enable EPEL repository, with a command like this:

```
sudo rpm -Uvh http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm
```

Then install Ivy (this should work on Fedora too): 

```
sudo yum install apache-ivy
```

*Note:* The Ivy client will download required files the first time it is run, which may take several minutes. It will show `resolving dependencies...` while this is happening.

### Other systems

Download an [Ivy binary distribution](http://ant.apache.org/ivy/download.cgi), extract Ivy's jar file somewhere, and set the environment variable `IVY_JAR` to point to it.

For example: `export IVY_JAR=~/apps/apache-ivy-2.3.0/ivy-2.3.0.jar`